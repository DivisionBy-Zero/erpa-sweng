package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;
import com.annimon.stream.Optional;

public class DummyGameService extends  DummyDataService<Game> implements GameService {

    private final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";

    public DummyGameService(Context ctx) {
        super(ctx, Game.class);
    }


    @Override String dataFolder() {
        return SAVED_GAME_DATA_FOLDER;
    }

    @Override
    public Optional<Game> getGame(String gameUuid) {
        return getOne(gameUuid);
    }

    @Override
    public void saveGame(Game g) {
        saveOne(g);
    }

    @Override
    public Set<Game> getAllGames() {
        return getAll();
    }
}
