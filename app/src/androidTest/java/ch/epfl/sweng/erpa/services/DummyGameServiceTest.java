package ch.epfl.sweng.erpa.services;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.annimon.stream.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;

import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTest {
    private DummyGameService gs;

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
    public void addedGamePersists() {
        Game g = getGame("addedGame");
        gs.saveGame(g);
        Optional<Game> found = gs.getGame(g.getGameUuid());
        assertTrue(found.isPresent());

        Game foundGame = found.get();
        assertEquals(g, foundGame);
    }

    @Test
    public void testAddedAll() {
        int numTests = 500;
        List<Game> games = new ArrayList<>(numTests);
        for (int i = 0; i < numTests; i++) {
            Game g = getGame(String.valueOf(i));
            games.add(g);
            gs.saveOne(g);
        }
        Set<Game> all = gs.getAll();
        assertTrue(all.containsAll(games));
    }

    @Test(expected = IllegalStateException.class)
    public void testExceptionOnIllegalSave() {
        Game evilGame = getGame("folder");
        File newFolder = new File(gs.getGameDir(), evilGame.getGameUuid() + DummyGameService.SAVED_GAME_FILE_EXTENSION);
        newFolder.delete();
        newFolder.mkdir();
        gs.saveGame(evilGame);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExceptionOnGet() {
        File evilFile = new File(gs.getGameDir(), "Evil");
        evilFile.delete();
        DummyGameService.fetchExistingGameFromFile(evilFile);
    }

    @Test
    public void removeSaveFile() {
        assertTrue(gs.removeGames());
    }
}
