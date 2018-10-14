package ch.epfl.sweng.erpa.services;

import javax.inject.Inject;
import javax.inject.Provider;

import ch.epfl.sweng.erpa.model.UserProfile;

public class UserProfileProvider implements Provider<UserProfile> {

    @Inject UserProfileProvider(){

    }

    @Override public UserProfile get() {
        return new UserProfile(true, true);
    }
}
