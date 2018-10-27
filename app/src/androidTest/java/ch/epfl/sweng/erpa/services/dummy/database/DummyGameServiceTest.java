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
import java.util.List;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.util.TestUtils;

import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static ch.epfl.sweng.erpa.util.TestUtils.numTests;
import static ch.epfl.sweng.erpa.util.TestUtils.populateUUIDObjects;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTest {
    private GameService gs;

    @Before
    public void initGS() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        gs = new DummyGameService(ctx);
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
