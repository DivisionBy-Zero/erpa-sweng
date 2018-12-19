package ch.epfl.sweng.erpa.modules;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.android.dx.stock.ProxyBuilder;
import com.annimon.stream.Exceptional;
import com.annimon.stream.Stream;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Supplier;

import ch.epfl.sweng.erpa.operations.DependencyCoordinator;
import ch.epfl.sweng.erpa.operations.annotations.Service;
import toothpick.Scope;
import toothpick.config.Module;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;

/**
 * TODO @Roos comment please
 */
@SuppressWarnings("unchecked")
public class TrivialProxifiedModules extends Module {
    private Scope scope;
    private Map<Class, DependencyCoordinator> registeredCoordinators;

    public TrivialProxifiedModules(Scope scope, Class<? extends DependencyCoordinator>... dependencyConfigurators) {
        this.scope = scope;
        registeredCoordinators = scope.getInstance(Map.class, RES_DEPENDENCY_COORDINATORS);
        Stream.of(dependencyConfigurators)
                // Get an instance for this dependencyConfigurator
                .map(dcClass -> {
                    DependencyCoordinator instance = registeredCoordinators.get(dcClass);
                    if (instance == null) instance = scope.getInstance(dcClass);
                    return instance;
                })
                // Put it in the registeredCoordinators map
                .peek(dc -> registeredCoordinators.put(dc.configuredDependencyClass(), dc))
                // Make a proxy instance of its configured class and publish it in the scope
                .peek(this::bindProxyfiedInstance)
                // Scan the configured class for services -- Cast needed by compiler
                .forEach(dc -> registerCoordinatorDependencyServices((DependencyCoordinator) dc));
    }

    private <T> void bindProxyfiedInstance(DependencyCoordinator<T> dependencyCoordinator) {
        Class<T> cls = dependencyCoordinator.configuredDependencyClass();
        T proxifiedInstance = Exceptional.of(() -> getInterfaceProxifiedInstance(dependencyCoordinator, cls))
                .recover((e) -> getClassProxifiedInstance(dependencyCoordinator, cls))
                .getOrThrowRuntimeException();
        this.bind(cls).toInstance(proxifiedInstance);
    }

    @NonNull
    private <T> T getInterfaceProxifiedInstance(DependencyCoordinator<T> dependencyCoordinator, Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, registeredCoordinators.get(cls));
    }

    private <T> T getClassProxifiedInstance(DependencyCoordinator<T> dependencyCoordinator, Class<T> cls) throws IOException {
        return ProxyBuilder.forClass(cls)
                .dexCache((scope.getInstance(Application.class)).getDir("dx", Context.MODE_PRIVATE))
                .handler(dependencyCoordinator)
                .build();
    }

    private <T> void registerCoordinatorDependencyServices(DependencyCoordinator<T> dependencyCoordinator) {
        Class<T> cls = dependencyCoordinator.configuredDependencyClass();
        Stream.of(cls.getMethods())
                .filter(m -> m.isAnnotationPresent(Service.class))
                .map(m -> new AutomaticDependentServiceCoordinator(m.getReturnType(), cls))
                .forEach(this::bindProxyfiedInstance);
    }

    private class AutomaticDependentServiceCoordinator<T> implements DependencyCoordinator<T> {
        private Class<T> serviceClass;
        private Class serviceProviderClass;
        private Method serviceInstanceGetter;

        AutomaticDependentServiceCoordinator(Class<T> serviceClass, Class serviceProviderClass) {
            this.serviceClass = serviceClass;
            this.serviceProviderClass = serviceProviderClass;
            this.serviceInstanceGetter = Stream.of(serviceProviderClass.getMethods())
                    .filter(m -> m.getParameterTypes().length == 0)
                    .filter(m -> m.isAnnotationPresent(Service.class))
                    .filter(m -> serviceClass.isAssignableFrom(m.getReturnType()))
                    .findFirst().orElseThrow(() -> new RuntimeException(String.format(
                            "Couldn't find service instance getter for service %s on class %s",
                            serviceClass.getName(), serviceProviderClass.getName())
                    ));
            registeredCoordinators.put(serviceClass, this);
        }

        private T getServiceInstanceFromProvider() {
            return (T) Exceptional.of(() ->
                    getServiceProviderInstance().invoke(null, serviceInstanceGetter, new Object[0])).get();
        }

        private DependencyCoordinator getServiceProviderInstance() {
            return registeredCoordinators.get(serviceProviderClass);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(getServiceInstanceFromProvider(), args);
        }

        @Override public boolean dependencyIsConfigured() {
            return getServiceProviderInstance().dependencyIsConfigured();
        }

        @Override public Intent dependencyConfigurationIntent() {
            return getServiceProviderInstance().dependencyConfigurationIntent();
        }

        @Override public Class<T> configuredDependencyClass() {
            return serviceClass;
        }
    }
}
