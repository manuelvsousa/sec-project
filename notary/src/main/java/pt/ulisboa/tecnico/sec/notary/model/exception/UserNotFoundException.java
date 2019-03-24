package pt.ulisboa.tecnico.sec.notary.model.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String user) {
        super("User " + user + " Not Found");
    }
}
