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
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;

public class UserAuthService {
    @Inject @Named("Dependency Configurators") Set<DependencyConfigurator> coordinators;

    //private RemoteServicesProvider rsp;
    @Inject RemoteServicesProvider rsp;

    @Inject public UserAuthService(@Named("application") Scope scope, Application app) {
        Toothpick.inject(this, scope);

        Set<Class> autowiredClasses = Stream.of(this.getClass().getFields())
                // Injected fields
                .filter(f -> f.isAnnotationPresent(Inject.class))
                // Field class
                .map(Field::getType)
                .collect(Collectors.toSet());

        Set<DependencyConfigurator> coordinatorsWithUnconfiguredDependencies = Stream.of(coordinators)
                .filter(cc -> autowiredClasses.contains(cc.configuredDependencyClass()))
                // Only coordinators with non-configured dependencies
                .filter(cc -> !cc.dependencyIsConfigured())
                .collect(Collectors.toSet());

        if (!coordinatorsWithUnconfiguredDependencies.isEmpty()) {
            List<Intent> configurationActivities = Stream.of(coordinatorsWithUnconfiguredDependencies)
                    .map(DependencyConfigurator::dependencyConfigurationIntent)
                    .map(i -> i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME))
                    .collect(Collectors.toList());

            // Push the configuration activities on top
            //noinspection SimplifyStreamApiCallChains: This warning is wrong
            app.startActivities(Stream.of(configurationActivities).toArray(Intent[]::new));
        }
        //rsp = new DummyRemoteServicesProvider();
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
