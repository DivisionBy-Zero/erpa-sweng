package ch.epfl.sweng.erpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class UserProfile implements UuidObject {
    public enum Experience {Noob, Casual, Expert}

    @NonNull String uuid;
    private transient String username;
    private transient String accessToken;
    private transient Experience xp;
    @NonNull private Boolean isGm;
    @NonNull private Boolean isPlayer;
}
