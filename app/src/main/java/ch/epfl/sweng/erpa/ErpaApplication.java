package ch.epfl.sweng.erpa;

import android.app.Application;

import java.lang.reflect.Proxy;

import ch.epfl.sweng.erpa.modules.ErpaApplicationModule;
import ch.epfl.sweng.erpa.smoothie.FactoryRegistry;
import ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

public class ErpaApplication extends Application {
    private Scope appScope = Toothpick.openScope(this);;

    @Override
    public void onCreate() {
        super.onCreate();

        Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
        FactoryRegistryLocator.setRootRegistry(new FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new MemberInjectorRegistry());

        appScope.installModules(new ErpaApplicationModule(this));
        appScope.installModules(new Module(){{
            bind(Scope.class).withName("application").toInstance(appScope);
        }});
    }
}
