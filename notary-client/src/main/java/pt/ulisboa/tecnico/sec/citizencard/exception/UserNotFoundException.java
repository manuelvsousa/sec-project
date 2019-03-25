package pt.ulisboa.tecnico.sec.citizencard.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
