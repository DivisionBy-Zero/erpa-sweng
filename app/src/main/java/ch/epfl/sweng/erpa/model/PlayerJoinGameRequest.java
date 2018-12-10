package ch.epfl.sweng.erpa.model;

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
    @NonNull private String joinRequestId;
    @NonNull private RequestStatus requestStatus;
    @NonNull private String gameUuid;
    @NonNull private String userUuid;

    public enum RequestStatus {REQUEST_TO_JOIN, CONFIRMED, REJECTED, REMOVED, HAS_QUIT}
}
