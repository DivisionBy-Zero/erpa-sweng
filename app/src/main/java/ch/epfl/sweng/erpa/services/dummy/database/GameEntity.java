package ch.epfl.sweng.erpa.services.dummy.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import ch.epfl.sweng.erpa.model.Game;

@Entity
class GameEntity
{
    @PrimaryKey
    @NonNull
    private String uid;

    @ColumnInfo(name = "game")
    private Game game;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
