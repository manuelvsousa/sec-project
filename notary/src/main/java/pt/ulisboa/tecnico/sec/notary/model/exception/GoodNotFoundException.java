package pt.ulisboa.tecnico.sec.notary.model.exception;

public class GoodNotFoundException extends RuntimeException {
    public GoodNotFoundException(String good) {
        super("Good " + good + " Not Found");
    }
}
