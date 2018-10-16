package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import com.annimon.stream.Optional;

import java.util.Objects;

@Entity
public class Game implements Serializable
{

    public Game(){}

    @PrimaryKey
    @NonNull
    private String gid;

    public String getGmUniqueID() { return gmUniqueID; }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public void setGmUniqueID(String gmUniqueID) {
        this.gmUniqueID = gmUniqueID;
    }

    public GameParticipants getPlayers() {
        return players;
    }

    public void setPlayers(GameParticipants players) {
        this.players = players;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinPlayer(int minPlayer) {
        this.minPlayer = minPlayer;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setUniverse(String universe) {
        this.universe = universe;
    }

    public void setOneshotOrCampaign(OneshotOrCampaign oneshotOrCampaign) {
        this.oneshotOrCampaign = oneshotOrCampaign;
    }

    public void setNumberSessions(Optional<Integer> numberSessions) {
        this.numberSessions = numberSessions;
    }

    public Optional<Integer> getSessionLengthInMinutes() {
        return sessionLengthInMinutes;
    }

    public void setSessionLengthInMinutes(Optional<Integer> sessionLengthInMinutes) {
        this.sessionLengthInMinutes = sessionLengthInMinutes;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String gmUniqueID;
    private GameParticipants players;
    private String name;
    private int minPlayer;
    private int maxPlayer;
    private Difficulty difficulty;
    private String universe;
    private OneshotOrCampaign oneshotOrCampaign;
    private Optional<Integer> numberSessions;
    private Optional<Integer> sessionLengthInMinutes;
    private String description;

    public Game(String gmUniqueID, String name, int minPlayer,
                int maxPayer, Difficulty difficulty, String universe,
                OneshotOrCampaign oneshotOrCampaign, Optional<Integer> numberSessions,
                Optional<Integer> sessionLengthInMinutes, String description, String gid) {
        this.gmUniqueID = gmUniqueID;
        this.name = name;
        this.minPlayer = minPlayer;
        this.maxPlayer = maxPayer;
        this.difficulty = difficulty;
        this.universe = universe;
        this.oneshotOrCampaign = oneshotOrCampaign;
        this.numberSessions = numberSessions;
        this.sessionLengthInMinutes = sessionLengthInMinutes;
        this.description = description;
        this.gid = gid;
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

    public Optional<Integer> getSessionLengthInMinutes() {
        return sessionLengthInMinutes;
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
                ", sessionLength='" + sessionLengthInMinutes + '\'' +
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
                Objects.equals(sessionLengthInMinutes, game.sessionLengthInMinutes) &&
                Objects.equals(description, game.description);
    }

    @Override
    public int hashCode() {

        return Objects.hash(gmUniqueID, players, name, minPlayer, maxPlayer, difficulty, universe, oneshotOrCampaign, numberSessions, sessionLengthInMinutes, description);
    }

    public enum Difficulty {
        NOOB, CHILL, HARD
    }

    public enum OneshotOrCampaign {
        ONESHOT, CAMPAIGN
    }
}
