package ch.epfl.sweng.erpa.model;

import android.arch.persistence.room.ColumnInfo;
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
@RequiredArgsConstructor
public class UserProfile implements UuidObject {
    @PrimaryKey @NonNull String uuid;
    @ColumnInfo(name = "is_gm") @NonNull private Boolean isGm;
    @ColumnInfo(name = "is_player") @NonNull private Boolean isPlayer;
    public enum Experience {Noob, Casual, Expert}
