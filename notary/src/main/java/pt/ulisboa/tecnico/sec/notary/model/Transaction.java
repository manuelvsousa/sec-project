package pt.ulisboa.tecnico.sec.notary.model;

import java.io.Serializable;

public class Transaction implements Serializable {

    private Good good;
    private User seller;
    private User buyer;
    private String time;

    public Transaction(Good good, User seller, User buyer, String time) {
        this.good = good;
        this.seller = seller;
        this.buyer = buyer;
        this.time = time;
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

    public String getTime() {
        return time;
    }
}
