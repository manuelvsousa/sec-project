package pt.ulisboa.tecnico.sec.model.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() { super("User Not Found"); }
}
