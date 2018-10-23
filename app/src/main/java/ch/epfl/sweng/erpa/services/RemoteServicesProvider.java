package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.operations.annotations.Service;

public interface RemoteServicesProvider {
    // Here be data proxies
    @Service GameService getGameService();
    @Service UserAuthProvider getUserAuthProvider();

    // Here be metadata
    String getFriendlyProviderName();

    String getFriendlyProviderDescription();

    // Here be LifeCycle management
    void terminate();
}
