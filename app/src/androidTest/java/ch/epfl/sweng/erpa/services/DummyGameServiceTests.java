package ch.epfl.sweng.erpa.services;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;

@RunWith(AndroidJUnit4.class)
public class DummyGameServiceTests
{
    DummyGameService gs;
    @Before
    public void initGS()
    {
        Context ctx = InstrumentationRegistry.getTargetContext();
        gs = new DummyGameService(ctx);
    }
    @Test
    public void addedGamePersists()
    {
        Game g = getGame("addedGame");
        gs.saveGame(g);
        Optional<Game> found = gs.getGame(g.getGid());
        assert(found.isPresent());
        Game foundGame = found.get();
        assert(g.equals(foundGame));
        

    }
    private Game getGame(String gid)
    {
        return new Game(
                gid,
                "Sapphie",
                "The land of the Sapphie",
                "Bepsi is gud",
                "Sapphtopia",
                "E X T R E M E",
                "Campaign",
                -73,
                Integer.MAX_VALUE
        );
    }
}
