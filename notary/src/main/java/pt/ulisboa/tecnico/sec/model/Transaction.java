package pt.ulisboa.tecnico.sec.model;

import java.io.Serializable;
import java.util.Date;

//@XmlRootElement       //only needed if we also want to generate XML
public class Transaction implements Serializable {

    private Good good;
    private User seller;
    private User buyer;

    public Transaction(Good good, User seller, User buyer) {
        this.good = good;
        this.seller = seller;
        this.buyer = buyer;
    }

    public Good getGood() {
        return good;
    }

    public User getSeller() {
        return seller;
    }

    public User getBuyer() {
        return buyer;
    }
}
