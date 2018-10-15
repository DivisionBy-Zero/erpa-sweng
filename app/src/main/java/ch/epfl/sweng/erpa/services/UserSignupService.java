package ch.epfl.sweng.erpa.services;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;

import com.annimon.stream.Optional;

public class UserSignupService {

    private RemoteServicesProvider rsp;

    @Inject
    public UserSignupService() {
        rsp = new DummyRemoteServicesProvider();
    }

    public Optional<UserProfile> storeNewUser(String username, String password, String level, boolean isGm, boolean isPlayer) {
        Optional<String> checkUid = rsp.getUidFromUsername(username);
        if (checkUid.isPresent()) {
            return Optional.empty();
        }
        String uid = "user|" + UUID.randomUUID().toString();
        UserProfile.Experience xp = level.equals("Noob") ? UserProfile.Experience.Noob :
                                    level.equals("Casual") ? UserProfile.Experience.Casual :
                                            UserProfile.Experience.Expert;
        UserProfile user = new UserProfile(uid, username, createAccessToken(uid, password), xp, isGm, isPlayer);
        rsp.storeNewUser(user);
        return Optional.of(user);
    }

    public static String createAccessToken(String uid, String password) {
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
