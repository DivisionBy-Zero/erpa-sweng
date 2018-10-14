package ch.epfl.sweng.erpa.model;

import lombok.NonNull;

public class MyAccountButtons {

    @NonNull private String text;
    @NonNull private Class activityClass;
    @NonNull private boolean activeForPlayer;
    @NonNull private boolean activeForGM;

}
