package pt.ulisboa.tecnico.sec.notary.model.exception;

public class GoodNotFoundException extends RuntimeException {
    public GoodNotFoundException(String message) {
        super("Good " + message + " Not Found");
    }
}
