package ch.epfl.sweng.erpa.services;

import android.app.Application;
import android.content.Intent;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.operations.DependencyConfigurator;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;

public class UserAuthService {
    @Inject @Named("Dependency Configurators") Set<DependencyConfigurator> coordinators;

    //private RemoteServicesProvider rsp;
    @Inject RemoteServicesProvider rsp;

    @Inject public UserAuthService(@Named("application") Scope scope, Application app) {
        RemoteServicesProviderCoordinator rsp = scope.getInstance(RemoteServicesProviderCoordinator.class);
        rsp.rspClassFromFullyQualifiedName("ch.epfl.sweng.erpa.services.dummy.DummyRemoteServiceProvider").ifPresent(rsp::bindRemoteServicesProvider);
    }

    public Optional<UserAuth> getUserAuth(String username, String password) {
        Optional<String> uid = rsp.getUidFromUsername(username);
        if (uid.isPresent()) {
            String id = uid.get();
            String accessToken = createAccessToken(id, password);
            if(rsp.verifyAccessToken(id, accessToken)) {
                return Optional.of(new UserAuth(id, accessToken));
            }
        }
        return Optional.empty();
    }

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
