package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.io.IOException;
import java.util.Set;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.GCP.ServerException;

public interface UserManagementService {
    String PROP_INTENT_USER = "user_uuid";
    String UUID_PREFIX = "user|";

    String getUuidForUsername(String username) throws IOException, ServerException;

    Optional<Username> getUsernameFromUserUuid(String userUuid) throws IOException, ServerException;

    String registerNewUsername(String username) throws IOException, ServerException;

    Void registerAuth(UserAuth auth) throws IOException, ServerException;

    Optional<UserProfile> getUserProfile(String userUuid) throws IOException, ServerException;

    UserProfile saveUserProfile(UserProfile up) throws IOException, ServerException;

    UserProfile registerUserProfile(UserProfile up) throws IOException, ServerException;
}
