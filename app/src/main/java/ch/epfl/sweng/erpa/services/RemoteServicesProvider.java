package ch.epfl.sweng.erpa.services;

public interface RemoteServicesProvider {
    // Here be data proxy

    // Here be metadata
    String getFriendlyProviderName();
    String getFriendlyProviderDescription();

    // Here be LifeCycle management
    default void terminate(){}
}
