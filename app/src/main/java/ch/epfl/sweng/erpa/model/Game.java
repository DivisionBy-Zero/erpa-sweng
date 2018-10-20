package ch.epfl.sweng.erpa.model;

import com.annimon.stream.Optional;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Game {
    public enum Difficulty {NOOB, CHILL, HARD}

    public enum OneshotOrCampaign {ONESHOT, CAMPAIGN}

    @NonNull private String gameUuid;
    @NonNull private Set<String> playersUuid;
    @NonNull private String name;
    @NonNull private Integer minPlayer;
    @NonNull private Integer maxPlayer;
    @NonNull private Difficulty difficulty;
    @NonNull private String universe;
    @NonNull private OneshotOrCampaign oneshotOrCampaign;
    @NonNull private Optional<Integer> numberSessions;
    @NonNull private Optional<Integer> sessionLengthInMinutes;
    @NonNull private String description;

    public Game withPlayer(String newPlayerUuid){
        HashSet<String> newPlayerSet = new HashSet<>(playersUuid);
        newPlayerSet.add(newPlayerUuid);

        return this.toBuilder().playersUuid(newPlayerSet).build();
    }

    public Game removePlayer(String playerToRemove){
        HashSet<String> newPlayerSet = new HashSet<>(playersUuid);
        newPlayerSet.remove(playerToRemove);

        return this.toBuilder().playersUuid(newPlayerSet).build();
    }
}
