package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.UserProfile;

public interface UserProfileService {
    String PROP_INTENT_USER = "user_uuid";
    String UUID_PREFIX = "user|";

    Optional<UserProfile> getUserProfile(String userUuid);
    void saveUserProfile(UserProfile up);
    Set<UserProfile> getAllUserProfiles();
}
