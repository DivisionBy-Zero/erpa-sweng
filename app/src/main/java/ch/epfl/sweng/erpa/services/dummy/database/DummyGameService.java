package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Optional;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;

import static android.content.ContentValues.TAG;

public class DummyGameService extends  DummyDataService<Game> implements GameService {

    private final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";

    public DummyGameService(Context ctx) {
        super(ctx, Game.class);
    }


    @Override String dataFolder() {
        return SAVED_GAME_DATA_FOLDER;
    }

    @Override public Optional<Game> getGame(String gameId) {
        return getOne(gameId);
    }

    @Override public void saveGame(Game g) {
        saveOne(g);
    }

    @Override public Set<Game> getAllGames() {
        return getAll();
    }

}
