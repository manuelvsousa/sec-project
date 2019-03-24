package pt.ulisboa.tecnico.sec.notary.model.exception;

public class UserDoesNotOwnGood extends RuntimeException {
    public UserDoesNotOwnGood(String user, String good) {
        super("User: " + user + " does not own good: " + good);
    }
}
