package ch.epfl.sweng.erpa.model;

import android.os.Bundle;

import com.annimon.stream.Optional;

import lombok.Data;
import lombok.NonNull;

@Data
public class MyAccountButton {

    @NonNull private String text;
    @NonNull private Class activityClass;
    @NonNull private Optional<Bundle> bundle;
    @NonNull private Boolean activeForPlayer;
    @NonNull private Boolean activeForGM;

}
