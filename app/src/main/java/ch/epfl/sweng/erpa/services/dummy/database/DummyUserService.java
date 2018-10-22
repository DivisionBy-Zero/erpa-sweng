package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.UserProfile;

public class DummyUserService extends DummyDataService<UserProfile>{

    private static final String USER_PROFILE_DATA_FOLDER = "user_profiles_data";

    public DummyUserService(Context ctx) {
        super(ctx, UserProfile.class);
    }

    @Override String dataFolder() {
        return USER_PROFILE_DATA_FOLDER;
    }

    @Override
    public Optional<UserProfile> getUserProfile(String userUuid) {
        return getOne(userUuid);
    }

    @Override
    public void saveUserProfile(UserProfile up) {
        saveOne(up);
    }
}
