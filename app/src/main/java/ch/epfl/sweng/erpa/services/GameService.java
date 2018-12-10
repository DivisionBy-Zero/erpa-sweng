package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.FunctionalInterface;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface GameService {
    String PROP_INTENT_GAME_UUID = "game_key";
    String UUID_PREFIX = "game|";

    /**
     * Fetch a game from a game database
     * @param gameUuid the id of the game
     * @return the game if it was found, or empty if it wasn't
     */
    Optional<Game> getGame(String gameUuid) throws IOException, ServerException;

    /**
     * Save an <b>existing</b>game to the game database
     * @param g the game to save
     */
    void updateGame(Game g) throws IOException, ServerException;

    /**
     * Creates a <b>new</b> game and returns it
     * @param g the game, as it was created on the server (can be different than the game given)
     */
    Game createGame(Game g) throws IOException, ServerException;

    /**
     * Fetches the players in a game
     * @param gameUuid the game's uuid
     * @return the set of user profiles
     */
    List<PlayerJoinGameRequest> getGameJoinRequests(String gameUuid) throws IOException, ServerException;

    /**
     * Gets a filtered and sorted lazy stream of games
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
     * @param gameUuid the game to join
     */
    PlayerJoinGameRequest joinGame(String gameUuid) throws IOException, ServerException;

    @NoArgsConstructor
    @Getter
    class StreamRefiner implements Serializable {
        @NonNull SortedMap<SortCriteria, Ordering> sortCriterias = new TreeMap<>();
        @NonNull Set<GameFilter> gameFilters = new HashSet<>();

        public static StreamRefinerBuilder builder() {
            return new StreamRefinerBuilder();
        }

        public StreamRefinerBuilder toBuilder() {
            return new StreamRefinerBuilder(this);
        }

        public enum Ordering {ASCENDING, DESCENDING}

        public enum SortCriteria {DIFFICULTY, MAX_NUMBER_OF_PLAYERS, DISTANCE, DATE}

        public Map<String,String> toStringMap() {
            Map<String,String> result = new HashMap<>();
            for(SortCriteria key : sortCriterias.keySet()) {
                result.put(key.toString(), sortCriterias.get(key).toString());
            }
            return result;
        }

        @FunctionalInterface
        public interface GameFilter extends Function<Game, Boolean> {
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    class StreamRefinerBuilder implements Serializable {
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
        public StreamRefinerBuilder removeOneCriteria(
                @NonNull StreamRefiner.SortCriteria criteria) {
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
