package ch.epfl.sweng.erpa.services.dummy.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {GameEntity.class},version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class GameDB extends RoomDatabase
{
    public abstract DummyDao gameDao();
}
