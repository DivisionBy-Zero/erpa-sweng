package ch.epfl.sweng.erpa.services;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;

import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTest {
    DummyGameService gs;

    @Before
    public void initGS() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        gs = new DummyGameService(ctx);
    }

    @Test
    public void addedGamePersists() {
        Game g = getGame("addedGame");
        gs.saveOne(g);
        Optional<Game> found = gs.getOne(g.getGameUuid());
        assertTrue (found.isPresent());
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


}
