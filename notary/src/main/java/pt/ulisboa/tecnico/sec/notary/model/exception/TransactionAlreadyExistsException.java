package pt.ulisboa.tecnico.sec.notary.model.exception;

public class TransactionAlreadyExistsException extends RuntimeException {
    public TransactionAlreadyExistsException(String good, String buyer, String seller) {
        super("Transaction with buyer " + buyer + " with seller " + seller + " selling good " + good + " already exists");
    }
}
