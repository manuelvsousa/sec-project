package pt.ulisboa.tecnico.sec.model.exception;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException() { super("Invalid Transaction"); }
    public InvalidTransactionException(String message) { super("Invalid Transaction: " + message ); }
}
