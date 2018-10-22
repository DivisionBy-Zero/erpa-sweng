package ch.epfl.sweng.erpa.services;

import ch.epfl.sweng.erpa.model.UserProfile;

public interface UserService {
    void saveUser(UserProfile userProfile);
    UserProfile getUser(String uid);
}
