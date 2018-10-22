package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;

public class DummyGameService extends  DummyDataService<Game>{

    private final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";

    public DummyGameService(Context ctx) {
        super(ctx, Game.class);
    }


    @Override String dataFolder() {
        return SAVED_GAME_DATA_FOLDER;
    }

}
