package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;

public interface GameService {
    String GAME_UUID_PREFIX = "game|";
    String PROP_INTENT_GAMEUUID = "game uuid";

    Optional<Game> getGame(String gid);
    Set<Game> getAll();
    void saveGame(Game g);
    boolean removeGames();
}
