package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;

import static android.content.ContentValues.TAG;

public class DummyGameService implements ch.epfl.sweng.erpa.services.GameService {

    private final static String SAVED_GAME_FILE_EXTENSION = ".yaml";
    private final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";
    private final File gameDir;

    public DummyGameService(Context ctx) {
        File rootDir = ctx.getFilesDir();
        File gameDir = new File(rootDir, SAVED_GAME_DATA_FOLDER);
        this.gameDir = gameDir;
        //if the directory wasn't created, and no error was thrown, then it means the file/dir already existed
        if (!gameDir.mkdir()) {
            //if it wasn't a directory, abort
            if (!gameDir.isDirectory()) {
                throw new IllegalStateException("Game data folder (\"" + gameDir.getAbsolutePath() + "\") exists and is not a folder!");
            }
        }

    }

    @Override
    public Optional<Game> getGame(String gid) {
        File gameFile = new File(gameDir, gid + SAVED_GAME_FILE_EXTENSION);
        if (gameFile.isDirectory()) {
            throw new IllegalStateException("Folder " + gameFile.getAbsolutePath() + " exists!");
        } else {
            return Optional.of(gameFile)
                    .filter(File::exists)
                    .filter(File::isFile)
                    .map(DummyGameService::fetchExistingGameFromFile);
        }
    }

    //Note that this method does not check if the
    private static Game fetchExistingGameFromFile(File gameFile) {
        try {
            FileReader gReader = new FileReader(gameFile);
            return (new Yaml()).loadAs(gReader, Game.class);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File given as argument does not exist");
        }
    }

    @Override
    public Set<Game> getAll() {
        File[] games = gameDir.listFiles();
        return Stream.of(games)
                .filter(file -> file.getPath().endsWith(SAVED_GAME_FILE_EXTENSION))
                .filter(File::isFile)
                .map(DummyGameService::fetchExistingGameFromFile)
                .collect(Collectors.toSet());


    }


    @Override
    public void saveGame(Game g) {
        String gid = g.getGid();
        try {
            File gameFile = new File(gameDir, gid + SAVED_GAME_FILE_EXTENSION);
            if (!gameFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                gameFile.createNewFile();
            } else if (gameFile.isDirectory()) {
                throw new IllegalStateException("Trying to write to existing folder, as file! " + gameFile.getAbsolutePath());
            }
            FileOutputStream fOut = new FileOutputStream(gameFile, false);
            OutputStreamWriter writer = new OutputStreamWriter(fOut);

            (new Yaml()).dump(g, writer);
        } catch (FileNotFoundException ignored) { //we just created the file. it cannot possibly not exist (unless createNewFile threw an error)
        } catch (Exception e)
        {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("Could not save file");
        }
    }
}
