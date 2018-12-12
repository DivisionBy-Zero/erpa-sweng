package ch.epfl.sweng.erpa.model;

import com.annimon.stream.Optional;

import java.util.UUID;

import ch.epfl.sweng.erpa.services.GameService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Game implements UuidObject {
    @NonNull private String uuid;
    @NonNull private String gmUserUuid;
    @NonNull private String title;
    @NonNull private Integer minPlayers;
    @NonNull private Integer maxPlayers;
    @NonNull private Difficulty difficulty;
    @NonNull private String universe;
    @NonNull private Boolean isCampaign;
    @NonNull private Optional<Integer> numberOfSessions;
    @NonNull private Optional<Integer> sessionLengthInMinutes;
    @NonNull private String description;
    @NonNull private Double locationLat;
    @NonNull private Double locationLon;
    @NonNull private GameStatus gameStatus;

    public String getOneshotOrCampaign() {
        return isCampaign ? "Campaign" : "Oneshot";
    }

    public enum Difficulty {NOOB, CHILL, HARD}
    public enum GameStatus {CREATED, CONFIRMED, CANCELED, IN_PROGRESS, FINISHED}

    public static String genGameUuid() {
        return GameService.UUID_PREFIX + UUID.randomUUID();
    }
}
