package ch.epfl.sweng.erpa.operations;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;

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

@Singleton
public class LoggedUserCoordinator implements DependencyCoordinator<LoggedUser> {
    private final Intent configurationIntent;
    @Inject Context ctx;
    @Inject UserManagementService ups;
    private Optional<LoggedUser> currentLoggedUser = Optional.empty();

    @Inject public LoggedUserCoordinator(Context ctx) {
        configurationIntent = new Intent(ctx, LoginActivity.class);
    }

    private static <A> Consumer<A> failIfNull(String message, Consumer<A> next) {
        return a -> next.accept(Objects.requireNonNull(a, message));
    }

    @Override public boolean dependencyIsConfigured() {
        return currentLoggedUser.isPresent();
    }

    @Override public Intent dependencyConfigurationIntent() {
        return configurationIntent;
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
        return ts.run(() -> {
            String userUuid = ups.registerNewUsername(username);
            profile.setUuid(userUuid);
            String base64PublicKey = Base64.encodeToString(CryptoGrenouille
                .ed25519KeyPairFromPassphraseAndUserUuid(password, userUuid)
                .getPublic().getEncoded(), Base64.NO_WRAP);
            UserAuth userAuth = new UserAuth(userUuid, base64PublicKey, CryptoGrenouille.AUTH_STRATEGY_NAME);
            ups.registerAuth(userAuth);
            ups.registerUserProfile(profile);
            this.tryLogin(ts, username, password, successCallback, errorHandler);
            return null;
        }, o -> {
        }, errorHandler);
    }

    public AsyncTask<Void, Void, Exceptional<String>> tryLogin(
        AsyncTaskService ts, String usernameText, String password,
        Runnable successCallback, Consumer<Throwable> errorHandler) {
        return ts.run(() -> {
            String userUuid = ups.getUuidForUsername(usernameText);
            String b64AuthChallenge = ups.getBase64AuthenticationChallenge(userUuid);
            String challengeResponse = new CryptoGrenouille(password, userUuid).signBase64Encoded(b64AuthChallenge);
            UserSessionToken userSessionToken = ups.getSessionToken(userUuid, challengeResponse);
            Username username = ups.getUsernameFromUserUuid(userUuid).get();
            UserProfile userProfile = ups.getUserProfile(userUuid).get();
            this.publishLoggedUser(username, userSessionToken, userProfile);
            return null;
        }, o -> successCallback.run(), errorHandler);
    }

    private void publishLoggedUser(Username username, UserSessionToken userSessionToken, UserProfile userProfile) {
        setCurrentLoggedUser(new LoggedUser(userSessionToken, userProfile, username));
    }

    public void logout() {
        unsetCurrentLoggedUser();
    }
}
