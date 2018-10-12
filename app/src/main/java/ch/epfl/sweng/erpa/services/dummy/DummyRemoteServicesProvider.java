package ch.epfl.sweng.erpa.services.dummy;

import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

public class DummyRemoteServicesProvider implements RemoteServicesProvider {
    public DummyRemoteServicesProvider() {
    }

    @Override
    public String getFriendlyProviderName() {
        return "Dummy Remote Provider";
    }

    @Override
    public String getFriendlyProviderDescription() {
        return "This is a dummy storage provider. No information will be sent or received and everything will be stored locally in the application database.";
    }
}
