package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.GameService;

@Singleton
public class DummyGameService extends DummyDataService<Game> implements GameService {
    static final String SAVED_GAME_DATA_FOLDER = "saved_games_data";
    Multimap<String, PlayerJoinGameRequest> joinGameRequests = ArrayListMultimap.create();
    static public String currentUserUuid = "UserUuid";

    @Inject
    public DummyGameService(Context ctx) {
        super(ctx, Game.class);
    }

    @Override
    public void removeGames() {
        removeAll();
    }

    @Override public PlayerJoinGameRequest joinGame(String gameUuid) {
        PlayerJoinGameRequest request = new PlayerJoinGameRequest(
            UUID.randomUUID().toString(), PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN,
            gameUuid, currentUserUuid);
        joinGameRequests.put(gameUuid, request);
        return request;
    }

    @Override
    String dataFolder() {
        return SAVED_GAME_DATA_FOLDER;
    }

    @Override
    public Optional<Game> getGame(String gameUuid) {
        return getOne(gameUuid);
    }

    @Override
    public void updateGame(Game g) {
        saveOne(g);
    }

    @Override public Game createGame(Game g) {
        saveOne(g);
        return g;
    }

    @Override public List<PlayerJoinGameRequest> getGameJoinRequests(String gameUuid) {
        return new ArrayList<>(joinGameRequests.get(gameUuid));
    }

    @Override
    public PlayerJoinGameRequest updateGameJoinRequest(String gameUuid, PlayerJoinGameRequest request) throws IOException, ServerException {
        if (!joinGameRequests.containsKey(gameUuid))
            throw new NoSuchElementException("Unknown GameId");
        if (!request.getGameUuid().equals(gameUuid))
            throw new IllegalArgumentException("Request GameUuid does not match current GameUuid");
        PlayerJoinGameRequest current = Stream.of(joinGameRequests.get(gameUuid))
            .filter(rs -> rs.getJoinRequestId().equals(request.getJoinRequestId()))
            .findFirst().orElseThrow(() -> new NoSuchElementException("Could not find requestId"));
        if (!request.getUserUuid().equals(current.getUserUuid()))
            throw new IllegalArgumentException("Request UserUuid does not match current UserUuid");
        joinGameRequests.remove(gameUuid, current);
        joinGameRequests.put(gameUuid, request);
        return request;
    }

    @Override
    public ObservableAsyncList<Game> getAllGames(StreamRefiner sr) {
        return new ListToObservableListAdapter<Game>(this::getAll);
    }

    private static class ListToObservableListAdapter<T> extends AbstractList<T> implements ObservableAsyncList<T> {
        private final List<T> l;
        private final Provider<Collection<T>> collectionProvider;

        ListToObservableListAdapter(Provider<Collection<T>> l) {
            this.l = new ArrayList<>(l.get());
            collectionProvider = l;
        }

        @Override public T get(int idx) {
            return l.get(idx);
        }

        @Override public int size() {
            return l.size();
        }

        @Override public boolean isLoading() {
            return false;
        }

        @Override public void updateObservers() {
            // Nothing
        }

        @Override public void addObserver(Consumer<ObservableAsyncList<T>> o) {
            // This class issues no updates, thus, no observers are needed
        }

        @Override public boolean containsAll(@NonNull Collection<?> collection) {
            return l.containsAll(collection);
        }

        @Override public void refreshDataAndReset() {
            l.clear();
            l.addAll(collectionProvider.get());
            Log.d("refreshDataAndReset", l.size() + "");
            for (T t : l) {
                Log.d("refreshDataAndReset", t.toString());
            }
        }
    }
}
