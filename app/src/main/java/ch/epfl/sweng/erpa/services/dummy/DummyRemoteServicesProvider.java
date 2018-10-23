package ch.epfl.sweng.erpa.services.dummy;

import com.annimon.stream.Optional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.DataService;
<<<<<<< HEAD
import ch.epfl.sweng.erpa.services.GameService;
=======
>>>>>>> 2d044a6... Fix minor issues
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.UserProfileService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import lombok.Getter;
import ch.epfl.sweng.erpa.services.dummy.database.DummyUserService;


@Singleton
public class DummyRemoteServicesProvider implements RemoteServicesProvider {
    @Inject @Getter DummyGameService gameService;

    private ArrayList<UserProfile> userList;

    @Inject public DummyRemoteServicesProvider() {
        UserProfile defaultUser = new UserProfile("user|5b915f75-0ff0-43f8-90bf-f9e92533f926",
                "admin", createAccessToken("user|5b915f75-0ff0-43f8-90bf-f9e92533f926", "admin"),
                UserProfile.Experience.Casual, false, true);
        userList = new ArrayList<>();
        userList.add(defaultUser);
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
        Optional<UserProfile> u = getUserFromUsername(username);
        return u.map(UserProfile::getUuid);
    }

    @Override
    public boolean verifyAccessToken(String uid, String accessToken) {
        Optional<UserProfile> u = getUserFromUid(uid);
        if (u.isPresent())
            return accessToken.equals(u.get().getAccessToken());
        else
            return false;
    }

    @Override
    public void storeNewUser(UserProfile user) {
        userList.add(user);
    }

    private Optional<UserProfile> getUserFromUsername(String username) {
        for (UserProfile u : userList) {
            if (u.getUsername().equals(username))
                return Optional.of(u);
        }
        return Optional.empty();
    }

    private Optional<UserProfile> getUserFromUid(String uid) {
        for (UserProfile u : userList) {
            if (u.getUuid().equals(uid))
                return Optional.of(u);
        }
        return Optional.empty();
    }

    @Override
    public void terminate() {
    }

    // This function is temporary and will be removed it is just here so I can test everything
    private String createAccessToken(String uid, String password) {
        byte[] uidBytes = uid.getBytes(StandardCharsets.UTF_8);
        int uidBytesLength = uidBytes.length;
        byte[] salt16Bytes = new byte[16];
        System.arraycopy(uidBytes, uidBytesLength - 16, salt16Bytes, 0, 16);
        byte[] hashBytes = BCrypt.withDefaults().hash(6, salt16Bytes,
                password.getBytes(StandardCharsets.UTF_8));
        return new String(hashBytes, StandardCharsets.UTF_8);
    }

    @Inject public Context ctx;
    private DataService<Game> gs = null;

    @Override
    public DataService<Game> getGameService() {
        if (gs == null)
            gs = new DummyGameService(ctx);
        return gs;
    }

    private UserProfileService ups = null;
    @Override
    public UserProfileService getUserProfileService() {
        if(ups == null)
            ups = new DummyUserService(ctx);
        return ups;
    }
}
