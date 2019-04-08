package pt.ulisboa.tecnico.sec.notaryclient.exception;


public class InvalidSignature extends RuntimeException {
    public InvalidSignature(String message) {
        super(message);
    }
}
