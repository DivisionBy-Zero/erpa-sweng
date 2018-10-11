package ch.epfl.sweng.erpa.model;

import java.util.ArrayList;

public class UserAuth {

    private ArrayList<UserProfile> userList;

    public UserAuth() {
        userList = new ArrayList<>();
        userList.add(new UserProfile("admin", "admin"));
    }

    public boolean checkLogin(UserProfile user) {
        for(UserProfile u : userList) {
            if (user.equals(u))
                return true;
        }
        return false;
    }

    public boolean checkLogin(String name, String pass) {
        UserProfile toCheck = new UserProfile(name, pass);
        return checkLogin(toCheck);
    }
}
