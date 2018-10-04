package ch.epfl.sweng.erpa.modules;

import android.app.Application;
import android.content.SharedPreferences;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import toothpick.config.Module;
import toothpick.smoothie.provider.SharedPreferencesProvider;

/**
 * This class provides the component implementation injected in an Application scope.
 *
 * @link https://github.com/stephanenicolas/toothpick/wiki/Scopes
 */
public class ErpaApplicationModule extends Module {
    public ErpaApplicationModule(ErpaApplication application) {
        this(application, application.getString(R.string.preference_file_key));
    }

    public ErpaApplicationModule(ErpaApplication application, String preferencesName) {
        this.bind(Application.class).toInstance(application);
        this.bind(ErpaApplication.class).toInstance(application);
        this.bind(SharedPreferences.class).toProviderInstance(new SharedPreferencesProvider(application, preferencesName));
    }
}
