package pt.ulisboa.tecnico.sec.user.jaxrs.application;

import pt.ulisboa.tecnico.sec.user.model.User;
import pt.ulisboa.tecnico.sec.user.model.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.security.PrivateKey;

public class UserServ {
    private static UserServ uniqueinstance;
    private List<User> users = new ArrayList<>(); //para guardar as keys
    private Map<String, PrivateKey> privateKeysperUser = new HashMap<String, PrivateKey>();


    private UserServ() {

    }

    public static UserServ getInstance() {
        if (uniqueinstance == null) {
            uniqueinstance = new UserServ();
        }
        return uniqueinstance;
    }
    public void addPrivateKey(String userID, PrivateKey privateKey){
        privateKeysperUser.put(userID, privateKey);
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

    public PrivateKey getPrivateKey(String userID){
        PrivateKey privateKey = privateKeysperUser.get(userID);
        if(privateKey != null){
            return privateKey;
        }
        throw new UserNotFoundException(userID);
    }

}
