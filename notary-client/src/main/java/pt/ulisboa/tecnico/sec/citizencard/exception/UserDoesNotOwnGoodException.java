package pt.ulisboa.tecnico.sec.citizencard.exception;


public class UserDoesNotOwnGoodException extends RuntimeException {
    public UserDoesNotOwnGoodException(String message) {
        super(message);
    }
}
