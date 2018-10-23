package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

public interface GameService {
    String GAME_UUID_PREFIX = "game|";
    String PROP_INTENT_GAMEUUID = "game uuid";

    Optional<Game> getGame(String gid);

    Set<Game> getAll();

    void saveGame(Game g);

    @NoArgsConstructor
    @Data
    class StreamRefiner {
        @NonNull List<SortCriteria> sortCriterias = new ArrayList<>();
        @NonNull Ordering ordering = Ordering.DESCENDING;

        enum Ordering {ASCENDING, DESCENDING}

        enum SortCriteria {DIFFICULTY, MAX_NUMBER_OF_PLAYERS, DISTANCE}
    }

    class StreamRefinerBuilder {
        StreamRefiner result = new StreamRefiner();

        StreamRefinerBuilder sortBy(@NonNull StreamRefiner.SortCriteria criteria) {
            result.getSortCriterias().add(criteria);
            return this;
        }

        StreamRefinerBuilder ascending() {
            result.setOrdering(StreamRefiner.Ordering.ASCENDING);
            return this;
        }

        StreamRefinerBuilder descending() {
            result.setOrdering(StreamRefiner.Ordering.DESCENDING);
            return this;
        }
    }
}
