package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.UserProfile;

public interface UserProfileService {
    String PROP_INTENT_USER_UUID = "user|";

    Optional<UserProfile> getUserProfile(String userUuid);
    void saveUserProfile(UserProfile up);
    Set<UserProfile> getAllUserProfiles();
}
