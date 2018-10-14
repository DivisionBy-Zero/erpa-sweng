package ch.epfl.sweng.erpa.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class UserProfile {
    public enum Experience {Noob, Casual, Expert}

    @NonNull private final String uid;
    @NonNull private String username;
    @NonNull private String accessToken;
    @NonNull private Experience xp;
    @NonNull private Boolean isGm;
    @NonNull private Boolean isPlayer;

    public UserProfile(String uid, String username, String accessToken, Experience xp, boolean isGm, boolean isPlayer) {
        this.username = username;
        this.accessToken = accessToken;
        this.uid = uid;
        this.xp = xp;
        this.isGm = isGm;
        this.isPlayer = isPlayer;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Experience getXp() {
        return xp;
    }

    public boolean isGm() {
        return isGm;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.accessToken = password;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setXp(Experience xp) {
        this.xp = xp;
    }

    public void setGm(boolean gm) {
        isGm = gm;
    }

    public void setPlayer(boolean player) {
        isPlayer = player;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return isGm == that.isGm &&
                isPlayer == that.isPlayer &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(username, that.username) &&
                Objects.equals(accessToken, that.accessToken) &&
                xp == that.xp;
    }

    @Override public int hashCode() {
        return Objects.hash(uid, username, accessToken, xp, isGm, isPlayer);
    }
}
