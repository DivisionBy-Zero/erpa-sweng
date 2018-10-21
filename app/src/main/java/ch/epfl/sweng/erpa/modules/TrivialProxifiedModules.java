package ch.epfl.sweng.erpa.modules;

import com.annimon.stream.Stream;

import java.lang.reflect.Proxy;
import java.util.Set;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.operations.DependencyCoordinator;
import toothpick.Scope;
import toothpick.config.Module;

@SuppressWarnings("unchecked")
public class TrivialProxifiedModules extends Module {
    private Scope scope;

    public TrivialProxifiedModules(Scope scope, Class<? extends DependencyCoordinator>... dependencyConfigurators) {
        this.scope = scope;
        Set<DependencyCoordinator> registeredCoordinators =
                scope.getInstance(Set.class, ErpaApplication.RES_DEPENDENCY_COORDINATORS);
        Stream.of(dependencyConfigurators)
                .map(scope::getInstance)
                .peek(registeredCoordinators::add)
                .forEach(this::bindProxyfiedInstance);
    }

    private <T> void bindProxyfiedInstance(DependencyCoordinator<T> dependencyCoordinator) {
        Class<T> cls = dependencyCoordinator.configuredDependencyClass();
        T proxifiedInstance = (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls},
                scope.getInstance(dependencyCoordinator.getClass()));
        this.bind(cls).toInstance(proxifiedInstance);
    }
}
