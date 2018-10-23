package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.Game;

public interface GameService
{
    //constants
    String EXTRA_GAME_KEY = "game|";

    //methods
    Optional<Game> getGame(String gid);
    Set<Game> getAll();
    void saveGame(Game g);
}