package pt.ulisboa.tecnico.sec.notary.model.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User Not Found");
    }
}
