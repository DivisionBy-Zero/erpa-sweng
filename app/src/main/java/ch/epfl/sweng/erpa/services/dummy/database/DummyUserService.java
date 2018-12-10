package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;

@Singleton
public class DummyUserService extends DummyDataService<UserProfile> implements UserManagementService {
    private static final String USER_PROFILE_DATA_FOLDER = "user_profiles_data";
    BiMap<String, String> userUuidToUsernames = HashBiMap.create();

    @Inject
    public DummyUserService(Context ctx) {
        super(ctx, UserProfile.class);
    }

    @Override
    String dataFolder() {
        return USER_PROFILE_DATA_FOLDER;
    }

    @Override
    public Optional<UserProfile> getUserProfile(String userUuid) {
        return getOne(userUuid);
    }

    @Override
    public UserProfile saveUserProfile(UserProfile up) {
        saveOne(up);
        return up;
    }

    @Override
    public UserProfile registerUserProfile(UserProfile up) throws IOException, ServerException {
        if (!userUuidToUsernames.containsKey(up.getUuid()))
            throw new ServerException(404, "UserUuid Unknown");
        return saveUserProfile(up);
    }

    public void updateUserProfile(UserProfile up) {
        saveUserProfile(up);
    }

    @Override
    public String getUuidForUsername(String username) {
        return userUuidToUsernames.inverse().get(username);
    }

    @Override
    public Optional<Username> getUsernameFromUserUuid(String userUuid) {
        return Optional.ofNullable(userUuidToUsernames.get(userUuid))
            .map(username -> new Username(userUuid, username));
    }

    @Override
    public String registerNewUsername(String username) throws IOException, ServerException {
        String userUuid = UserManagementService.UUID_PREFIX + UUID.randomUUID();
        userUuidToUsernames.put(userUuid, username);
        return userUuid;
    }

    @Override public Void registerAuth(UserAuth auth) throws IOException, ServerException {
        return null;
    }
}
