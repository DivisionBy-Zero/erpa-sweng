package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.ForeignKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuth {
    @ForeignKey(entity = UserProfile.class, parentColumns = "uuid", childColumns = "user_uuid")
    @NonNull private String userUuid;

    @ColumnInfo(name = "public_key")
    @NonNull private String publicKey;
    @ColumnInfo(name = "authentication_strategy")
    @NonNull private String authenticationStrategy = "Grenouille";
}
