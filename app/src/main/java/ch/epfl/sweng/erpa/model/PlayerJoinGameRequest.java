package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PlayerJoinGameRequest {
    @PrimaryKey @NonNull private String joinRequestId;
    @ColumnInfo(name = "request_status") @NonNull private RequestStatus requestStatus;
    @ForeignKey(entity = Game.class, parentColumns = "uuid", childColumns = "game_uuid")
    @NonNull private String gameUuid;

    @ForeignKey(entity = UserProfile.class, parentColumns = "uuid", childColumns = "user_uuid")
    @NonNull private String userUuid;

    public enum RequestStatus {REQUEST_TO_JOIN, CONFIRMED, REJECTED, REMOVED, HAS_QUIT}
}
