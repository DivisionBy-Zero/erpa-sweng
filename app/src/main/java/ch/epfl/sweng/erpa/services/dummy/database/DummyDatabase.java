package ch.epfl.sweng.erpa.services.dummy.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import ch.epfl.sweng.erpa.model.Game;

@Database(entities = {Game.class},version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class DummyDatabase extends RoomDatabase
{
    public abstract DummyDao getGameDao();
}
