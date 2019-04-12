package pt.ulisboa.tecnico.sec.notary.model.exception;

public class GoodNotOnSale extends RuntimeException {
    public GoodNotOnSale(String good) {
        super("Good " + good + " Not on Sale");
    }
}
