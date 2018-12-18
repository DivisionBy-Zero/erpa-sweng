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
public class Username {
    @ForeignKey(entity = UserProfile.class, parentColumns = "uuid", childColumns = "user_uuid")
    @ColumnInfo(name = "user_uuid")
    @NonNull private String userUuid;
    @ColumnInfo
    @android.support.annotation.NonNull @PrimaryKey @NonNull private String username = "";
}
