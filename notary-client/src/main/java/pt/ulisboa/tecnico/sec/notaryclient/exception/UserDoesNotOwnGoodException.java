package pt.ulisboa.tecnico.sec.notaryclient.exception;


public class UserDoesNotOwnGoodException extends RuntimeException {
    public UserDoesNotOwnGoodException(String message) {
        super(message);
    }
}
