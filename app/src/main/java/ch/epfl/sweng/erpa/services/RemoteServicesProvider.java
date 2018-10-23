package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.operations.annotations.Service;

public interface RemoteServicesProvider {
    // Here be data proxies
    @Service GameService getGameService();

    // Here be metadata
    String getFriendlyProviderName();

    String getFriendlyProviderDescription();

    Optional<String> getUidFromUsername(String username);

    boolean verifyAccessToken(String uid, String accessToken);

    void storeNewUser(UserProfile user);

    // Here be LifeCycle management
    void terminate();
}
