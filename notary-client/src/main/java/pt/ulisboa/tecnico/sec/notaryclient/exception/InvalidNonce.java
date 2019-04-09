package pt.ulisboa.tecnico.sec.notaryclient.exception;


public class InvalidNonce extends RuntimeException {
    public InvalidNonce(String message) {
        super(message);
    }
}
