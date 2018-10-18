package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;
import com.annimon.stream.Optional;

import java.util.Objects;
import lombok.Data;

@Entity @Data
public class Game implements Serializable
{

    public Game(){}

    @PrimaryKey
    @NonNull
    private String gid;
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

    public enum Difficulty {
        NOOB, CHILL, HARD
    }

    public enum OneshotOrCampaign {
        ONESHOT, CAMPAIGN
    }
}
