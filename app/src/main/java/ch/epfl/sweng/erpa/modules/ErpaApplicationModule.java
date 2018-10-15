package ch.epfl.sweng.erpa.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.operations.UserProfileCoordinator;
import ch.epfl.sweng.erpa.services.UserAuthService;
import ch.epfl.sweng.erpa.services.UserSignupService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import toothpick.Scope;
import toothpick.config.Module;
import toothpick.smoothie.provider.SharedPreferencesProvider;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;

/**
 * This class provides the component implementation injected in an Application scope.
 *
 * @link https://github.com/stephanenicolas/toothpick/wiki/Scopes
 */
public class ErpaApplicationModule extends Module {
    public ErpaApplicationModule(ErpaApplication application, Scope applicationScope) {
        this.bind(Scope.class).withName(RES_APPLICATION_SCOPE).toInstance(applicationScope);

        SharedPreferencesProvider preferencesProvider =
                new SharedPreferencesProvider(application,
                        application.getString(R.string.preference_file_key));

        this.bind(Application.class).toInstance(application);
        this.bind(Context.class).toInstance(application);
        this.bind(ErpaApplication.class).toInstance(application);
        this.bind(RemoteServicesProviderCoordinator.class).to(
                RemoteServicesProviderCoordinator.class);
        this.bind(SharedPreferences.class).toProviderInstance(preferencesProvider);
        this.bind(UserAuthService.class).to(UserAuthService.class);
        this.bind(UserProfileCoordinator.class).to(UserProfileCoordinator.class);
        this.bind(UserSignupService.class).to(UserSignupService.class);

        // Dummy Remote Services Provider-related binds
        this.bind(DummyGameService.class).to(DummyGameService.class);
        // TODO(@Roos) replace injection by UserProviderService
        this.bind(UserProfile.class).toInstance(new UserProfile("user|" + UUID.randomUUID().toString(), "kevinLeBeauGoss", "myAccesTocken", UserProfile.Experience.Noob, true, true));
    }
}
