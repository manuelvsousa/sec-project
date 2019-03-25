package pt.ulisboa.tecnico.sec.citizencard.exception;


public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
