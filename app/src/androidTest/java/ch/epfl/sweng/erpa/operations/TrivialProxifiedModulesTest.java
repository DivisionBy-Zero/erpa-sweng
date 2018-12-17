package ch.epfl.sweng.erpa.operations;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.activities.DependencyConfigurationAgnosticTest;
import ch.epfl.sweng.erpa.modules.ErpaApplicationModule;
import ch.epfl.sweng.erpa.modules.TrivialProxifiedModules;
import ch.epfl.sweng.erpa.operations.helpers.TestDependencyAbstractClass;
import ch.epfl.sweng.erpa.operations.helpers.TestDependencyClass;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class TrivialProxifiedModulesTest extends DependencyConfigurationAgnosticTest {
    @Inject Scope scope;

    @Test
    public void testBindsSimpleInstance() throws Throwable {
        MyTestCoordinator myTestClassMock = Mockito.mock(MyTestCoordinator.class);
        when(myTestClassMock.configuredDependencyClass()).thenReturn(TestDependencyClass.class);

        // Publish the coordinator in the scope
        scope.installModules(new Module() {{
            bind(MyTestCoordinator.class).toInstance(myTestClassMock);
        }});

        // When installing a TrivialProxifiedModule with MyTestCoordinator
        scope.installModules(new TrivialProxifiedModules(scope, MyTestCoordinator.class));

        // Verify that the given instance was considered
        verify(myTestClassMock, atLeastOnce()).configuredDependencyClass();

        // Verify that we've never been called to proxy
        verify(myTestClassMock, never()).invoke(any(), any(), any());

        Object expected = "How did I learn to stop worrying and love the bomb.";
        when(myTestClassMock.invoke(any(), any(), any())).thenReturn(expected);
        // Ask for an instance of the class proxyfied by the coordinator and call a method
        assertEquals(expected, scope.getInstance(TestDependencyClass.class).method());
        // Verify that the coordinator was invoked to supply the value
        verify(myTestClassMock, times(1)).invoke(any(), any(), any());
    }

    @Test
    public void testBindsAbstractInstance() throws Throwable {
        AbstractTestCoordinator myTestClassMock = Mockito.mock(AbstractTestCoordinator.class);
        when(myTestClassMock.configuredDependencyClass()).thenReturn(TestDependencyAbstractClass.class);

        // Publish the coordinator in the scope
        scope.installModules(new Module() {{
            bind(AbstractTestCoordinator.class).toInstance(myTestClassMock);
        }});

        // When installing a TrivialProxifiedModule with AbstractTestCoordinator
        scope.installModules(new TrivialProxifiedModules(scope, AbstractTestCoordinator.class));

        // Verify that the given instance was considered
        verify(myTestClassMock, atLeastOnce()).configuredDependencyClass();

        // Verify that we've never been called to proxy
        verify(myTestClassMock, never()).invoke(any(), any(), any());

        Object expected = "How did I learn to stop worrying and love the bomb.";
        when(myTestClassMock.invoke(any(), any(), any())).thenReturn(expected);
        // Ask for an instance of the class proxyfied by the coordinator and call a method
        assertEquals(expected, scope.getInstance(TestDependencyAbstractClass.class).method());
        // Verify that the coordinator was invoked to supply the value
        verify(myTestClassMock, times(1)).invoke(any(), any(), any());
    }

    @Test
    public void testBindsNestedServices() throws Throwable {
        // Publish and install the coordinator in the scope
        scope.installModules(new Module() {{
            bind(MyTestCoordinator.class).toInstance(new MyTestCoordinator());
        }});
        scope.installModules(new TrivialProxifiedModules(scope, MyTestCoordinator.class));


        TestDependencyClass.TestServiceInterface serviceMock1 =
                Objects.requireNonNull(Mockito.mock(TestDependencyClass.TestServiceInterface.class));
        scope.getInstance(MyTestCoordinator.class).ret = serviceMock1;
        // Ask for an instance of the nested service and call a method
        scope.getInstance(TestDependencyClass.TestServiceInterface.class).getObject();

        TestDependencyClass.TestServiceInterface serviceMock2 =
                Objects.requireNonNull(Mockito.mock(TestDependencyClass.TestServiceInterface.class));
        scope.getInstance(MyTestCoordinator.class).ret = serviceMock2;
        // Ask for an instance of the nested service and call a method
        scope.getInstance(TestDependencyClass.TestServiceInterface.class).getObject();


        // Verify that the service methods were called only once
        verify(serviceMock1, times(1)).getObject();
        verify(serviceMock2, times(1)).getObject();
    }

    // @formatter:off
    public static class MyTestCoordinator implements DependencyCoordinator<TestDependencyClass> {
        TestDependencyClass.TestServiceInterface ret;

        @Override public boolean dependencyIsConfigured() { return false; }
        @Override public Intent dependencyConfigurationIntent() { return null; }
        @Override public Class<TestDependencyClass> configuredDependencyClass() { return TestDependencyClass.class; }
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { return ret; }
    }

    public static class AbstractTestCoordinator implements DependencyCoordinator<TestDependencyAbstractClass> {
        TestDependencyClass.TestServiceInterface ret;

        @Override public boolean dependencyIsConfigured() { return false; }
        @Override public Intent dependencyConfigurationIntent() { return null; }
        @Override public Class<TestDependencyAbstractClass> configuredDependencyClass() { return TestDependencyAbstractClass.class; }
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { return ret; }
    }
    // @formatter:on
}
