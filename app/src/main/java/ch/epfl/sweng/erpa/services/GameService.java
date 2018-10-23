package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.epfl.sweng.erpa.model.Game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface GameService {
    String GAME_UUID_PREFIX = "game|";
    String PROP_INTENT_GAMEUUID = "game uuid";

    Optional<Game> getGame(String gid);

    Set<Game> getAll();

    void saveGame(Game g);

    boolean removeGames();

    @NoArgsConstructor
    @Getter
    class StreamRefiner {
        @NonNull SortedMap<SortCriteria, Ordering> sortCriterias = new TreeMap<>();

        public static StreamRefinerBuilder builder() {
            return new StreamRefinerBuilder();
        }

        public StreamRefinerBuilder toBuilder() { return new StreamRefinerBuilder(this); }

        public enum Ordering {ASCENDING, DESCENDING}

        public enum SortCriteria {DIFFICULTY, MAX_NUMBER_OF_PLAYERS, DISTANCE, DATE}
    }

    @AllArgsConstructor
    @NoArgsConstructor
    class StreamRefinerBuilder {
        private StreamRefiner result = new StreamRefiner();

        public StreamRefinerBuilder sortBy(@NonNull StreamRefiner.SortCriteria criteria,
                                           StreamRefiner.Ordering ordering) {
            result.getSortCriterias().put(criteria, ordering);
            return this;
        }

        public StreamRefinerBuilder removeOneCriteria(@NonNull StreamRefiner.SortCriteria criteria) {
            result.getSortCriterias().remove(criteria);
            return this;
        }

        public StreamRefinerBuilder clearCriteria() {
            if (result.getSortCriterias() != null)
                result.getSortCriterias().clear();
            return this;
        }

        public StreamRefiner build() {
            return result;
        }
    }
}
