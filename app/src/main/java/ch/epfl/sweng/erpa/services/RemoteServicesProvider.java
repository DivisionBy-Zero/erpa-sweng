package ch.epfl.sweng.erpa.services;

import ch.epfl.sweng.erpa.operations.annotations.Service;

public interface RemoteServicesProvider {
    // Here be data proxies
    @Service GameService getGameService();
    @Service UserManagementService getUserProfileService();

    // Here be metadata
    String getFriendlyProviderName();
    String getFriendlyProviderDescription();

    // Here be LifeCycle management
    void terminate();
}
