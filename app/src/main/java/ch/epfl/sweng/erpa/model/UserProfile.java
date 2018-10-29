package ch.epfl.sweng.erpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile implements UuidObject {
    @Override
    public String getUuid() {
        return getUserUuid();
    }

    public enum Experience {Noob, Casual, Expert};

    @NonNull private String userUuid;
    @NonNull private String username;
    @NonNull private String accessToken;
    @NonNull private Experience xp;
    @NonNull private Boolean isGm;
    @NonNull private Boolean isPlayer;
}
