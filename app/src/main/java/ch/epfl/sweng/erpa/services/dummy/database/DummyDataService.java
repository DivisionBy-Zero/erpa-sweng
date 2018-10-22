package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;

import ch.epfl.sweng.erpa.model.UuidObject;
import ch.epfl.sweng.erpa.services.DataService;

import static android.content.ContentValues.TAG;

public abstract class DummyDataService<T extends UuidObject> implements DataService<T> {
    private final static String SAVED_DATA_FILE_EXTENSION = ".yaml";
    private final Function<File, T> fileFetcher;

    private final File dataDir;

    DummyDataService(Context ctx, Class<T> tClass) {
        this.dataDir = new File(ctx.getFilesDir(), dataFolder());
        this.fileFetcher = file -> fetchExistingDataFromFile(file, tClass);
        if (!dataDir.mkdir()) {
            if (!dataDir.isDirectory()) {
                throw new IllegalStateException(tClass.getName() + " data folder (\"" + dataDir.getAbsolutePath() + "\") exists and is not a folder!");
            }
        }
    }

    abstract String dataFolder();

    @Override
    public Optional<T> getOne(String gid) {
        File gameFile = new File(dataDir, gid + SAVED_DATA_FILE_EXTENSION);
        if (gameFile.isDirectory()) {
            throw new IllegalStateException("Folder " + gameFile.getAbsolutePath() + " exists!");
        } else {
            return Optional.of(gameFile)
                    .filter(File::exists)
                    .filter(File::isFile)
                    .map(fileFetcher);
        }
    }

    @Override
    public void saveOne(T t) {
        String gid = t.getUuid();
        try {
            File gameFile = new File(dataDir, gid + SAVED_DATA_FILE_EXTENSION);
            if (!gameFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                gameFile.createNewFile();
            } else if (gameFile.isDirectory()) {
                throw new IllegalStateException("Trying to write to existing folder, as file! " + gameFile.getAbsolutePath());
            }
            FileOutputStream fOut = new FileOutputStream(gameFile, false);
            OutputStreamWriter writer = new OutputStreamWriter(fOut);

            new Yaml().dump(t, writer);
        } catch (FileNotFoundException ignored) { //we just created the file. it cannot possibly not exist (unless createNewFile threw an error)
        } catch (Exception e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("Could not save file");
        }
    }

    private static <U> U fetchExistingDataFromFile(File dataFile, Class<U> uClass) {
        try {
            FileReader gReader = new FileReader(dataFile);
            return (new Yaml()).loadAs(gReader, uClass);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File given as argument does not exist");
        }
    }

    @Override
    public Set<T> getAll() {
        File[] games = dataDir.listFiles();
        return Stream.of(games)
                .filter(file -> file.getPath().endsWith(SAVED_DATA_FILE_EXTENSION))
                .filter(File::isFile)
                .map(fileFetcher)
                .collect(Collectors.toSet());
    }
}
