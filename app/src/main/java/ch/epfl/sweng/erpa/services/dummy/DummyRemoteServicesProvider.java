package ch.epfl.sweng.erpa.services.dummy;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyUserService;
import lombok.Getter;


@Singleton
public class DummyRemoteServicesProvider implements RemoteServicesProvider {
    @Inject @Getter DummyGameService gameService;
    @Inject @Getter DummyUserService userProfileService;

    private ArrayList<UserProfile> userList;

    @Inject public DummyRemoteServicesProvider() {
        UserProfile defaultUser = new UserProfile("user|5b915f75-0ff0-43f8-90bf-f9e92533f926", false, true);
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
    public void terminate() {
    }
}
