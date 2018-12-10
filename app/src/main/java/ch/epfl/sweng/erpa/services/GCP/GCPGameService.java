package ch.epfl.sweng.erpa.services.GCP;

import android.util.Log;

import com.annimon.stream.Optional;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.GameService;

@Singleton
public class GCPGameService implements GameService {
    private final GCPApi.GameInterface gameInterface;
    @Inject OptionalDependencyManager odm;

    @Inject public GCPGameService() {
        this.gameInterface = GCPRemoteServicesProvider.getRetrofit().create(GCPApi.GameInterface.class);
    }

    private String mkAuthHeader() {
        return odm.get(UserSessionToken.class).map(UserSessionToken::getSessionToken).orElse("");
    }

    @Override public Optional<Game> getGame(String gameUuid) throws IOException, ServerException {
        return GCPRemoteServicesProvider.callAndReturnOptional(gameInterface.getGame(gameUuid, mkAuthHeader()));
    }

    @Override public void updateGame(Game g) throws IOException, ServerException {
        GCPRemoteServicesProvider.executeAndThrowOnError(gameInterface.updateGame(g.getUuid(), g, mkAuthHeader()));
    }

    @Override public Game createGame(Game g) throws IOException, ServerException {
        return GCPRemoteServicesProvider.executeAndThrowOnError(gameInterface.saveGame(g, mkAuthHeader())).body();
    }

    @Override
    public List<PlayerJoinGameRequest> getGameJoinRequests(String gameUuid) throws IOException, ServerException {
        return GCPRemoteServicesProvider.executeAndThrowOnError(
            gameInterface.getGamePlayerJoinRequests(gameUuid, mkAuthHeader())).body();
    }

    @Override public ObservableAsyncList<Game> getAllGames(StreamRefiner sr) {
        return new LazyGameStream(10, sr.toStringMap());
    }

    @Override public void removeGames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlayerJoinGameRequest joinGame(String gameUuid) throws IOException, ServerException {
        Log.d("joinGame", "Attempting to join game " + gameUuid);
        return GCPRemoteServicesProvider.executeAndThrowOnError(
            gameInterface.joinGame(gameUuid, mkAuthHeader())).body();
    }
}
