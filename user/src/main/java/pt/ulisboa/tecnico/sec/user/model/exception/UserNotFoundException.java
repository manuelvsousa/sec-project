package pt.ulisboa.tecnico.sec.user.model.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String user) {
        super("User " + user + " Not Found");
    }
}
