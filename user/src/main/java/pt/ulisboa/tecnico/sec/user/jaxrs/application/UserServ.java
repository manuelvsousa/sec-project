package pt.ulisboa.tecnico.sec.user.jaxrs.application;

import java.security.PrivateKey;

public class UserServ {
    private static UserServ uniqueinstance;
    private String userID;
    private PrivateKey privateKey;


    private UserServ() {

    }

    public static UserServ getInstance() {
        if (uniqueinstance == null) {
            uniqueinstance = new UserServ();
        }
        return uniqueinstance;
    }

    public void addPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getUserID(String userID) {
        return userID;
    }

    public PrivateKey getPrivateKey(String userID) {
        return privateKey;
    }

}
