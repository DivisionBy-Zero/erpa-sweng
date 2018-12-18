package ch.epfl.sweng.erpa.database;

import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Supplier;
import com.annimon.stream.function.ThrowableSupplier;

import java.io.IOException;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class CachingUserService implements UserManagementService {
    private final UserManagementService wrappedService;
    private final CachingRemoteServicesProvider parent;

    @Override
    public String getUuidForUsername(String username) throws IOException, ServerException {
        try {
            return parent.proxyAndCache(
                    () -> wrappedService.getUuidForUsername(username), // delegated call
                    (database, usernameEntry) -> database.usernameDao().insertAll(usernameEntry), // inserter
                    (uuid) -> new Username(uuid, username), // uuid to username
                    Username::getUserUuid, // username to uuid
                    erpaDatabase -> erpaDatabase.usernameDao().getUsername(username)); // database getter
        } catch (IOException | ServerException throwable) {
            throw throwable;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public Optional<Username> getUsernameFromUserUuid(String userUuid) throws IOException, ServerException {
        return wrappedService.getUsernameFromUserUuid(userUuid);
    }

    @Override
    public String registerNewUsername(String username) throws IOException, ServerException {
        return wrappedService.registerNewUsername(username);
    }

    @Override public Void registerAuth(UserAuth auth) throws IOException, ServerException {
        return wrappedService.registerAuth(auth);
    }

    @Override
    public Optional<UserProfile> getUserProfile(String userUuid) throws IOException, ServerException {
        try {
            return parent.<Optional<UserProfile>, UserProfile>proxyAndCache(
                    () -> wrappedService.getUserProfile(userUuid),
                    (database, userProfile) -> {
                        if (userProfile != null) {
                            database.userProfileDao().insertAll(userProfile);
                        }
                    },
                    userProfileOptional -> userProfileOptional.orElse(null),
                    Optional::ofNullable,
                    erpaDatabase -> erpaDatabase.userProfileDao().getUserProfile(userUuid)
            );
        } catch (IOException | ServerException throwable) {
            throw throwable;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public UserProfile saveUserProfile(UserProfile up) throws IOException, ServerException {
        return wrappedService.saveUserProfile(up);
    }

    @Override
    public UserProfile registerUserProfile(UserProfile up) throws IOException, ServerException {
        return wrappedService.registerUserProfile(up);
    }
}
