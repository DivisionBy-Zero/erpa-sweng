package ch.epfl.sweng.erpa;

import android.app.Application;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.modules.ErpaApplicationModule;
import ch.epfl.sweng.erpa.modules.TrivialProxifiedModules;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GCP.GCPRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

@SuppressWarnings("unchecked")
public class ErpaApplication extends Application {
    public static final String RES_APPLICATION_SCOPE = "Application Scope";
    public static final String RES_DEPENDENCY_COORDINATORS = "Dependency Coordinators";
    public static final String RES_REMOTE_SERVICES_PROVIDERS = "Remote Service Providers";
    public static final String RES_LIST_OF_GAMES = "List of games";

    // Remote Service Providers
    private final Set<Class<? extends RemoteServicesProvider>> remoteServicesProviders = Stream.of(
            DummyRemoteServicesProvider.class,
            GCPRemoteServicesProvider.class
    ).collect(Collectors.toSet());

    List<Game> sampleListOfGames = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialise Dependency Injection framework
        Toothpick.setConfiguration(Configuration.forProduction().disableReflection().preventMultipleRootScopes());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());

        installModules(Toothpick.openScope(this));
    }

    public void installModules(Scope appScope) {
        // Install trivial application modules
        appScope.installModules(new ErpaApplicationModule(this, appScope));

        // Publish collection instances
        appScope.installModules(new Module() {{
            bind(Set.class).withName(RES_REMOTE_SERVICES_PROVIDERS).toInstance(remoteServicesProviders);
            bind(Map.class).withName(RES_DEPENDENCY_COORDINATORS).toInstance(new HashMap());
            bind(List.class).withName(RES_LIST_OF_GAMES).toInstance(sampleListOfGames);
        }});

        // Service coordinators
        appScope.installModules(new TrivialProxifiedModules(appScope,
            RemoteServicesProviderCoordinator.class
        ));
        // Service coordinators
        appScope.installModules(new TrivialProxifiedModules(appScope,
            LoggedUserCoordinator.class
        ));
    }
}
