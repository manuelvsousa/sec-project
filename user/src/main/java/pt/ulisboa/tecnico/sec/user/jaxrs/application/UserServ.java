package pt.ulisboa.tecnico.sec.user.jaxrs.application;

import pt.ulisboa.tecnico.sec.user.model.User;
import pt.ulisboa.tecnico.sec.user.model.exception.UserNotFoundException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserServ {
    private List<User> users = new ArrayList<>(); //para guardar as keys
    private static UserServ uniqueinstance;


    private UserServ() {

    }

    public static UserServ getInstance() {
        if(uniqueinstance == null) {
            uniqueinstance = new UserServ();
        }
        return uniqueinstance;
    }

    public void addUser(User u) {
        users.add(u);
    }

    public User getUser(String userID) {
        for (User u : users) {
            if (u.getUserID().equals(userID)) {
                return u;
            }
        }
        throw new UserNotFoundException(userID);
    }

}
