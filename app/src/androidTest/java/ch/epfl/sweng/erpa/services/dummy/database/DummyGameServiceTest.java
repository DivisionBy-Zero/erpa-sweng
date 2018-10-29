package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.modules.ErpaApplicationModule;
import ch.epfl.sweng.erpa.operations.DependencyConfigurationHelper;
import ch.epfl.sweng.erpa.operations.DependencyCoordinator;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.util.TestUtils;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.config.Module;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static ch.epfl.sweng.erpa.ErpaApplication.RES_APPLICATION_SCOPE;
import static ch.epfl.sweng.erpa.ErpaApplication.RES_DEPENDENCY_COORDINATORS;
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static ch.epfl.sweng.erpa.util.TestUtils.numTests;
import static ch.epfl.sweng.erpa.util.TestUtils.populateUUIDObjects;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTest {
    private GameService gs;

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
        gs = scope.getInstance(DummyGameService.class);
    }
    @After
    public void cleanUp() {
        gs.removeGames();
    }

    @Test
    public void testConstant() {
        assertEquals(DummyGameService.SAVED_GAME_DATA_FOLDER,((DummyGameService)gs).dataFolder());
    }

    @Test
    public void testAdded() {
        Game g = getGame("testAdded");
        gs.saveGame(g);
        Optional<Game> res = gs.getGame(g.getGameUuid());
        assertTrue(res.isPresent());
        assertEquals(g,res.get());
    }
    @Test
    public void testAllAdded() {
        List<Game> games = new ArrayList<>(numTests);
        populateUUIDObjects(games,gs,TestUtils::getGame);
        assertTrue(gs.getAllGames().containsAll(games));
    }
}
