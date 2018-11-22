package ch.epfl.sweng.erpa.model;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@ToString(exclude = "accessToken")
public class UserAuth {
    @NonNull private String uid;
    @NonNull private String accessToken;
}
