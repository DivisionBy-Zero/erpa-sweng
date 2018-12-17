package ch.epfl.sweng.erpa.activities;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Stream;

import org.junit.Before;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUser;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.UserManagementService;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

public abstract class DependencyConfigurationAgnosticTest {
    Scope scope; // No @Inject, manually bound
    @SuppressWarnings("unused") // Populated on injection
    @Inject private RemoteServicesProviderCoordinator remoteServicesProviderCoordinator;

    public static void registerCurrentlyLoggedUser(LoggedUserCoordinator coordinator, Username currentUser) {
        UserSessionToken userSessionToken = new UserSessionToken(currentUser.getUserUuid(), currentUser.getUserUuid());
        UserProfile userProfile = new UserProfile(currentUser.getUserUuid(), true, true);
        coordinator.setCurrentLoggedUser(new LoggedUser(userSessionToken, userProfile, currentUser));

        DummyGameService.currentUserUuid = currentUser.getUserUuid();
    }

    public static Username registerUsername(UserManagementService userManagementService, String username) {
        return Exceptional.of(() -> new Username(userManagementService.registerNewUsername(username), username)).get();
    }

    @Before
    public void prepare() throws Throwable {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());
        scope = Toothpick.openScope(InstrumentationRegistry.getTargetContext().getApplicationContext());
        ErpaApplication application = scope.getInstance(ErpaApplication.class);

        Toothpick.reset(scope);
        application.installModules(scope);

        // Manually inject the class since toothpick doesn't work here...
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = this.getClass(); c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }

        Stream.of(fields)
            .filter(f -> f.isAnnotationPresent(Inject.class))
            .peek(m -> Log.i("Test Injector", "Field " + m.toGenericString()))
            .peek(m -> System.out.println(m.toGenericString()))
            .peek(m -> m.setAccessible(true))
            .forEach(m -> Exceptional.of(() -> {
                m.set(this, scope.getInstance(m.getType()));
                return null;
            }).getOrThrowRuntimeException());
        remoteServicesProviderCoordinator.bindRemoteServicesProvider(DummyRemoteServicesProvider.class);
    }
}
