package ch.epfl.sweng.erpa.services.GCP;

import com.annimon.stream.Optional;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.UserManagementService;

@Singleton
public class GCPUserManagementService implements UserManagementService {
    private final GCPApi.UserInterface userInterface;
    @Inject OptionalDependencyManager odm;

    @Inject public GCPUserManagementService() {
        this.userInterface = GCPRemoteServicesProvider.getRetrofit().create(GCPApi.UserInterface.class);
    }

    private static String mkAuthHeader(UserSessionToken sessionToken) {
        return sessionToken.getSessionToken();
    }

    private String mkAuthHeader() {
        return odm.get(UserSessionToken.class).map(UserSessionToken::getSessionToken).orElse("");
    }

    @Override
    public Optional<UserProfile> getUserProfile(String userUuid) throws IOException, ServerException {
        return GCPRemoteServicesProvider.callAndReturnOptional(userInterface.getUser(userUuid, mkAuthHeader()));
    }

    @Override
    public UserProfile saveUserProfile(UserProfile up) throws IOException, ServerException {
        return userInterface.updateUser(up, up.getUuid(), mkAuthHeader()).execute().body();
    }

    @Override
    public UserProfile registerUserProfile(UserProfile up) throws IOException, ServerException {
        return GCPRemoteServicesProvider.executeAndThrowOnError(userInterface.registerUser(up)).body();
    }

    @Override
    public String getUuidForUsername(String username) throws IOException, ServerException {
        return GCPRemoteServicesProvider.executeAndThrowOnError(
            userInterface.getUuidFromUsername(username)).body().string();
    }

    @Override
    public Optional<Username> getUsernameFromUserUuid(String userUuid) throws IOException, ServerException {
        return GCPRemoteServicesProvider.callAndReturnOptional(userInterface.getUsernameFromUuid(userUuid));
    }

    @Override
    public String registerNewUsername(String username) throws IOException, ServerException {
        return GCPRemoteServicesProvider.executeAndThrowOnError(
            userInterface.registerUsername(username)).body().string();
    }

    @Override public Void registerAuth(UserAuth auth) throws IOException, ServerException {
        return GCPRemoteServicesProvider.executeAndThrowOnError(userInterface.registerAuth(auth)).body();
    }
}
