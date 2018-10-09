package ch.epfl.sweng.erpa.services.dummy;

import android.app.Application;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import at.favre.lib.crypto.bcrypt.BCrypt;

import ch.epfl.sweng.erpa.model.UserProfile;
import java.util.List;

import javax.inject.Inject;

import at.favre.lib.crypto.bcrypt.BCrypt;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.GameService;

public class DummyRemoteServicesProvider implements RemoteServicesProvider {

    private ArrayList<UserProfile> userList;

    public DummyRemoteServicesProvider() {
        UserProfile defaultUser = new UserProfile("user|5b915f75-0ff0-43f8-90bf-f9e92533f926", "admin", createAccessToken("user|5b915f75-0ff0-43f8-90bf-f9e92533f926", "admin"), UserProfile.Experience.Casual, false, true);
        userList = new ArrayList<>();
        userList.add(defaultUser);
	}

    @Override
    public String getFriendlyProviderName() {
        return "Dummy Remote Provider";
    }

    @Override
    public String getFriendlyProviderDescription() {
        return "This is a dummy storage provider. No information will be sent or received and everything will be stored locally in the application database.";
    }

    @Override
    public Optional<String> getUidFromUsername(String username) {
        Optional<UserProfile> u = getUserFromUsername(username);
        return u.map(UserProfile::getUid);
    }

    @Override
    public boolean verifyAccessToken(String uid, String accessToken) {
        Optional<UserProfile> u = getUserFromUid(uid);
        if (u.isPresent())
            return accessToken.equals(u.get().getAccessToken());
        else
            return false;
    }

    @Override
    public void storeNewUser(UserProfile user) {
        userList.add(user);
    }

    private Optional<UserProfile> getUserFromUsername(String username) {
        for (UserProfile u: userList) {
            if (u.getUsername().equals(username))
                return Optional.of(u);
        }
        return Optional.empty();
    }

    private Optional<UserProfile> getUserFromUid(String uid) {
        for (UserProfile u: userList) {
            if (u.getUid().equals(uid))
                return Optional.of(u);
        }
        return Optional.empty();
    }

    @Override public void terminate() {
    }

    // This function is temporary and will be removed it is just here so I can test everything
    private String createAccessToken(String uid, String password) {
        byte[] uidBytes = uid.getBytes(StandardCharsets.UTF_8);
        int uidBytesLength = uidBytes.length;
        byte[] salt16Bytes = new byte[16];
        System.arraycopy(uidBytes, uidBytesLength - 16, salt16Bytes, 0, 16);
        byte[] hashBytes = BCrypt.withDefaults().hash(6, salt16Bytes, password.getBytes(StandardCharsets.UTF_8));
        return new String(hashBytes, StandardCharsets.UTF_8);
    }


    private GameService gs = new DummyGameService();

    @Override
    public GameService getGameService()
    {
        return gs;
    }

    private static class DummyGameService implements GameService
    {
        @Entity
        class GameEntity
        {
            @PrimaryKey
            private String uid;

            @ColumnInfo(name = "game", typeAffinity = ColumnInfo.BLOB)
            private String game;

            public String getGame() {
                return game;
            }

            public void setGame(String game) {
                this.game = game;
            }

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }
        }

        @Database(entities = {GameEntity.class},version = 1)
        @TypeConverters({Converters.class})
        public abstract class GameDB extends RoomDatabase
        {
            public abstract GameDao gameDao();
        }
        static class Converters
        {
            @TypeConverter
            public static Game fromString(String val)
            {
                Type gameType = new TypeToken<Game>(){}.getType();
                Gson gson = new Gson();
                return gson.fromJson(val, gameType);
            }

            @TypeConverter
            public static String fromGame(Game g)
            {
                return (new Gson()).toJson(g);
            }
        }

        @Dao
        public interface GameDao
        {
            @Query("SELECT * FROM gameentity")
            List<GameEntity> getAll();

            @Query("SELECT * FROM gameentity WHERE uid IS (:gid)")
            GameEntity getGame(String gid);

            @Insert(onConflict = OnConflictStrategy.REPLACE)
            public void insert(GameEntity g);
        }



        @Override
        public Optional<Game> getGame(String uid)
        {
            return Optional.empty();
        }

        @Override
        public void saveGame(Game g)
        {

        }

        @Override
        public List<Game> listGames()
        {
            return null;
        }

    }



}
