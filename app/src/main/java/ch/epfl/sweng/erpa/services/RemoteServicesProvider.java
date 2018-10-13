package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

public interface RemoteServicesProvider {
    // Here be data proxy

    // Here be metadata
    String getFriendlyProviderName();

    String getFriendlyProviderDescription();
    Optional<String> getUidFromUsername(String username);
    boolean verifyAccessToken(String uid, String accessToken);

    // Here be LifeCycle management
    default void terminate() {
    }
}
