package ch.epfl.sweng.erpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "sessionToken")
public class UserSessionToken {
    @NonNull private String userUuid;
    @NonNull private String sessionToken;
}
