package ch.epfl.sweng.erpa.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import java.util.List;

import ch.epfl.sweng.erpa.database.converter.*;
import ch.epfl.sweng.erpa.model.*;

@Database(entities = {Game.class,
        PlayerJoinGameRequest.class,
        UserAuth.class,
        Username.class,
        UserProfile.class,
        UserSessionToken.class},
        version = 1)
@TypeConverters({Converters.class})
public abstract class ERPADatabase extends RoomDatabase {
    public abstract GameDao gameDao();
    public abstract PlayerJoinGameRequestDao playerJoinGameRequestDao();
    public abstract UserSessionTokenDao userSessionTokenDao();
    public abstract UserProfileDao userProfileDao();
    public abstract UsernameDao usernameDao();
    public abstract UserAuthDao userAuthDao();

    @Dao
    public static interface PlayerJoinGameRequestDao {
        @Query("select * from player_join_game_request where id like :joinRequestId")
        PlayerJoinGameRequest getPlayerJoinGameRequest(String joinRequestId);
    }

    @Dao
    public interface UserAuthDao {
        @Query("SELECT * FROM user_auth WHERE public_key LIKE :publicKey")
        UserAuth getUserAuth(String publicKey);
    }

    @Dao
    public interface UsernameDao {
        @Query("SELECT * FROM username WHERE username LIKE :username")
        Username getUserName(String username);
    }

    @Dao
    public interface UserProfileDao {
        @Query("SELECT * FROM user_profile where uuid LIKE :uuid")
        UserProfile getUserProfile(String uuid);
    }

    @Dao
    public interface UserSessionTokenDao {
        @Query("select * from user_session_token where session_token like :sessionToken")
        UserSessionToken getUserSessionToken(String sessionToken);
    }

    @Dao
    public interface GameDao {
        @Query("SELECT * FROM game") List<Game> getAll();
        @Insert void insertAll(Game... games);
    }
}
