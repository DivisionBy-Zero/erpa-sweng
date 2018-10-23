package ch.epfl.sweng.erpa.services;

import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;

import org.libsodium.jni.Sodium;
import org.libsodium.jni.keys.KeyPair;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.epfl.sweng.erpa.model.UserAuth;

import static org.libsodium.jni.SodiumConstants.PUBLICKEY_BYTES;
import static org.libsodium.jni.SodiumConstants.SECRETKEY_BYTES;
import static org.libsodium.jni.SodiumConstants.SIGNATURE_BYTES;

public class UserAuthService extends DependencyConfigurationAgnosticService {

    @Inject RemoteServicesProvider rsp;
    private UserAuthProvider uap;

    @Inject public UserAuthService() {
        uap = rsp.getUserAuthProvider();
    }

    public Optional<UserAuth> getUserAuth(String username, String password) {
        return uap.userUuidFromUsername(username).flatMap(userUUID ->
            createHashFromUserUuidAndPass(userUUID, password).flatMap(Function.Util.safe(hash ->
                // Shouldn't throw since we just recovered the UUID for this username
                authenticate(userUUID, generateKeyPairFromHash(hash)).map(sessionToken ->
                    new UserAuth(userUUID, sessionToken)))));
    }

    public Optional<UserAuth> signUpUser(String username, String password) throws UserAuthProvider.UserAlreadyRegisteredException {
        String newUserUUID = uap.registerUser(username);
        return createHashFromUserUuidAndPass(newUserUUID, password).flatMap(Function.Util.safe(userUUID -> {
            KeyPair keyPair = generateKeyPairFromHash(userUUID);
            String base64publicKey = Bytes.from(keyPair.getPublicKey().toBytes()).encodeBase64();
            // Shouldn't throw since (signing up new user => token valid) and we didn't throw when creating the new UUID.
            uap.registerNewUserPublicKey(new UserAuth(userUUID, null), base64publicKey);
            // Shouldn't throw since we just created the user!
            return authenticate(userUUID, keyPair).map(sessionToken -> new UserAuth(userUUID, sessionToken));
        }));
    }

    /**
     * Creates a resilient hash from an user UUID and his password.
     *
     * The resulting hash is OpenBSD's BCrypt hash of password;
     * the salt used in BCrypt is the result of encrypting the UUID using AES with the specified password
     *
     * @param userUuid User UUID
     * @param password User Password
     * @return a base64-encoded hash, empty in case of failure
     */
    private Optional<String> createHashFromUserUuidAndPass(String userUuid, String password) {
        Bytes userUuidBytes = Bytes.from(userUuid, StandardCharsets.UTF_8);
        Bytes passwordBytes = Bytes.from(password, StandardCharsets.UTF_8);

        Bytes last16BytesFromUid = userUuidBytes.reverse().resize(16).reverse();

        try {
            Bytes salt = encryptAES(passwordBytes, last16BytesFromUid);
            Bytes hash = bcryptPasswordHash(salt, passwordBytes);
            return Optional.of(hash.encodeBase64());
        } catch (Exception e) {
            Log.e("mkHashFromUserUUID", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Negotiates a Session token with the server
     *
     * @param userUuid User UUID to Authenticate
     * @param keyPair User Password-Derived Keypair
     * @return Session token, empty if challenge verification failed
     */
    private Optional<String> authenticate(String userUuid, KeyPair keyPair) throws UserAuthProvider.UserDoesNotExistsException {
        Bytes challenge = Bytes.parseBase64(uap.getChallenge(userUuid));
        byte[] signedMessage = new byte[SIGNATURE_BYTES + challenge.length()];
        Sodium.crypto_sign_ed25519(signedMessage, null, challenge.array(), challenge.length(),
                keyPair.getPrivateKey().toBytes());
        return uap.verifyChallengeAndGetSessionToken(userUuid, Bytes.from(signedMessage).encodeBase64());
    }

    /**
     * Generates a deterministic ed25519 asymmetric keypair from the given hash
     * @param hash Base64-encoded hash
     * @return ed25519 keypair
     */
    private KeyPair generateKeyPairFromHash(String hash) {
        byte[] sk = new byte[SECRETKEY_BYTES];
        byte[] pk = new byte[PUBLICKEY_BYTES];
        Sodium.crypto_box_curve25519xsalsa20poly1305_seed_keypair(pk, sk, Bytes.parseBase64(hash).array());
        Log.d("Public Key: ", Bytes.from(pk).encodeBase64());
        return new KeyPair(sk);
    }

    private static Bytes encryptAES(Bytes key, Bytes plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.array(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return Bytes.from(cipher.doFinal(plainText.array()));
    }

    private static Bytes bcryptPasswordHash(Bytes salt, Bytes password) {
        return Bytes.from(BCrypt.withDefaults().hash(6, salt.array(), password.array()));
    }
}
