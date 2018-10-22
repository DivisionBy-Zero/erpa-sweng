package ch.epfl.sweng.erpa.operations;

import android.content.Intent;

import java.lang.reflect.InvocationHandler;

public interface DependencyCoordinator<T> extends InvocationHandler {
    boolean dependencyIsConfigured();

    Intent dependencyConfigurationIntent();

    Class<T> configuredDependencyClass();
}
