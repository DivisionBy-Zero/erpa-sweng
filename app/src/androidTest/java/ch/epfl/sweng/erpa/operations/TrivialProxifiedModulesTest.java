package ch.epfl.sweng.erpa.operations;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.modules.TrivialProxifiedModules;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class TrivialProxifiedModulesTest {
    Scope scope;

    @Before
    public void prepare() {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());
        scope = Toothpick.openScope(InstrumentationRegistry.getContext());
        scope.installModules(new Module() {{
            bind(Scope.class).withName(RES_APPLICATION_SCOPE).toInstance(scope);
            bind(DependencyConfigurationHelper.class).to(DependencyConfigurationHelper.class);
            bind(Set.class).withName(RES_DEPENDENCY_COORDINATORS)
                    .toInstance(new HashSet<DependencyCoordinator>());
        }});
    }

    @Test
    public void testBindsSimpleInstance() {
        scope.installModules(new TrivialProxifiedModules(scope, TestClass.class));
    }
}

class TestDependencyClass {}
class TestClass implements DependencyCoordinator<TestDependencyClass> {
    @Inject TestClass() {}
    @Override public boolean dependencyIsConfigured() { return false; }
    @Override public Intent dependencyConfigurationIntent() { return null; }
    @Override public Class<TestDependencyClass> configuredDependencyClass() { return TestDependencyClass.class; }
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { return null; }
}
