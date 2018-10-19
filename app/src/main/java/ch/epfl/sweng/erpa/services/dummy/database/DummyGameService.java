package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Optional;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.erpa.model.Game;

public class DummyGameService implements ch.epfl.sweng.erpa.services.GameService
{

    private final static String EXTENSION = ".yaml";
    private final static String GAME_DATA_FOLDER = "game_data";
    private final File gameDir;

    public DummyGameService(Context ctx)
    {
        File rootDir = ctx.getFilesDir();
        File gameDir = new File(rootDir, GAME_DATA_FOLDER);
        this.gameDir = gameDir;
        //if the directory wasn't created, and no error was thrown, then it means the file/dir already existed
        if(!gameDir.mkdir())
        {
            //if it wasn't a directory, abort
            if(!gameDir.isDirectory())
            {
                throw new IllegalStateException("Game data folder (\"" + gameDir.getAbsolutePath() + "\") exists and is not a folder!");
            }
        }

    }

    @Override
    public Optional<Game> getGame(String gid)
    {
        File game = new File(gameDir, gid+EXTENSION);
        if(game.exists() && game.isFile())
        {
            return Optional.of(fetchGameExisting(game));
        }
        if(!game.isFile())
        {
            throw new IllegalStateException("Folder " + game.getAbsolutePath() + " exists!");
        }
        else
        {
            return Optional.empty();
        }
    }
    private static Game fetchGameExisting(File gameFile)
    {
        try
        {
            FileReader gReader = new FileReader(gameFile);
            Yaml yaml = new Yaml();
            return yaml.loadAs(gReader,Game.class);

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Game> getAll()
    {
        List<Game> result = new ArrayList<>();
        File[] games = gameDir.listFiles();
        Yaml yaml = new Yaml();
        for(File f: games)
        {
            String filePath = f.getPath();

            if(f.isFile() && filePath.endsWith(EXTENSION))
            {
                result.add(fetchGameExisting(f));
            }
        }
        return result;
    }



    @Override
    public void saveGame(Game g)
    {
        String gid = g.getGid();
        try
        {
            File gameFile = new File(gameDir, gid + EXTENSION);
            if (!gameFile.exists())
            {
                gameFile.createNewFile();
            }
            else if(gameFile.isDirectory())
            {
                throw new IllegalStateException("Trying to write to existing folder, as file! " + gameFile.getAbsolutePath());
            }
            FileOutputStream fOut = new FileOutputStream(gameFile, false);
            OutputStreamWriter writer = new OutputStreamWriter(fOut);


            Yaml yaml = new Yaml();
            yaml.dump(g, writer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
