package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Collectors;
import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;

import com.annimon.stream.Optional;

@Singleton
public class DummyGameService extends DummyDataService<Game> implements GameService {

    @Override
    public boolean removeGames() {
        return removeAll();
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
