package ch.epfl.sweng.erpa.services.dummy.database;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.modules.ErpaApplicationModule;
import ch.epfl.sweng.erpa.operations.DependencyConfigurationHelper;
import ch.epfl.sweng.erpa.operations.DependencyCoordinator;
import ch.epfl.sweng.erpa.services.dummy.database.DummyUserService;
import ch.epfl.sweng.erpa.util.TestUtils;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;
import static ch.epfl.sweng.erpa.util.TestUtils.getUserProfile;
import static ch.epfl.sweng.erpa.util.TestUtils.populateUUIDObjects;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyUserProfileServiceTest {

    DummyUserService ups;

    @Before
    public void prepare() {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());
        Scope scope = Toothpick.openScope(InstrumentationRegistry.getTargetContext().getApplicationContext());
        ErpaApplication application = scope.getInstance(ErpaApplication.class);

        Toothpick.reset(scope);
        scope.installModules(new ErpaApplicationModule(application, scope));

        scope.installModules(new Module() {{
            bind(Scope.class).withName(RES_APPLICATION_SCOPE).toInstance(scope);
            bind(DependencyConfigurationHelper.class).to(DependencyConfigurationHelper.class);
            bind(Map.class).withName(RES_DEPENDENCY_COORDINATORS)
                    .toInstance(new HashMap<Class, DependencyCoordinator>());
        }});
        ups = scope.getInstance(DummyUserService.class);
    }

    @Test
    public void testAddedPersists() {
        String uid = "-1";
        UserProfile up = getUserProfile(uid);
        ups.saveUserProfile(up);
        Optional<UserProfile> optUp = ups.getUserProfile(uid);
        assertTrue(optUp.isPresent());
        assertEquals(up, optUp.get());
    }

    @Test
    public void testAllAdded() {
        int numTests = 500;
        List<UserProfile> userProfiles = new ArrayList<>(numTests);

        populateUUIDObjects(userProfiles, ups, TestUtils::getUserProfile);
        assertTrue("Contains all added elements", ups.getAllUserProfiles().containsAll(userProfiles));
    }
}
