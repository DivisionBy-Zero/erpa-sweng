package ch.epfl.sweng.erpa.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.DependencyConfigurationHelper;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GCP.GCPGameService;
import ch.epfl.sweng.erpa.services.GCP.GCPUserManagementService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyUserService;
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

        SharedPreferencesProvider preferencesProvider = new SharedPreferencesProvider(application,
            application.getString(R.string.preference_file_key));

        this.bind(Application.class).toInstance(application);
        this.bind(Context.class).toInstance(application);
        this.bind(DependencyConfigurationHelper.class).to(DependencyConfigurationHelper.class);
        this.bind(ErpaApplication.class).toInstance(application);
        this.bind(OptionalDependencyManager.class).to(OptionalDependencyManager.class);
        this.bind(RemoteServicesProviderCoordinator.class).to(RemoteServicesProviderCoordinator.class);
        this.bind(SharedPreferences.class).toProviderInstance(preferencesProvider);

        // Dummy Remote Services Provider-related binds
        this.bind(DummyGameService.class).to(DummyGameService.class);
        this.bind(DummyUserService.class).to(DummyUserService.class);

        // Google platform services
        this.bind(GCPGameService.class).to(GCPGameService.class);
        this.bind(GCPUserManagementService.class).to(GCPUserManagementService.class);

        this.bind(LoggedUserCoordinator.class).to(LoggedUserCoordinator.class);
    }
}
