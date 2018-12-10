package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Optional;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ListObserver;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.GameService;

@Singleton
public class DummyGameService extends DummyDataService<Game> implements GameService {

    @Override
    public void removeGames() {
        removeAll();
    }

    @Override public PlayerJoinGameRequest joinGame(String gameUuid) {
        // TODO(@Roos) Stub
        return new PlayerJoinGameRequest("1", gameUuid,
            PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN, "UserUuid");
    }

    final static String SAVED_GAME_DATA_FOLDER = "saved_games_data";

    @Inject
    public DummyGameService(Context ctx) {
        super(ctx, Game.class);
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
        // TODO @Sapphie Stub
        return new ArrayList<>();
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

        @Override public void addObserver(ListObserver o) {
            // This class issues no updates, thus, no observers are needed
        }

        @Override public boolean containsAll(@NonNull Collection<?> collection) {
            return l.containsAll(collection);
        }

        @Override public void refreshDataAndReset() {
            l.clear();
            l.addAll(collectionProvider.get());
            Log.d("refreshDataAndReset", l.size() + "");
            for(T t: l) {
                Log.d("refreshDataAndReset", t.toString());
            }
        }
    }
}
