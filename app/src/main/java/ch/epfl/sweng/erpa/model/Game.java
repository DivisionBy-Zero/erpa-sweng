package ch.epfl.sweng.erpa.model;

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

    public Game(String gmName, String name, String minPlayer,
                String maxPayer, String difficulty,
                String universe, String oneshotOrCampaign,
                String sessionLength, String description) {
        this.gmName = gmName;
        this.name = name;
        this.minPlayer = minPlayer;
        this.maxPayer = maxPayer;
        this.difficulty = difficulty;
        this.universe = universe;
        this.oneshotOrCampaign = oneshotOrCampaign;
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

}
