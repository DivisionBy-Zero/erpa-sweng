package ch.epfl.sweng.erpa.services.dummy;

import android.content.Context;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.UserAuthProvider;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import lombok.Getter;


public class DummyRemoteServicesProvider implements RemoteServicesProvider {
    @Inject public Context ctx;
    @Inject @Getter public UserAuthProvider userAuthProvider;
    @Inject @Getter public GameService gameService;

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
