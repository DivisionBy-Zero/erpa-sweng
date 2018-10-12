package ch.epfl.sweng.erpa.model;

import java.util.Objects;

public class UserAuth {

    private String uid;
    private String accessToken;

    public UserAuth(String uid, String accessToken) {
        this.uid = uid.toUpperCase();
        this.accessToken = accessToken;
    }

    public String getuid() {
        return uid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuth userAuth = (UserAuth) o;
        return Objects.equals(uid, userAuth.uid) &&
                Objects.equals(accessToken, userAuth.accessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, accessToken);
    }

    @Override
    public String toString() {
        return "UserAuth{" +
                "uid='" + uid + '\'' +
                ", accessToken=REDACTED}";
    }
}
