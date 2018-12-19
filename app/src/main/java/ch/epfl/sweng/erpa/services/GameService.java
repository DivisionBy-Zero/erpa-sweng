package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.FunctionalInterface;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface GameService {
    String PROP_INTENT_GAME_UUID = "game_key";
    String UUID_PREFIX = "game|";

    /**
     * Fetch a game from a game database
     *
     * @param gameUuid the id of the game
     * @return the game if it was found, or empty if it wasn't
     */
    Optional<Game> getGame(String gameUuid) throws IOException, ServerException;

    /**
     * Save an <b>existing</b>game to the game database
     *
     * @param g the game to save
     */
    void updateGame(Game g) throws IOException, ServerException;

    /**
     * Creates a <b>new</b> game and returns it
     *
     * @param g the game, as it was created on the server (can be different than the game given)
     */
    Game createGame(Game g) throws IOException, ServerException;

    /**
     * Fetches the players in a game
     *
     * @param gameUuid the game's uuid
     * @return the set of user profiles
     */
    List<PlayerJoinGameRequest> getGameJoinRequests(String gameUuid) throws IOException, ServerException;

    /**
     * Modifies a given JoinGameRequests. Used by the GM change the status of a request.
     *
     * @param gameUuid the game's uuid
     * @return the set of user profiles
     */
    PlayerJoinGameRequest updateGameJoinRequest(String gameUuid, PlayerJoinGameRequest request) throws IOException, ServerException;

    /**
     * Gets a filtered and sorted lazy stream of games
     *
     * @param sr the ordering and filtering criteria
     * @return a lazy stream
     */
    ObservableAsyncList<Game> getAllGames(StreamRefiner sr);

    /**
     * Deletes all games
     */
    void removeGames();

    /**
     * Sends a join request for a specific game
     *
     * @param gameUuid the game to join
     */
    PlayerJoinGameRequest joinGame(String gameUuid) throws IOException, ServerException;

    @NoArgsConstructor
    @Getter
    public class StreamRefiner implements Serializable {
        @NonNull SortedMap<SortCriteria, Ordering> sortCriterias = new TreeMap<>();
        @NonNull Set<GameFilter> gameFilters = new HashSet<>();

        public static StreamRefinerBuilder builder() {
            return new StreamRefinerBuilder();
        }

        public StreamRefinerBuilder toBuilder() {
            return new StreamRefinerBuilder(this);
        }

        public Map<String, String> toStringMap() {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<SortCriteria, Ordering> kv : sortCriterias.entrySet()) {
                result.put(kv.getKey().toString(), kv.getValue().toString());
            }
            Stream.of(gameFilters).map(GameFilter::queryParams)
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(p -> result.put(p.getFirst(), p.getSecond()));
            return result;
        }

        public enum Ordering {
            ASCENDING("asc"),
            DESCENDING("desc");

            private final String repr;

            Ordering(String repr) {
                this.repr = repr;
            }

            @Override public String toString() {
                return repr;
            }
        }

        public enum SortCriteria {
            DATE("date"),
            DIFFICULTY("difficulty"),
            DISTANCE("distance"),
            MAX_NUMBER_OF_PLAYERS("max_players"),
            MIN_NUMBER_OF_PLAYERS("min_players");

            private final String repr;

            SortCriteria(String repr) {
                this.repr = repr;
            }

            @Override public String toString() {
                return "sort_" + repr;
            }
        }

        @FunctionalInterface
        public interface GameFilter extends Function<Game, Boolean> {
            default Optional<Pair<String, String>> queryParams() {
                return Optional.empty();
            }
        }

        public static class WithGameMaster implements GameFilter, Serializable {
            public final String user_uuid;

            public WithGameMaster(String userUuid) {
                this.user_uuid = Objects.requireNonNull(userUuid);
            }

            @Override public Boolean apply(Game game) {
                return user_uuid.equals(game.getGmUserUuid());
            }

            @Override public Optional<Pair<String, String>> queryParams() {
                return Optional.of(new Pair<>("with_gm", user_uuid));
            }
        }

        public static class WithTitle implements GameFilter, Serializable {
            public final String titleQuery;

            public WithTitle(String titleQuery) {
                this.titleQuery = Objects.requireNonNull(titleQuery);
            }

            @Override public Boolean apply(Game game) {
                return game.getTitle().toLowerCase().contains(titleQuery.toLowerCase());
            }

            @Override public Optional<Pair<String, String>> queryParams() {
                return Optional.of(new Pair<>("title_query", titleQuery));
            }
        }

        public static class WithPlayerPending implements GameFilter, Serializable {
            public final String playerUuid;

            public WithPlayerPending(String playerUuid) {
                this.playerUuid = playerUuid;
            }

            @Override public Boolean apply(Game game) {
                throw new UnsupportedOperationException("Cannot filter, requires a Game Service operation.");
            }

            @Override public Optional<Pair<String, String>> queryParams() {
                return Optional.of(new Pair<>("player_pending", playerUuid));
            }
        }

        public static class WithPlayerConfirmed implements GameFilter, Serializable {
            public final String playerUuid;

            public WithPlayerConfirmed(String playerUuid) {
                this.playerUuid = playerUuid;
            }

            @Override public Boolean apply(Game game) {
                throw new UnsupportedOperationException("Cannot filter, requires a Game Service operation.");
            }

            @Override public Optional<Pair<String, String>> queryParams() {
                return Optional.of(new Pair<>("player_confirmed", playerUuid));
            }
        }

        public static class WithGameStatus implements GameFilter, Serializable {
            public final Game.GameStatus gameStatus;

            public WithGameStatus(Game.GameStatus gameStatus) {
                this.gameStatus = gameStatus;
            }

            @Override public Boolean apply(Game game) {
                return gameStatus.equals(game.getGameStatus());
            }

            @Override public Optional<Pair<String, String>> queryParams() {
                return Optional.of(new Pair<>("game_status", gameStatus.toString()));
            }
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public class StreamRefinerBuilder implements Serializable {
        private StreamRefiner result = new StreamRefiner();

        /**
         * Add a {@code SortCriteria} and its associated {@code Ordering} to the map of existing criteria.
         * If a criteria is already present, only the {@code Ordering} will be changed and the criteria will
         * keep it's previous priority.
         *
         * @param criteria the criteria to sort by
         * @param ordering the ordering associated to the {@code criteria}
         * @return a {@link StreamRefinerBuilder}
         */
        public StreamRefinerBuilder sortBy(@NonNull StreamRefiner.SortCriteria criteria,
                                           StreamRefiner.Ordering ordering) {
            result.getSortCriterias().put(criteria, ordering);
            return this;
        }

        public StreamRefinerBuilder filterBy(@NonNull StreamRefiner.GameFilter gameFilter) {
            result.getGameFilters().add(gameFilter);
            return this;
        }

        /**
         * Remove a {@code SortCriteria} and its associated {@code Ordering} from the map of existing
         * criteria if it exists.
         *
         * @param criteria the criteria to sort by
         * @return a {@link StreamRefinerBuilder}
         */
        public StreamRefinerBuilder removeOneCriteria(@NonNull StreamRefiner.SortCriteria criteria) {
            result.getSortCriterias().remove(criteria);
            return this;
        }

        public StreamRefinerBuilder removeOneFilter(@NonNull StreamRefiner.GameFilter gameFilter) {
            result.getGameFilters().remove(gameFilter);
            return this;
        }

        /**
         * Remove all {@code SortCriteria} and their associated {@code Ordering} from the map of existing
         * criteria.
         *
         * @return a {@link StreamRefinerBuilder}
         */
        public StreamRefinerBuilder clearCriteria() {
            if (result.getSortCriterias() != null)
                result.getSortCriterias().clear();
            return this;
        }

        public StreamRefinerBuilder clearFilters() {
            if (result.getGameFilters() != null)
                result.getGameFilters().clear();
            return this;
        }

        public StreamRefinerBuilder clearRefinements() {
            clearCriteria();
            clearFilters();
            return this;
        }

        public StreamRefiner build() {
            return result;
        }
    }
}
