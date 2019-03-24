package pt.ulisboa.tecnico.sec.notaryclient.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
