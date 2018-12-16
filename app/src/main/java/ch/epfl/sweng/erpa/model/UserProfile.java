package ch.epfl.sweng.erpa.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@RequiredArgsConstructor
public class UserProfile implements UuidObject {
    @NonNull String uuid;
    @NonNull private Boolean isGm;
    @NonNull private Boolean isPlayer;
    public enum Experience {Noob, Casual, Expert}
}
