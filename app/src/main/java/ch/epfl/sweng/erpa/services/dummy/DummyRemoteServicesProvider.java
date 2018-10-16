package ch.epfl.sweng.erpa.services.dummy;

import com.annimon.stream.Optional;

import java.nio.charset.StandardCharsets;

import at.favre.lib.crypto.bcrypt.BCrypt;

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

    // This function is temporary and will be removed it is just here so I can test everything
    private String createAccessToken(String uid, String password) {
        byte[] uidBytes = uid.getBytes(StandardCharsets.UTF_8);
        int uidBytesLength = uidBytes.length;
        byte[] salt16Bytes = new byte[16];
        for (int i = 0; i<16; ++i)
            salt16Bytes[i] = uidBytes[uidBytesLength - 16 + i];
        byte[] hashBytes = BCrypt.withDefaults().hash(6, salt16Bytes, password.getBytes(StandardCharsets.UTF_8));
        String str = new String(hashBytes, StandardCharsets.UTF_8);
        return str;
    }
}
