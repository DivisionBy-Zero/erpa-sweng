package ch.epfl.sweng.erpa.services.dummy.database;

import com.annimon.stream.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;

@Singleton
public class DummyUserService implements UserManagementService {
    BiMap<String, String> userUuidToUsernames = HashBiMap.create();
    Map<String, UserProfile> userProfiles = new HashMap<>();

    @Override
    public Optional<UserProfile> getUserProfile(String userUuid) {
        return Optional.ofNullable(userProfiles.get(userUuid));
    }

    @Override
    public UserProfile saveUserProfile(UserProfile up) throws ServerException {
        if (!userUuidToUsernames.containsKey(up.getUuid()))
            throw new ServerException(404, "UserUuid Unknown");
        userProfiles.put(up.getUuid(), up);
        return up;
    }

    @Override
    public UserProfile registerUserProfile(UserProfile up) throws IOException, ServerException {
        return saveUserProfile(up);
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
