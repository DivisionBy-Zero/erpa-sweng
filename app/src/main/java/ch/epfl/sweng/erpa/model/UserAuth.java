package ch.epfl.sweng.erpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuth {
    @NonNull private String userUuid;
    @NonNull private String publicKey;
    @NonNull private String authenticationStrategy = "Grenouille";
}
