package ch.epfl.sweng.erpa.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class MyAccountButton {

    @NonNull private String text;
    @NonNull private Class activityClass;
    @NonNull private boolean activeForPlayer;
    @NonNull private boolean activeForGM;

}
