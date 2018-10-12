package ch.epfl.sweng.erpa.operations;

import android.content.Intent;

public interface DependencyConfigurator<T> {
    boolean dependencyIsConfigured();

    Intent dependencyConfigurationIntent();

    Class<T> configuredDependencyClass();
}
