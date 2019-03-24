package pt.ulisboa.tecnico.sec.model.exception;

public class UserDoesNotOwnGood extends RuntimeException {
    public UserDoesNotOwnGood() { super("User Does Not own Good"); }
}
