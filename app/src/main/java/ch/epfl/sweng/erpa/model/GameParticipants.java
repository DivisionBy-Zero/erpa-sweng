package ch.epfl.sweng.erpa.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameParticipants {
    private List<String> players;

    public GameParticipants(List<String> players) {
        this.players = new ArrayList<>(players);
    }

    @Override
    public String toString() {
        return "GameParticipants{" +
                "players=" + players +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameParticipants that = (GameParticipants) o;
        return Objects.equals(players, that.players);
    }

    @Override
    public int hashCode() {

        return Objects.hash(players);
    }

    public List<String> getPlayers() {

        return Collections.unmodifiableList(players);
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
