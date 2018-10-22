package ch.epfl.sweng.erpa.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserProfile implements UuidObject {
    @Override
    public String getUuid() {
        return getUid();
    }

    public enum Experience {Noob, Casual, Expert};

    @NonNull private final String uid;
    @NonNull private String username;
    @NonNull private String accessToken;
    @NonNull private Experience xp;
    @NonNull private Boolean isGm;
    @NonNull private Boolean isPlayer;
}
