package ch.epfl.sweng.erpa.services.firebase;

import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

public class FirebaseRemoteServicesProvider implements RemoteServicesProvider {
    public FirebaseRemoteServicesProvider() { }

    @Override
    public String getFriendlyProviderName() {
        return "Google Firebase";
    }

    @Override
    public String getFriendlyProviderDescription() {
        return "Your phone will connect directly to Firebase, Google Play Services is required for this to work";
    }
}
