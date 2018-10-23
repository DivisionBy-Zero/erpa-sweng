package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.model.UserAuth;

public interface UserAuthProvider {
    class UserAlreadyRegisteredException extends Exception {
    }

    class UserDoesNotExistsException extends Exception {
    }

    class UserPublicKeyRegistrationFailed extends Exception {
    }

    String USER_UUID_PREFIX = "user|";
    String SESSION_TOKEN_PREFIX = "s_token|";

    /**
     * Registers a new User.
     * <p>
     * If a user with this name already exists and has an associated public key, this will fail.
     * If a user with this name already exists but has no associated public key, a new User UUID
     * will be generated and returned.
     *
     * @param username Username
     * @return User UUID of the newly created user.
     * @throws UserAlreadyRegisteredException Username with associated public key already exists.
     */
    String registerUser(String username) throws UserAlreadyRegisteredException;

    /**
     * Associates a new Public key to the specified user.
     * <p>
     * If a public key associated to this user exist and the Session Token is not valid, this will fail.
     *
     * @param userAuth        The session token may be null if there's no public key associated to the user.
     * @param base64publicKey User public key to Register.
     * @throws UserPublicKeyRegistrationFailed A public key associated to the user exists and the Session Token is not valid.
     */
    void registerNewUserPublicKey(UserAuth userAuth, String base64publicKey) throws UserPublicKeyRegistrationFailed;

    /**
     * Returns the User UUID of a registered user with known a public key.
     *
     * @param username Username.
     * @return User UUID associated to this username.
     */
    Optional<String> userUuidFromUsername(String username);

    /**
     * Returns a Challenge to be signed with the user's private key to request a Session Token.
     *
     * @param userUuid User UUID.
     * @return A Base64-encoded challenge.
     */
    String getChallenge(String userUuid) throws UserDoesNotExistsException;

    /**
     * Returns a new Session Token provided that the Challenge Verification was correct.
     *
     * @param userUuid        User UUID
     * @param signedChallenge Base64-encoded signed challenge.
     * @return a Session Token, empty if the verification failed or the challenge is not correct.
     */
    Optional<String> verifyChallengeAndGetSessionToken(String userUuid, String signedChallenge);
}
