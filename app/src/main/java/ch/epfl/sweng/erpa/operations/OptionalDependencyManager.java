package ch.epfl.sweng.erpa.operations;

import com.annimon.stream.Optional;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import toothpick.Scope;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;

@Singleton
public class OptionalDependencyManager {
    @Inject @Named(RES_DEPENDENCY_COORDINATORS) Map<Class, DependencyCoordinator<?>> coordinators;
    @Inject @Named(RES_APPLICATION_SCOPE) Scope scope;

    @Inject OptionalDependencyManager() {
    }

    public <T> Optional<T> get(Class<T> dependencyClass) {
        if (coordinators.containsKey(dependencyClass)) {
            return Optional.ofNullable(coordinators.get(dependencyClass))
                    .filter(DependencyCoordinator::dependencyIsConfigured)
                    .map(dc -> scope.getInstance(dependencyClass));
        }
        return Optional.of(scope.getInstance(dependencyClass));
    }
}
