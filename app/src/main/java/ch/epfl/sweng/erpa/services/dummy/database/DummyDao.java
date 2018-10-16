package ch.epfl.sweng.erpa.services.dummy.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ch.epfl.sweng.erpa.model.Game;

@Dao
public interface DummyDao
{
    @Query("SELECT * FROM game")
    List<Game> getAll();

    @Query("SELECT * FROM game WHERE gid IS (:gid)")
    Game getGame(String gid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(Game g);

}
