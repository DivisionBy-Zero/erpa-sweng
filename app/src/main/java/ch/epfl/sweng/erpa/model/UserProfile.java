package ch.epfl.sweng.erpa.model;

import java.util.Objects;

public class UserProfile {

    private String username;
    private String accessToken;

    public UserProfile(String username, String password) {
        this.username = username;
        this.accessToken = password;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.accessToken = password;
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
