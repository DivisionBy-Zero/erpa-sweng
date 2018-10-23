package ch.epfl.sweng.erpa.services.dummy;

import com.annimon.stream.Optional;

import org.libsodium.jni.Sodium;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import at.favre.lib.bytes.Bytes;
import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserAuthProvider;

class DummyUserAuthProvider implements UserAuthProvider {
    private Map<String, String> userUuidToBase64PublicKey = new HashMap<>();
    private Map<String, String> userUuidToSessionToken = new HashMap<>();
    private Map<String, String> usernameToUserUuid = new HashMap<>();
    private Map<String, UserProfile> userUuidToUserProfile = new HashMap<>();
    private Map<String, Bytes> userUuidToAuthChallenge = new HashMap<>();

    UserProfile defaultUser = new UserProfile("user|5b915f75-0ff0-43f8-90bf-f9e92533f926", UserProfile.Experience.Casual, false, true);

    @Inject public DummyUserAuthProvider() {
    }

    @Override public Optional<String> userUuidFromUsername(String username) {
        return Optional.of(username)
                .filter(u -> !u.isEmpty())
                .map(u -> usernameToUserUuid.get(u))
                .filter(userUuidToBase64PublicKey::containsKey);
    }

    @Override
    public String registerUser(String username) throws UserAlreadyRegisteredException {
        if (Optional.ofNullable(usernameToUserUuid.get(username))
                .filter(userUuidToBase64PublicKey::containsKey).isPresent()) {
            throw new UserAlreadyRegisteredException();
        }

        String userUuid = USER_UUID_PREFIX + UUID.randomUUID().toString();

        if (usernameToUserUuid.containsKey(userUuid)) {
            System.err.println("User registration UUID collision, retrying...");
            return registerUser(username);
        }

        usernameToUserUuid.put(username, userUuid);
        return userUuid;
    }

    @Override
    public void registerNewUserPublicKey(UserAuth userAuth, String base64publicKey) throws UserPublicKeyRegistrationFailed {

    }

    @Override public String getChallenge(String userUuid) throws UserDoesNotExistsException {
        return Optional.of(userUuid)
                .filter(u -> u.startsWith(USER_UUID_PREFIX))
                .filter(userUuidToBase64PublicKey::containsKey)
                .map(uid -> {
                    byte[] challengeBytes = new byte[32];
                    Sodium.randombytes(challengeBytes, 32);
                    Bytes challenge = Bytes.from(challengeBytes);
                    userUuidToAuthChallenge.put(uid, challenge);
                    return challenge.encodeBase64();
                }).orElseThrow(UserDoesNotExistsException::new);
    }

    @Override
    public Optional<String> verifyChallengeAndGetSessionToken(String userUuid, String base64SignedChallenge) {
        return Optional.of(userUuid)
                .filter(u -> u.startsWith(USER_UUID_PREFIX))
                .filter(userUuidToBase64PublicKey::containsKey)
                .filter(userUuidToAuthChallenge::containsKey)
                .flatMap(u -> {
                    Bytes associatedPublicKey = Bytes.parseBase64(Objects.requireNonNull(userUuidToBase64PublicKey.get(u)));
                    Bytes requestedChallenge = Objects.requireNonNull(userUuidToAuthChallenge.get(u));
                    Bytes signedChallenge = Bytes.parseBase64(base64SignedChallenge);

                    return Optional.ofNullable(
                            verifyAndExtractChallenge(associatedPublicKey, signedChallenge, requestedChallenge.length()))
                            .filter(requestedChallenge::equals)
                            .map(challenge -> SESSION_TOKEN_PREFIX + UUID.randomUUID().toString());
                });
    }

    private Bytes verifyAndExtractChallenge(Bytes associatedPublicKey, Bytes signedChallenge, int expectedLength) {
        byte[] receivedChallengeBytes = new byte[expectedLength];
        if (Sodium.crypto_sign_open(receivedChallengeBytes, null,
                signedChallenge.array(), signedChallenge.length(), associatedPublicKey.array()) != 0) {
            return null;
        }
        return Bytes.from(receivedChallengeBytes);
    }
}
