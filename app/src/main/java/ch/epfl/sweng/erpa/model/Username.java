package ch.epfl.sweng.erpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Username {
    @NonNull private String userUuid;
    @NonNull private String username;
}
