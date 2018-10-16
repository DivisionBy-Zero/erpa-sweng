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
    @Query("SELECT * FROM gameentity")
    List<GameEntity> getAll();

    @Query("SELECT * FROM gameentity WHERE uid IS (:gid)")
    GameEntity getGame(String gid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(GameEntity g);


    default void insert(Game g)
    {
        GameEntity g0=new GameEntity(g);
        g0.setGame(g);
        g0.setUid(g.getGid());
        insert(g0);
    }
}
