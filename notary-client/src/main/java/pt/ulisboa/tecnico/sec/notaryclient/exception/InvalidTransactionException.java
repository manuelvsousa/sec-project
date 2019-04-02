package pt.ulisboa.tecnico.sec.notaryclient.exception;


public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
