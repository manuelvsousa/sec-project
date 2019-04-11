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

    public void setUserID(String userID){
        this.userID = userID;
    }

    public PrivateKey getPrivateKey(){
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey){
        this.privateKey = privateKey;
    }

}
