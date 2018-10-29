package ch.epfl.sweng.erpa.operations;

import android.content.Intent;

import com.annimon.stream.Optional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserProfileService;

import static ch.epfl.sweng.erpa.services.UserSignupService.createAccessToken;

@Singleton
public class UserProfileCoordinator implements DependencyCoordinator<UserProfile> {

    private Optional<UserProfile> currentProfile = Optional.empty();

    @Inject public UserProfileCoordinator() {
        // TODO(@Anne): Remove this when login is implemented
        String uid = "user|5b915f75-0ff0-43f8-90bf-f9e92533f926";
        UserProfile defaultUser = new UserProfile(uid, "admin", createAccessToken(uid, "admin"), UserProfile.Experience.Casual, false, true);
        currentProfile = Optional.of(defaultUser);
    }

    @Override public boolean dependencyIsConfigured() {
        return currentProfile.isPresent();
    }

    @Override public Intent dependencyConfigurationIntent() {
        // TODO(@Anne): return login activity Intent
        throw new RuntimeException("Login activity non implemented");
    }

    @Override public Class<UserProfile> configuredDependencyClass() {
        return UserProfile.class;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!currentProfile.isPresent()) throw new IllegalStateException("No user logged in");
        return method.invoke(currentProfile.get(), args);
    }
}
