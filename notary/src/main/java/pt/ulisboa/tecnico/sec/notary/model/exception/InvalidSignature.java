package pt.ulisboa.tecnico.sec.notary.model.exception;


public class InvalidSignature extends RuntimeException {
    public InvalidSignature(String message) {
        super(message);
    }
}
