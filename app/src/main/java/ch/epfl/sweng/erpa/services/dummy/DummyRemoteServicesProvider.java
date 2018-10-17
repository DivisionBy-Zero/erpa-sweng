package ch.epfl.sweng.erpa.services.dummy;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

import static ch.epfl.sweng.erpa.utils.ActivityUtils.createAccessToken;


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

    @Override
    public Optional<String> getUidFromUsername(String username) {
        // The uid returned is a random uid generate with javaZ.util.UUID.randomUUID()
        // This will work for all users but it's for testing purposes because user sign up and
        // database isn't set up yet
        return Optional.of("user|5b915f75-0ff0-43f8-90bf-f9e92533f926");
    }

    @Override
    public boolean verifyAccessToken(String uid, String accessToken) {
        // I left the password in plain here for testing with one single user
        return accessToken.equals(createAccessToken(uid, "admin"));
    }
}
