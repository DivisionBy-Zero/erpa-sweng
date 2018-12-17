package ch.epfl.sweng.erpa.services.dummy.database;

import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.activities.DependencyConfigurationAgnosticTest;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;

import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTest extends DependencyConfigurationAgnosticTest {
    @Inject private DummyGameService underTest;

    @After
    public void cleanUp() {
        underTest.removeGames();
    }

    @Test
    public void testAdded() {
        Game g = getGame("testAdded");
        Optional<Game> res;
        underTest.updateGame(g);
        res = underTest.getGame(g.getUuid());
        assertTrue(res.isPresent());
        assertEquals(g, res.get());
    }

    @Test
    public void testAllAdded() {
        List<Game> games = new ArrayList<>();
        for (int i = 0; i < 100; ++i) {
            Game up = getGame("Game " + Integer.toString(i));
            games.add(underTest.createGame(up));
        }

        for (Game game : games)
            assertTrue(underTest.getGame(game.getUuid()).isPresent());

        assertTrue(underTest.getAllGames(new GameService.StreamRefiner()).containsAll(games));
    }
}
