package ch.epfl.sweng.erpa.model;

import java.util.List;
import java.util.Objects;
import com.annimon.stream.Optional;

public class Game {
    private String gmUniqueID;
    private GameParticipants players;
    private String name;
    private int minPlayer;
    private int maxPlayer;
    private Difficulty difficulty;
    private String universe;
    private OneshotOrCampaign oneshotOrCampaign;
    private Optional<Integer> numberSessions;
    private Optional<Integer> sessionLength;
    private String description;

    public Game(String gmUniqueID, String name, int minPlayer,
                int maxPayer, Difficulty difficulty, String universe,
                OneshotOrCampaign oneshotOrCampaign, Optional<Integer> numberSessions,
                Optional<Integer> sessionLength, String description) {
        this.gmUniqueID = gmUniqueID;
        this.name = name;
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPayer;
        this.difficulty = difficulty;
        this.universe = universe;
        this.oneshotOrCampaign = oneshotOrCampaign;
        this.numberSessions = numberSessions;
        this.sessionLength = sessionLength;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getMinPlayer() {
        return minPlayer;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public String getUniverse() {
        return universe;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public OneshotOrCampaign getOneshotOrCampaign() {
        return oneshotOrCampaign;
    }

    public Optional<Integer> getNumberSessions() {
        return numberSessions;
    }

    public Optional<Integer> getSessionLength() {
        return sessionLength;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gmUniqueID='" + gmUniqueID + '\'' +
                ", players=" + players +
                ", name='" + name + '\'' +
                ", minPlayer='" + minPlayer + '\'' +
                ", maxPayer='" + maxPlayer + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", universe='" + universe + '\'' +
                ", oneshotOrCampaign='" + oneshotOrCampaign + '\'' +
                ", numberSessions='" + numberSessions + '\'' +
                ", sessionLength='" + sessionLength + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(gmUniqueID, game.gmUniqueID) &&
                Objects.equals(players, game.players) &&
                Objects.equals(name, game.name) &&
                Objects.equals(minPlayer, game.minPlayer) &&
                Objects.equals(maxPlayer, game.maxPlayer) &&
                Objects.equals(difficulty, game.difficulty) &&
                Objects.equals(universe, game.universe) &&
                Objects.equals(oneshotOrCampaign, game.oneshotOrCampaign) &&
                Objects.equals(numberSessions, game.numberSessions) &&
                Objects.equals(sessionLength, game.sessionLength) &&
                Objects.equals(description, game.description);
    }

    @Override
    public int hashCode() {

        return Objects.hash(gmUniqueID, players, name, minPlayer, maxPlayer, difficulty, universe, oneshotOrCampaign, numberSessions, sessionLength, description);
    }

    public enum Difficulty {
        NOOB, CHILL, HARD
    }
    public enum OneshotOrCampaign {
        ONESHOT, CAMPAIGN
    }
}
