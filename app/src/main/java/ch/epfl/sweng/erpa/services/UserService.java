package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.UserProfile;

public interface UserService {

    Set<UserProfile> getAllUsers();
    void saveUser(UserProfile userProfile);
    Optional<UserProfile> getUser(String uid);
}
