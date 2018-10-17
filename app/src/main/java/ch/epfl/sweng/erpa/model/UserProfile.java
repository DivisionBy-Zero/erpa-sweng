package ch.epfl.sweng.erpa.model;

import java.util.Objects;

public class UserProfile {

    public enum Experience {Noob, Casual, Expert};

    private final String uid;
    private String username;
    private String accessToken;
    private Experience xp;
    private boolean isGm;
    private boolean isPlayer;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile user = (UserProfile) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(accessToken, user.accessToken);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username, accessToken);
    }

    @Override
    public String toString() {
        return accessToken;
    }
}
