package ch.epfl.sweng.erpa.operations;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Objects;
import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.activities.LoginActivity;
import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static ch.epfl.sweng.erpa.operations.AsyncTaskService.failIfNotFound;

@Singleton
public class LoggedUserCoordinator implements DependencyCoordinator<LoggedUser> {
    @Inject Context ctx;
    @Inject UserManagementService ups;
    private Optional<LoggedUser> currentLoggedUser = Optional.empty();

    @Inject public LoggedUserCoordinator() {
    }

    private static <A> Consumer<A> failIfNull(String message, Consumer<A> next) {
        return a -> next.accept(Objects.requireNonNull(a, message));
    }

    @Override public boolean dependencyIsConfigured() {
        return currentLoggedUser.isPresent();
    }

    @Override public Intent dependencyConfigurationIntent() {
        return new Intent(ctx, LoginActivity.class);
    }

    public void setCurrentLoggedUser(LoggedUser up) {
        this.currentLoggedUser = Optional.of(up);
    }

    public void unsetCurrentLoggedUser() {
        this.currentLoggedUser = Optional.empty();
    }

    @Override public Class<LoggedUser> configuredDependencyClass() {
        return LoggedUser.class;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!currentLoggedUser.isPresent()) throw new IllegalStateException("No user logged in");
        return method.invoke(currentLoggedUser.get(), args);
    }

    public AsyncTask<Void, Void, Exceptional<String>> trySignUp(
        AsyncTaskService ts, String username, String password, UserProfile profile,
        Runnable successCallback, Consumer<Throwable> errorHandler) {
        // @formatter:off Think of this like a scala query with the arrows graphically reversed...
        AsyncTaskService.Runner<String> signUp = ts.create(() -> ups.registerNewUsername(username),
            failIfNull("Could not register username",
            userUuid -> ts.run(() -> {
                profile.setUuid(userUuid);
                // TODO(@Roos): Use the UserAuthDerivation function when it's implemented
                return new UserAuth(userUuid, userUuid, "");
            }, failIfNull("The provided password is invalid",
            userAuth -> ts.run(() -> {
                // TODO(@Roos): Call the GetSessionToken endpoint with the password info
                ups.registerAuth(userAuth);
                return ups.registerUserProfile(profile);
            }, failIfNull("Could not finish registration",
            userProfile -> {
                this.tryLogin(ts, username, password, successCallback, errorHandler);
            }))))));
        // @formatter:on
        signUp.setThrowableConsumer(errorHandler);
        signUp.execute();
        return signUp;
    }

    public AsyncTask<Void, Void, Exceptional<String>> tryLogin(AsyncTaskService ts,
                                                               String usernameText, String passwordText,
                                                               Runnable successCallback,
                                                               Consumer<Throwable> errorHandler) {
        // @formatter:off Think of this like a scala query with the arrows graphically reversed...
        AsyncTaskService.Runner<String> login = ts.create(() -> ups.getUuidForUsername(usernameText),
            failIfNull(String.format("Could not retrieve UserUUID for username '%s'", usernameText),
            userUuid -> ts.run(() -> {
                // TODO(@Roos): Call the GetSessionToken endpoint with the password info
                return new UserSessionToken(userUuid, userUuid);
            },
            userSessionToken -> ts.run(() -> ups.getUsernameFromUserUuid(userUuid), failIfNotFound(userUuid,
            (Username username) -> ts.run(() -> ups.getUserProfile(userUuid), failIfNotFound(userUuid,
            (UserProfile userProfile) -> {
                this.publishLoggedUser(username, userSessionToken, userProfile);
                successCallback.run();
            })))))));
        // @formatter:on
        login.setThrowableConsumer(errorHandler);
        login.execute();
        return login;
    }

    private void publishLoggedUser(Username username, UserSessionToken userSessionToken, UserProfile userProfile) {
        setCurrentLoggedUser(new LoggedUser(userSessionToken, userProfile, username));
    }

    private void logout() {
        unsetCurrentLoggedUser();
    }
}
