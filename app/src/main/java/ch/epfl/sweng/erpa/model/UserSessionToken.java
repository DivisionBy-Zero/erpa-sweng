package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(tableName = "user_session_token")
@ToString(exclude = "sessionToken")
public class UserSessionToken {
    @ForeignKey(entity = UserProfile.class, parentColumns = "uuid", childColumns = "user_uuid")
    @NonNull private String userUuid;

    @PrimaryKey @ColumnInfo(name = "session_token") @android.support.annotation.NonNull @NonNull private String sessionToken = "";
}
