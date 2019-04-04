package pt.ulisboa.tecnico.sec.user.model;

public class User {
    private String userID;
    private String publicKey;

    public User(String userID, String publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getPublicKey() {
        return this.publicKey;
    }
}
