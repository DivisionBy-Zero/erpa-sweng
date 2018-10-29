package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Optional;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;

@Singleton
public class DummyGameService extends DummyDataService<Game> implements GameService {

    @Override
    public void removeGames() {
        removeAll();
    }

    final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";

    @Inject
    public DummyGameService(Context ctx) {
        super(ctx, Game.class);
    }


    @Override
    String dataFolder() {
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
