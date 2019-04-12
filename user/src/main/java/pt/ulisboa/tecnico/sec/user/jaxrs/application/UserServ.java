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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

}
