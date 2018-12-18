package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "user_profile")
public class UserProfile implements UuidObject {
    @PrimaryKey @android.support.annotation.NonNull @NonNull String uuid = "";
    @ColumnInfo(name = "is_gm") @NonNull private Boolean isGm;
    @ColumnInfo(name = "is_player") @NonNull private Boolean isPlayer;
    public enum Experience {Noob, Casual, Expert}
}
