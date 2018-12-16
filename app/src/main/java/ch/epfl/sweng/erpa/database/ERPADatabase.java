package ch.epfl.sweng.erpa.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import ch.epfl.sweng.erpa.database.converter.*;
import ch.epfl.sweng.erpa.model.*;

@Database(entities = {Game.class, PlayerJoinGameRequest.class, UserAuth.class, Username.class, UserProfile.class, UserSessionToken.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class ERPADatabase extends RoomDatabase {
    public abstract GameDao gameDao();
}
