package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.operations.DependencyConfigurationHelper;
import ch.epfl.sweng.erpa.operations.DependencyCoordinator;
import toothpick.Toothpick;

public abstract class DependencyConfigurationAgnosticActivity extends AppCompatActivity {
    public static final String REQUESTING_ACTIVITY_INTENT_KEY = "Parent " + Intent.class.getCanonicalName();
    @Inject DependencyConfigurationHelper dependencyConfigurationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toothpick.inject(this, Toothpick.openScopes(getApplication(), this));

        List<Class> unconfiguredDependencies =
                dependencyConfigurationHelper.getNotConfiguredDependenciesForInstance(this);

        if (!unconfiguredDependencies.isEmpty()) {
            List<Intent> configurationActivities = Stream.of(unconfiguredDependencies)
                    .map(depCls -> dependencyConfigurationHelper
                            .getDependencyConfiguratorForClass(depCls)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Cannot find Configurator for class " + depCls.getName())))
                    .map(DependencyCoordinator::dependencyConfigurationIntent)
                    .map(i -> i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME))
                    .collect(Collectors.toList());

            // Push ourselves on the top of the call stack
            this.startActivity(getIntent());
            // Push the configuration activities on top
            //noinspection SimplifyStreamApiCallChains: This warning is wrong
            this.startActivities(Stream.of(configurationActivities).toArray(Intent[]::new));
            this.finish();
        }
    }

    public boolean dependenciesNotReady() {
        return isFinishing();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
