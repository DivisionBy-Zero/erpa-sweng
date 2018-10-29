package ch.epfl.sweng.erpa.services.GCF;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;

class GCFGameService extends GCFDataService<Game> implements GameService {
    @Override public Optional<Game> getGame(String gameUuid) {
        return getOne(gameUuid);
    }

    @Override public void saveGame(Game g) {
        saveOne(g);
    }

    @Override public Stream<Game> getAllGames(StreamRefiner streamRefiner) {
        return Stream.of();
        // TODO (@Sapphie): send request
    }

    @Override public void removeGames() {
        removeAll();
    }
}
