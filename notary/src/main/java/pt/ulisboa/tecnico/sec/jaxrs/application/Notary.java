package pt.ulisboa.tecnico.sec.jaxrs.application;

import pt.ulisboa.tecnico.sec.model.Good;
import pt.ulisboa.tecnico.sec.model.Status;
import pt.ulisboa.tecnico.sec.model.Transaction;
import pt.ulisboa.tecnico.sec.model.User;
import pt.ulisboa.tecnico.sec.model.exception.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Notary implements Serializable {
    private final static String SERIALIZE_FILE_NAME = "notary.ser";

    private static Notary uniqueInstance;

    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    private Notary() {
    }

    public static Notary getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Notary();
        }
        return uniqueInstance;
    }

    public void addUser(User u) {
        users.add(u);
        this.save();
    }

    public void addTransaction(String goodID, String buyerID, String sellerID) {
        if (sellerID.equals(buyerID)) {
            throw new InvalidTransactionException("Buyer and Seller cant be the same");
        }
        transactions.add(new Transaction(this.getGood(goodID), this.getUser(buyerID), this.getUser(sellerID)));
        this.save();
    }

    public void setIntentionToSell(String goodID, String sellerID) {
        for (Good g : this.getUser(sellerID).getGoods()) {
            if (g.getID().equals(goodID)) {
                g.setOnSale(true);
                this.save();
                return;
            }
        }
        throw new UserDoesNotOwnGood();
    }

    private Good getGood(String id) {
        for (User u : users) {
            for (Good g : u.getGoods()) {
                if (g.getID().equals(id)) {
                    return g;
                }
            }
        }
        throw new GoodNotFoundException();
    }

    private User getUser(String id) {
        for (User u : users) {
            if (u.getID().equals(id)) {
                return u;
            }
        }
        throw new UserNotFoundException();
    }


    public Status getGoodStatus(String goodID) {

        for (User u : users) {
            for (Good g : u.getGoods()) {
                if (g.getID().equals(goodID)) {
                    return new Status(u, g.onSale());
                }
            }
        }
        throw new GroupNotFoundException();

    }

    protected Object readResolve() {
        return getInstance();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        uniqueInstance = this;
    }

    public static void save() {

        try {
            Notary notary = Notary.getInstance();
            ObjectOutput out = null;

            out = new ObjectOutputStream(new FileOutputStream(SERIALIZE_FILE_NAME));
            out.writeObject(notary);
            out.close();

            System.out.println("Object has been serialized");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}