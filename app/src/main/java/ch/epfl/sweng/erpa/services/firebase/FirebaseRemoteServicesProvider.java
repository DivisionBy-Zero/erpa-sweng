package ch.epfl.sweng.erpa.services.firebase;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.UserAuthProvider;

public class FirebaseRemoteServicesProvider implements RemoteServicesProvider {
    public FirebaseRemoteServicesProvider() {
    }

    @Override public UserAuthProvider getUserAuthProvider() {
        return null;
    }

    @Override
    public String getFriendlyProviderName() {
        return "Google Firebase";
    }

    @Override
    public String getFriendlyProviderDescription() {
        return "Your phone will connect directly to Firebase, Google Play Services is required for this to work";
    }

    @Override
    public Optional<String> getUidFromUsername(String username) {
        return null;
    }

    @Override
    public boolean verifyAccessToken(String uid, String accessToken) {
        return false;
    }

    @Override
    public void storeNewUser(UserProfile user) {
        return;
    }

    @Override
    public GameService getGameService() {
        return null;
    }

    @Override 
    public void terminate() {

    }
}
