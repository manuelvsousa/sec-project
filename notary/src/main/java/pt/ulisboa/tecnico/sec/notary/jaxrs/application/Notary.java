package pt.ulisboa.tecnico.sec.notary.jaxrs.application;

import pt.ulisboa.tecnico.sec.notary.model.Good;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.Transaction;
import pt.ulisboa.tecnico.sec.notary.model.User;
import pt.ulisboa.tecnico.sec.notary.model.exception.*;
import pt.ulisboa.tecnico.sec.notary.util.CitizenCard;
import pt.ulisboa.tecnico.sec.util.Crypto;

import java.io.*;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

public class Notary implements Serializable {
    private final static String SERIALIZE_FILE_NAME = "notary.ser";

    private static Notary uniqueInstance;

    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private PrivateKey privatekey;

    private Notary() {
    }

    public static Notary getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Notary();
        }
        return uniqueInstance;
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

    public PrivateKey getPrivateKey() {
        if (this.privatekey == null) {
            throw new NullPointerException("Private Key is null");
        }
        return this.privatekey;
    }

    public void setPrivateKey(PrivateKey privatekey) {
        this.privatekey = privatekey;
    }

    public String sign(byte[] toSign) throws Exception {
        if (this.privatekey == null) {
            System.out.println("Going to sign with Citizen Card");
            return Crypto.getInstance().byteToHex(CitizenCard.getInstance().sign(toSign));
        }
        System.out.println("Going to sign with privateKey");
        return Crypto.getInstance().sign(this.getPrivateKey(), toSign);
    }

    public void addUser(User u) {
        users.add(u);
        this.save();
    }

    public void addTransaction(String goodID, String buyerID, String sellerID, String time) {
        if (sellerID.equals(buyerID)) {
            throw new InvalidTransactionException("Buyer and Seller cant be the same");
        }
        for (Transaction t : this.transactions) {
            if (t.getGood().getID().equals(goodID) && t.getBuyer().getID().equals(buyerID) && t.getSeller().getID().equals(sellerID) && t.getTime().equals(time)) {
                throw new TransactionAlreadyExistsException(goodID, buyerID, sellerID);
            }
        }
        Good g = this.getGood(goodID);
        User buyer = this.getUser(buyerID);
        User seller = this.getUser(sellerID);
        seller.removeGood(g);
        g.setOnSale(false);
        buyer.addGood(g);
        transactions.add(new Transaction(this.getGood(goodID), this.getUser(sellerID), this.getUser(buyerID),time));
        this.save();
    }

    public void setIntentionToSell(String goodID, String sellerID) {
        if (!this.getUser(sellerID).getGoods().contains(this.getGood(goodID))) {
            throw new UserDoesNotOwnGood(sellerID, goodID);
        }
        this.getGood(goodID).setOnSale(true);
    }

    private Good getGood(String goodID) {
        for (User u : users) {
            for (Good g : u.getGoods()) {
                if (g.getID().equals(goodID)) {
                    return g;
                }
            }
        }
        throw new GoodNotFoundException(goodID);
    }

    public User getUser(String userID) {
        for (User u : users) {
            if (u.getID().equals(userID)) {
                return u;
            }
        }
        throw new UserNotFoundException(userID);
    }

    public State getStateOfGood(String goodID) {

        for (User u : users) {
            for (Good g : u.getGoods()) {
                if (g.getID().equals(goodID)) {
                    return new State(u.getID(), g.onSale());
                }
            }
        }
        throw new GoodNotFoundException(goodID);

    }

    protected Object readResolve() {
        return getInstance();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        uniqueInstance = this;
    }

}