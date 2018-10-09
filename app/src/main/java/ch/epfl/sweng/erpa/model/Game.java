package ch.epfl.sweng.erpa.model;

import java.util.List;
import java.util.Objects;

public class Game {
    private String gmName;
    private List<String> players;
    private String name;
    private String minPlayer;
    private String maxPayer;
    private String difficulty;
    private String universe;
    private String oneshotOrCampaign;
    private String numberSessions;
    private String sessionLength;
    private String description;

    public Game(String gmName, String name, String minPlayer,
                String maxPayer, String difficulty, String universe,
                String oneshotOrCampaign, String numberSessions,
                String sessionLength, String description) {
        this.gmName = gmName;
        this.name = name;
        this.minPlayer = minPlayer;
        this.maxPayer = maxPayer;
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

    public String getMinPlayer() {
        return minPlayer;
    }

    public String getMaxPayer() {
        return maxPayer;
    }

    public String getUniverse() {
        return universe;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getOneshotOrCampaign() {
        return oneshotOrCampaign;
    }

    public String getNumberSessions() {
        return numberSessions;
    }

    public String getSessionLength() {
        return sessionLength;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gmName='" + gmName + '\'' +
                ", players=" + players +
                ", name='" + name + '\'' +
                ", minPlayer='" + minPlayer + '\'' +
                ", maxPayer='" + maxPayer + '\'' +
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
        return Objects.equals(gmName, game.gmName) &&
                Objects.equals(players, game.players) &&
                Objects.equals(name, game.name) &&
                Objects.equals(minPlayer, game.minPlayer) &&
                Objects.equals(maxPayer, game.maxPayer) &&
                Objects.equals(difficulty, game.difficulty) &&
                Objects.equals(universe, game.universe) &&
                Objects.equals(oneshotOrCampaign, game.oneshotOrCampaign) &&
                Objects.equals(numberSessions, game.numberSessions) &&
                Objects.equals(sessionLength, game.sessionLength) &&
                Objects.equals(description, game.description);
    }

    @Override
    public int hashCode() {

        return Objects.hash(gmName, players, name, minPlayer, maxPayer, difficulty, universe, oneshotOrCampaign, numberSessions, sessionLength, description);
    }
}
