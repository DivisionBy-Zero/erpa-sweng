package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.List;

import ch.epfl.sweng.erpa.model.Game;

public interface GameService
{
    Optional<Game> getGame(String gid);
    List<Game> getAll();
    void saveGame(String gid);
}
