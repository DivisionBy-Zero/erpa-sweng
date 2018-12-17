package ch.epfl.sweng.erpa.operations;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.annotations.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class LoggedUser {
    UserSessionToken userSessionToken;
    UserProfile userProfile;
    Username username;

    @Service public UserSessionToken getUserSessionToken() {
        return userSessionToken;
    }

    @Service public UserProfile getUserProfile() {
        return userProfile;
    }

    @Service public Username getUsername() {
        return username;
    }
}
