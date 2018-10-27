package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Exceptional;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;
import lombok.Getter;

import static android.content.ContentValues.TAG;

@Singleton
public class DummyGameService implements GameService {
    public final static String SAVED_GAME_FILE_EXTENSION = ".yaml";
    public final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";
    @Getter
    private final File gameDir;

    @Inject
    public DummyGameService(Context ctx) {
        File rootDir = ctx.getFilesDir();
        File gameDir = new File(rootDir, SAVED_GAME_DATA_FOLDER);
        this.gameDir = gameDir;

        // If the directory wasn't created, and no error was thrown, then it means the file/dir already existed
        if (!gameDir.mkdir()) {
            // If it wasn't a directory, abort
            if (!gameDir.isDirectory()) {
                throw new IllegalStateException("Game data folder (\"" + gameDir.getAbsolutePath() + "\") exists and is not a folder!");
            }
        }
    }

    /**
     * Fetches files that is guaranteed to exist
     *
     * @param gameFile the file (is assumed to exist)
     * @return the game contained in the file
     */
    public static Game fetchExistingGameFromFile(File gameFile) {
        FileReader fr = Exceptional.of(() -> new FileReader(gameFile))
                .getOrThrow(new IllegalArgumentException("Game did not exist!"));
        return new Yaml().load(fr);
    }

    @Override
    public Optional<Game> getGame(String gid) {
        return Optional.of(
                new File(gameDir, gid + SAVED_GAME_FILE_EXTENSION))
                .filter(File::exists)
                .filter(File::isFile)
                .map(DummyGameService::fetchExistingGameFromFile);
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
        String gid = g.getGameUuid();
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

            new Yaml().dump(g, writer);
        } catch (FileNotFoundException ignored) {
            // We just created the file. it cannot possibly not exist (unless createNewFile threw an error)
        } catch (IOException e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("Could not save file");
        }
    }

    @Override
    public boolean removeGames() {
        File[] files = gameDir.listFiles();
        boolean res = true;
        for (File f : files)
            res &= f.delete();
        return res;
    }
}
