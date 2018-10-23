package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;

public interface GameService extends DataService<Game>{

    String EXTRA_GAME_KEY = "game|";

    Optional<Game> getGame(String gameUuid);
    void saveGame(Game g);
    Set<Game> getAllGames();
}
