package ch.epfl.sweng.erpa;

import android.app.Application;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.lang.reflect.Proxy;
import java.util.Set;

import ch.epfl.sweng.erpa.modules.ErpaApplicationModule;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.firebase.FirebaseRemoteServicesProvider;
import ch.epfl.sweng.erpa.smoothie.FactoryRegistry;
import ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

public class ErpaApplication extends Application {
    // Remote Service Providers
    @SuppressWarnings("unchecked")
    private final Set<Class<? extends RemoteServicesProvider>> remoteServicesProviders = Stream.of(
            DummyRemoteServicesProvider.class,
            FirebaseRemoteServicesProvider.class
    ).collect(Collectors.toSet());

    // Dependency Configurators
    @SuppressWarnings("unchecked")
    private final Set<Class<? extends RemoteServicesProviderCoordinator>> dependencyConfiguratorClasses = Stream.of(
            RemoteServicesProviderCoordinator.class
    ).collect(Collectors.toSet());

    @Override
    public void onCreate() {
        super.onCreate();
        initToothpick(Toothpick.openScope(this));
    }

    public void initToothpick(Scope appScope) {
        // Initialise Dependency Injection framework
        Toothpick.setConfiguration(Configuration.forProduction().disableReflection().preventMultipleRootScopes());
        FactoryRegistryLocator.setRootRegistry(new FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new MemberInjectorRegistry());

        // Install trivial application modules
        appScope.installModules(new ErpaApplicationModule(this));
        // Publish the general application scope
        appScope.installModules(new Module(){{
            bind(Scope.class).withName("application").toInstance(appScope);
        }});

        // Publish
        appScope.installModules(new Module(){{
            bind(Set.class).withName("Remote Service Providers").toInstance(remoteServicesProviders);
            bind(Set.class).withName("Dependency Configurators").toInstance(
                Stream.of(dependencyConfiguratorClasses).map(appScope::getInstance).collect(Collectors.toSet()));
        }});

        // Create Remote Service Provider singleton instance proxy
        RemoteServicesProvider proxifiedRspInstance = (RemoteServicesProvider)
            Proxy.newProxyInstance(RemoteServicesProvider.class.getClassLoader(),
                new Class[] { RemoteServicesProvider.class },
                appScope.getInstance(RemoteServicesProviderCoordinator.class));

        // Publish
        appScope.installModules(new Module(){{
            bind(RemoteServicesProvider.class).toInstance(proxifiedRspInstance);
        }});
    }
}
