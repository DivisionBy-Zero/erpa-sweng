package ch.epfl.sweng.erpa.model;

import java.util.Objects;

public class UserProfileDTO implements UserProfile {
    private Experience xp;
    private String accessToken;
    private String uid;
    private String username;
    private boolean isGm;
    private boolean isPlayer;

    public UserProfileDTO(){}

    public UserProfileDTO(Experience xp, String accessToken, String uid, String username, boolean isGm, boolean isPlayer) {
        this.xp = xp;
        this.accessToken = accessToken;
        this.uid = uid;
        this.username = username;
        this.isGm = isGm;
        this.isPlayer = isPlayer;
    }

    @Override public Experience getXp() {
        return xp;
    }

    @Override public void setXp(Experience xp) {
        this.xp = xp;
    }

    @Override public String getAccessToken() {
        return accessToken;
    }

    @Override public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override public String getUid() {
        return uid;
    }

    @Override public void setUid(String uid) {
        this.uid = uid;
    }

    @Override public String getUsername() {
        return username;
    }

    @Override public void setUsername(String username) {
        this.username = username;
    }

    @Override public boolean isGM() {
        return isGm;
    }

    @Override public void setGM(boolean gm) {
        isGm = gm;
    }

    @Override public boolean isPlayer() {
        return isPlayer;
    }

    @Override public void setPlayer(boolean player) {
        isPlayer = player;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfileDTO that = (UserProfileDTO) o;
        return isGm == that.isGm &&
                isPlayer == that.isPlayer &&
                xp == that.xp &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(username, that.username);
    }

    @Override public int hashCode() {
        return Objects.hash(xp, accessToken, uid, username, isGm, isPlayer);
    }
}
