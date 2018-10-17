package ch.epfl.sweng.erpa.services;

import android.app.Application;

import com.annimon.stream.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.operations.DependencyConfigurator;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import toothpick.Scope;
import toothpick.Toothpick;

import static ch.epfl.sweng.erpa.utils.ActivityUtils.createAccessToken;

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
}
