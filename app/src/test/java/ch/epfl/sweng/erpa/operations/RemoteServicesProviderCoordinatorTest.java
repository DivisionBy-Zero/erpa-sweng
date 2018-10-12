package ch.epfl.sweng.erpa.operations;

import android.content.Context;
import android.content.SharedPreferences;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Proxy;

import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.config.Module;
import toothpick.testing.ToothPickRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteServicesProviderCoordinatorTest {
    @Rule public final ToothPickRule toothPickRule = new ToothPickRule(this, "rspCoordinatorTest");
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock Context androidApplicationContext;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) // Methods calls return mocks (e.g. builders).
            SharedPreferences sharedPreferences;

    private Scope scope;

    @Before
    public void setUp() {
        when(androidApplicationContext.getString(anyInt())).thenReturn("test");
        // Have a _the_ dummy implementation by default
        when(sharedPreferences.getString(anyString(), any())).thenReturn(DummyRemoteServicesProvider.class.getName());

        scope = toothPickRule.getScope();
        scope.installModules(new Module() {{
            bind(Scope.class).withName("application").toInstance(scope);
        }});
        toothPickRule.inject(this);
    }

    @Test
    public void testSelfConfiguringFromSharedPreferences() {
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        assertTrue(underTest.dependencyIsConfigured());
    }

    @Test
    public void testNoSelfConfiguringWhenSharedPreferencesIsEmpty() {
        when(sharedPreferences.getString(anyString(), any())).thenReturn(null);
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        assertFalse(underTest.dependencyIsConfigured());
    }

    @Test
    public void testNoSelfConfiguringWhenSharedPreferencesIsInvalid() {
        when(sharedPreferences.getString(anyString(), any()))
                .thenReturn(this.getClass().getName());
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        assertFalse(underTest.dependencyIsConfigured());
    }

    @Test
    public void testBindingNullRspDeconfiguresCoordinator() {
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        assertTrue(underTest.dependencyIsConfigured());
        underTest.bindRemoteServicesProvider(null);
        assertFalse(underTest.dependencyIsConfigured());
    }

    @Test
    public void testDependencyConfigurationIntentNotNull() {
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        assertNotNull(underTest.dependencyConfigurationIntent());
    }

    @Test
    public void testConfigureDependencyClass() {
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        assertEquals(RemoteServicesProvider.class, underTest.configuredDependencyClass());
    }

    @Test
    public void testReflectiveInvocationPassthrough() {
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        underTest.bindRemoteServicesProvider(null);
        assertFalse(underTest.dependencyIsConfigured());

        RemoteServicesProvider syntheticProvider = new SyntheticRemoteServicesProvider();
        underTest.bindRemoteServicesProvider(syntheticProvider.getClass());

        RemoteServicesProvider proxifiedInstance =
                (RemoteServicesProvider) Proxy.newProxyInstance(RemoteServicesProvider.class.getClassLoader(),
                        new Class[]{RemoteServicesProvider.class}, underTest);

        assertEquals(SyntheticRemoteServicesProvider.mlp, proxifiedInstance.getFriendlyProviderName());
    }

    @Test
    public void testTerminateIsCalledWhenBinderReplaced() {
        RemoteServicesProviderCoordinator underTest = scope.getInstance(RemoteServicesProviderCoordinator.class);
        underTest.bindRemoteServicesProvider(SyntheticRemoteServicesProvider.class);
        assertTrue(underTest.dependencyIsConfigured());

        Fuse fuse = ((SyntheticRemoteServicesProvider) underTest.getCurrentProvider().get()).fuse;
        underTest.bindRemoteServicesProvider(null);
        assertFalse(underTest.dependencyIsConfigured());

        assertTrue(fuse.ignited);
    }
}

class Fuse {
    boolean ignited = false;

    void ignite() {
        ignited = true;
    }
}

@SuppressWarnings("WeakerAccess")
class SyntheticRemoteServicesProvider implements RemoteServicesProvider {
    final static String mlp = "My little pony";
    public final Fuse fuse = new Fuse();

    public SyntheticRemoteServicesProvider() {
    }

    @Override
    public String getFriendlyProviderName() {
        return mlp;
    }

    @Override
    public String getFriendlyProviderDescription() {
        return null;
    }

    @Override
    public void terminate() {
        fuse.ignite();
    }

    @Override
    public Optional<String> getUidFromUsername(String username) {
        return null;
    }

    @Override
    public boolean verifyAccessToken(String uid, String accessToken) {
        return false;
    }
}
