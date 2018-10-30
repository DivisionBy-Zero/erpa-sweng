package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.FunctionalInterface;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.epfl.sweng.erpa.model.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface GameService extends DataService<Game> {
    String PROP_INTENT_GAME = "game_key";
    String UUID_PREFIX = "game|";

    Optional<Game> getGame(String gameUuid);
    void saveGame(Game g);
    Set<Game> getAllGames();
    void removeGames();

    @NoArgsConstructor
    @Getter
    class StreamRefiner {
        @NonNull SortedMap<SortCriteria, Ordering> sortCriterias = new TreeMap<>();
        @NonNull Set<GameFilter> gameFilters = new HashSet<>();

        public static StreamRefinerBuilder builder() {
            return new StreamRefinerBuilder();
        }

        public StreamRefinerBuilder toBuilder() { return new StreamRefinerBuilder(this); }

        public enum Ordering {ASCENDING, DESCENDING}

        public enum SortCriteria {DIFFICULTY, MAX_NUMBER_OF_PLAYERS, DISTANCE, DATE}

        @FunctionalInterface
        public interface GameFilter extends Function<Game, Boolean> {
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    class StreamRefinerBuilder {
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
            return  this;
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
