package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;

import ch.epfl.sweng.erpa.model.UuidObject;
import ch.epfl.sweng.erpa.services.DataService;
import lombok.Getter;

import static android.content.ContentValues.TAG;

public abstract class DummyDataService<T extends UuidObject> implements DataService<T> {
    static final String SAVED_DATA_FILE_EXTENSION = ".yaml";
    @Getter private final Function<File, T> fileFetcher;
    @Getter private final File dataDir;

    DummyDataService(Context ctx, Class<T> cls) {
        this.dataDir = new File(ctx.getFilesDir(), dataFolder());
        this.fileFetcher = file -> fetchExistingDataFromFile(file, cls);
        if (!dataDir.mkdir() && !dataDir.isDirectory()) {
            throw new IllegalStateException(
                String.format("%s data folder (%s) exists and is not a folder!",
                    cls.getSimpleName(), dataDir.getAbsolutePath()));
        }
    }

    private static <U> U fetchExistingDataFromFile(File dataFile, Class<U> uClass) {
        FileReader fileReader = Exceptional.of(() -> new FileReader(dataFile))
            .ifExceptionIs(FileNotFoundException.class,
                (FileNotFoundException e) -> {
                    throw new IllegalArgumentException("File given as argument does not exist!");
                })
            .ifExceptionIs(IOException.class,
                (IOException e) -> {
                    Log.e("FetchData", Arrays.toString(e.getStackTrace()));
                    throw new RuntimeException(e);
                })
            .getOrThrowRuntimeException();
        return new Yaml().loadAs(fileReader, uClass);
    }

    abstract String dataFolder();

    @Override
    public void removeAll() {
        boolean res = true;
        for (File f : dataDir.listFiles()) {
            res = f.delete() && res;
        }
        if (!res) throw new RuntimeException("Unable to remove all save files!");
    }

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
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(gameFile, false))) {
                new Yaml().dump(t, writer);
            }
        } catch (FileNotFoundException exc) {
            throw new RuntimeException("Could not create backing file");
        } catch (IOException e) {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
            throw new RuntimeException("Could not save file");
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
