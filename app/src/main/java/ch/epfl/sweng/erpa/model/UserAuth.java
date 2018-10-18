package ch.epfl.sweng.erpa.model;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
public class UserAuth {
    @NonNull private String uid;
    @NonNull @ToString.Exclude private String accessToken;
}
