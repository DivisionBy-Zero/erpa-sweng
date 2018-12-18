package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import com.annimon.stream.Optional;

import java.util.UUID;

import ch.epfl.sweng.erpa.services.GameService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Game implements UuidObject {
    @android.support.annotation.NonNull @PrimaryKey @NonNull private String uuid = "";
    @ForeignKey(entity = UserProfile.class, parentColumns = "uuid", childColumns = "gm_user_uuid")
    @NonNull private String gmUserUuid;

    @ColumnInfo @NonNull private String title;
    @ColumnInfo(name = "min_players") @NonNull private Integer minPlayers;
    @ColumnInfo(name = "max_players") @NonNull private Integer maxPlayers;
    @ColumnInfo @NonNull private Difficulty difficulty;
    @ColumnInfo @NonNull private String universe;
    @ColumnInfo(name = "is_campaign") @NonNull private Boolean isCampaign;
    @ColumnInfo(name = "number_of_sessions") @NonNull private Optional<Integer> numberOfSessions;

    @ColumnInfo(name = "session_length_in_minutes") @NonNull
    private Optional<Integer> sessionLengthInMinutes;

    @ColumnInfo @NonNull private String description;
    @ColumnInfo(name = "location_lat") @NonNull private Double locationLat;
    @ColumnInfo(name = "location_lon") @NonNull private Double locationLon;
    @ColumnInfo(name = "game_status") @NonNull private GameStatus gameStatus;

    public String getOneshotOrCampaign() {
        return isCampaign ? "Campaign" : "Oneshot";
    }

    public enum Difficulty {NOOB, CHILL, HARD}

    public enum GameStatus {CREATED, CONFIRMED, CANCELED, IN_PROGRESS, FINISHED}

    public static String genGameUuid() {
        return GameService.UUID_PREFIX + UUID.randomUUID();
    }
}
