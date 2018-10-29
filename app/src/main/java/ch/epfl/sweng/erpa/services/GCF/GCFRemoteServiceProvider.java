package ch.epfl.sweng.erpa.services.GCF;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.UserProfileService;

public class GCFRemoteServiceProvider implements RemoteServicesProvider {
    // TODO (@Sapphie)
    @Override public GameService getGameService() {
        return null;
    }

    @Override public UserProfileService getUserProfileService() {
        return null;
    }

    @Override public String getFriendlyProviderName() {
        return null;
    }

    @Override public String getFriendlyProviderDescription() {
        return null;
    }

    @Override public Optional<String> getUidFromUsername(String username) {
        return null;
    }

    @Override public boolean verifyAccessToken(String uid, String accessToken) {
        return false;
    }

    @Override public void storeNewUser(UserProfile user) {

    }

    @Override public void terminate() {

    }
}
