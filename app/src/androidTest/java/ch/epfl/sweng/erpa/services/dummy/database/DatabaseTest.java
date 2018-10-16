package ch.epfl.sweng.erpa.services.dummy.database;

import android.arch.persistence.room.Room;
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

@RunWith(AndroidJUnit4.class)
public class DatabaseTest
{
    private DummyDao dummyDao;
    private DummyDatabase dummyDatabase;

    @Before
    public void createDb()
    {
        Context  ctx = InstrumentationRegistry.getTargetContext();
        dummyDatabase = Room.inMemoryDatabaseBuilder(ctx, DummyDatabase.class).build();
        dummyDao = dummyDatabase.getGameDao();
    }


    @After
    public void closeDb()
    {
        dummyDatabase.close();
    }


    @Test
    public void testAddThenRead()
    {
        final int numGames = 500;
        List<Game> games = new ArrayList<>(numGames);
        for(int i = 0; i<numGames; i++)
        {
            Game g = new Game("sapphie",
                    String.valueOf(i) + "",
                    0,
                    0,
                    Game.Difficulty.CHILL,
                    "Hello Kitty",
                    Game.OneshotOrCampaign.ONESHOT,
                    Optional.<Integer>empty(),
                    Optional.<Integer>empty(),
                    "",
                    "");
            dummyDao.insert(g);
            games.add(g);
        }
        for(int i = 0; i<numGames;i++)
        {
            GameEntity g = dummyDao.getGame(""+i);
            assert(g!=null);
            assert(g.getGame().equals(games.get(i)));
        }
        assert(dummyDao.getAll().size()==games.size());
    }
}
