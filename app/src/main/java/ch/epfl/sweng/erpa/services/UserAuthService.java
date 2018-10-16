package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;

public class UserAuthService {

    private RemoteServicesProvider rsp;

    @Inject public UserAuthService() {
        rsp = new DummyRemoteServicesProvider();
    }

    public Optional<UserAuth> getUserAuth(String username, String password) {
        Optional<String> uid = rsp.getUidFromUsername(username);
        if (uid.isPresent()) {
            String id = uid.get();
            String accessToken = createAccessToken(id, password);
            if (rsp.verifyAccessToken(id, accessToken)) {
                return Optional.of(new UserAuth(id, accessToken));
            }
        }
        return Optional.empty();
    }

    private String createAccessToken(String uid, String password) {
        byte[] uidBytes = uid.getBytes(StandardCharsets.UTF_8);
        int uidBytesLength = uidBytes.length;
        byte[] salt16Bytes = new byte[16];
        for (int i = 0; i < 16; ++i)
            salt16Bytes[i] = uidBytes[uidBytesLength - 16 + i];
        byte[] hashBytes = BCrypt.withDefaults().hash(6, salt16Bytes, password.getBytes(StandardCharsets.UTF_8));
        String str = new String(hashBytes, StandardCharsets.UTF_8);
        return str;
    }
}
