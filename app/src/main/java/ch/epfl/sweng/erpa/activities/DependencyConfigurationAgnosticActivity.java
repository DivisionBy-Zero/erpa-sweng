package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import ch.epfl.sweng.erpa.operations.DependencyConfigurator;
import toothpick.Scope;
import toothpick.Toothpick;

public abstract class DependencyConfigurationAgnosticActivity extends Activity {
    @Inject @Named("Dependency Configurators") Set<DependencyConfigurator> coordinators;

    private Scope scope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application application = getApplication();
        scope = Toothpick.openScopes(application, this);
        Toothpick.inject(this, scope);

        Set<Class> autowiredClasses = Stream.of(this.getClass().getFields())
                // Injected fields
                .filter(f -> f.isAnnotationPresent(Inject.class))
                // Field class
                .map(Field::getType)
                .collect(Collectors.toSet());

        Set<DependencyConfigurator> coordinatorsWithUnconfiguredDependencies = Stream.of(coordinators)
                .filter(cc -> autowiredClasses.contains(cc.configuredDependencyClass()))
                // Only coordinators with non-configured dependencies
                .filter(cc -> !cc.dependencyIsConfigured())
                .collect(Collectors.toSet());

        if (!coordinatorsWithUnconfiguredDependencies.isEmpty()) {
            List<Intent> configurationActivities = Stream.of(coordinatorsWithUnconfiguredDependencies)
                    .map(DependencyConfigurator::dependencyConfigurationIntent)
                    .map(i -> i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME))
                    .collect(Collectors.toList());

            // Push ourselves on the top of the call stack
            this.startActivity(new Intent(this, this.getClass()));
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
        Toothpick.inject(this, scope);
    }
}
