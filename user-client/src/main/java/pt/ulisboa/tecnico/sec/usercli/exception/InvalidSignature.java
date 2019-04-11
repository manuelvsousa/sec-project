package pt.ulisboa.tecnico.sec.usercli.exception;


public class InvalidSignature extends RuntimeException {
    public InvalidSignature(String message) {
        super(message);
    }
}
