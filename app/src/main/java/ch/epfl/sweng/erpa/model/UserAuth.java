package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserAuth {
    @ForeignKey(entity = UserProfile.class, parentColumns = "uuid", childColumns = "user_uuid")
    @NonNull private String userUuid;

    @PrimaryKey @ColumnInfo(name = "public_key")
    @android.support.annotation.NonNull @NonNull private String publicKey = "";
    @ColumnInfo(name = "authentication_strategy")
    @NonNull private String authenticationStrategy = "Grenouille";
}
