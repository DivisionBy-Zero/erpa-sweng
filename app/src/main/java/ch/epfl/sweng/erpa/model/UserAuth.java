package ch.epfl.sweng.erpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
public class UserAuth {
    @NonNull private String userUuid;
    @ToString.Exclude private String sessionToken;
}
