package ch.epfl.sweng.erpa.services.dummy.database;

import android.content.Context;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.LazyAsyncStream;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.GameService;

@Singleton
public class DummyGameService implements GameService {
    static final String SAVED_GAME_DATA_FOLDER = "saved_games_data";
    static public String currentUserUuid = "UserUuid";
    Multimap<String, PlayerJoinGameRequest> joinGameRequests = ArrayListMultimap.create();
    Map<String, Game> games = new HashMap<>();

    @Override
    public void removeGames() {
        games = new HashMap<>();
    }

    @Override public PlayerJoinGameRequest joinGame(String gameUuid) {
        PlayerJoinGameRequest request = new PlayerJoinGameRequest(
            UUID.randomUUID().toString(), PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN,
            gameUuid, currentUserUuid);
        if (games.get(gameUuid) == null)
            throw new NoSuchElementException("Could not find the requested game");
        joinGameRequests.put(gameUuid, request);
        return request;
    }

    @Override
    public Optional<Game> getGame(String gameUuid) {
        return Optional.ofNullable(games.get(gameUuid));
    }

    @Override
    public void updateGame(Game g) {
        games.put(g.getUuid(), g);
    }

    @Override public Game createGame(Game g) {
        updateGame(g);
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
        return new ListBackedLazyAsyncStream(new ArrayList<>(games.values()));
    }

    static class ListBackedLazyAsyncStream extends LazyAsyncStream<Game> {
        List<Game> baseList;
        public ListBackedLazyAsyncStream(List games) {
            super(games.size());
            baseList = games;
            this.elements.addAll(baseList);
            this.loading = false;
        }

        @Override public void loadAhead(int from) {
            if (from < baseList.size()) {
                for (int i = from; i < baseList.size() && i < from + chunks; ++i)
                    this.elements.add(baseList.get(i));
            }
            this.loading = false;
            updateObservers();
        }
    }
}
