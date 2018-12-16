package ch.epfl.sweng.erpa.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import ch.epfl.sweng.erpa.model.*;

@Dao
public interface GameDao {
    @Query("SELECT * FROM game") List<Game> getAll();
    @Insert void insertAll(Game... games);
}
