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

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTests {
    DummyGameService gs;

    @Before
    public void initGS() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        gs = new DummyGameService(ctx);
    }

    @Test
    public void addedGamePersists() {
        Game g = getGame("addedGame");
        gs.saveGame(g);
        Optional<Game> found = gs.getGame(g.getGameUuid());
        assert (found.isPresent());
        Game foundGame = found.get();
        assert (g.equals(foundGame));


    }

    @Test
    public void testAddedAll() {
        int numTests = 500;
        List<Game> games = new ArrayList<>(numTests);
        for (int i = 0; i < numTests; i++) {
            Game g = getGame(String.valueOf(i));
            games.add(g);
            gs.saveGame(g);
        }
        Set<Game> all = gs.getAll();
        assert (all.containsAll(games));
    }
    private Game getGame(String gid) {
        return new Game(
                gid,
                "Sapphie",
                new HashSet<String>(),
                "The land of the Sapphie",
                0,5,
                Game.Difficulty.CHILL,
                "Sapphtopia",
                Game.OneshotOrCampaign.ONESHOT,
                Optional.<Integer>of(-73),
                Optional.<Integer>of(Integer.MAX_VALUE),
                "bepsi is gud"
        );
    }
}
