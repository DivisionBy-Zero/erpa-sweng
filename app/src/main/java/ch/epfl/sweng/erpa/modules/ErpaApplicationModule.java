package ch.epfl.sweng.erpa.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import toothpick.config.Module;
import toothpick.smoothie.provider.SharedPreferencesProvider;

/**
 * This class provides the component implementation injected in an Application scope.
 *
 * @link https://github.com/stephanenicolas/toothpick/wiki/Scopes
 */
public class ErpaApplicationModule extends Module {
    public ErpaApplicationModule(ErpaApplication application) {
        SharedPreferencesProvider preferencesProvider =
                new SharedPreferencesProvider(application, application.getString(R.string.preference_file_key));

        this.bind(Application.class).toInstance(application);
        this.bind(Context.class).toInstance(application);
        this.bind(ErpaApplication.class).toInstance(application);
        this.bind(RemoteServicesProviderCoordinator.class).to(RemoteServicesProviderCoordinator.class);
        this.bind(SharedPreferences.class).toProviderInstance(preferencesProvider);
    }
}
