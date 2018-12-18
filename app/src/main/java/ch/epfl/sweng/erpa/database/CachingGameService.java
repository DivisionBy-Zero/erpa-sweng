package ch.epfl.sweng.erpa.database;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Consumer;

import java.io.IOException;
import java.util.List;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.services.GCP.LazyGameStream;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.GameService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CachingGameService implements GameService {
    private final GameService wrappedService;
    private final CachingRemoteServicesProvider parent;

    @Override public Optional<Game> getGame(String gameUuid) throws IOException, ServerException {
        return null;
    }

    @Override public void updateGame(Game g) throws IOException, ServerException {
        wrappedService.updateGame(g);
    }

    @Override public Game createGame(Game g) throws IOException, ServerException {
        return wrappedService.createGame(g);
    }

    @Override
    public List<PlayerJoinGameRequest> getGameJoinRequests(String gameUuid) throws IOException, ServerException {
        return wrappedService.getGameJoinRequests(gameUuid);
    }

    @Override
    public PlayerJoinGameRequest updateGameJoinRequest(String gameUuid, PlayerJoinGameRequest request) throws IOException, ServerException {
        return wrappedService.updateGameJoinRequest(gameUuid, request);
    }

    @Override public ObservableAsyncList<Game> getAllGames(StreamRefiner sr) {
        if (parent.getCached().isPresent() && sr.getGameFilters().isEmpty()) {
            List<Game> initialContent = parent.getCached().get().gameDao().getAll();
            return new AsyncStreamWrapper(initialContent, wrappedService.getAllGames(sr));
        } else {
            return wrappedService.getAllGames(sr);
        }
    }

    @Override public void removeGames() {
        wrappedService.removeGames();
    }

    @Override
    public PlayerJoinGameRequest joinGame(String gameUuid) throws IOException, ServerException {
        return wrappedService.joinGame(gameUuid);
    }

    @Override
    public ObservableAsyncList<Game> getAllGames(StreamRefiner sr, List<Game> initialCapacity) {
        return wrappedService.getAllGames(sr, initialCapacity);
    }


    @RequiredArgsConstructor
    private static class AsyncStreamWrapper implements ObservableAsyncList<Game> {

        private final List<Game> cachedContent;
        private final ObservableAsyncList<Game> proxiedAsyncList;

        @Override public Game get(int i) {
            if (proxiedAsyncList.size() > 0) {
                return proxiedAsyncList.get(i);
            } else {
                return cachedContent.get(i);
            }
        }

        @Override public int size() {
            if (proxiedAsyncList.size() > 0) {
                return proxiedAsyncList.size();
            } else {
                return cachedContent.size();
            }
        }

        @Override public boolean isLoading() {
            return proxiedAsyncList.isLoading();
        }

        @Override public void updateObservers() {
            proxiedAsyncList.updateObservers();
        }

        @Override public void addObserver(Consumer<ObservableAsyncList<Game>> o) {
            proxiedAsyncList.addObserver(o);
        }

        @Override public void refreshDataAndReset() {
            proxiedAsyncList.refreshDataAndReset();
        }
    }
}
