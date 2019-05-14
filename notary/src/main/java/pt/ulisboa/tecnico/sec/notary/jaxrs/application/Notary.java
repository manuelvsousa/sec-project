package pt.ulisboa.tecnico.sec.notary.jaxrs.application;

import pt.ulisboa.tecnico.sec.notary.model.*;
import pt.ulisboa.tecnico.sec.notary.model.exception.*;
import pt.ulisboa.tecnico.sec.notary.util.Checker;
import pt.ulisboa.tecnico.sec.notary.util.CitizenCard;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyGen;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.*;

public class Notary implements Serializable {
    private final static String SERIALIZE_FILE_NAME = "notary";
    private final static String SERIALIZE_FILE_EXTENSION = ".ser";
    private final static int F = 1;
    private final static  int N = 4 * F;

    private static Notary uniqueInstance;

    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private HashMap<String, Write> writeRegister = new HashMap<String, Write>();
    private transient KeyPair keys;
    private transient String publicKeySignature;
    private transient boolean withCC = false;

    private Notary() {
        try {
            this.keys = KeyGen.getInstance().generateRSAKey();
        } catch (Exception asd) {

        }
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
            ObjectOutput out;
            String saveFilename = SERIALIZE_FILE_NAME + System.getProperty("port") + SERIALIZE_FILE_EXTENSION;
            out = new ObjectOutputStream(new FileOutputStream(saveFilename));
            out.writeObject(notary);
            out.close();

            System.out.println("Object has been serialized");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void setWithCC(boolean withCC) {
        this.withCC = withCC;
        if (withCC) {
            if (this.publicKeySignature == null) {
                this.publicKeySignature = Crypto.getInstance().byteToHex(CitizenCard.getInstance().sign(this.keys.getPublic().getEncoded()));
            }
        }
    }

    public String getPublicKeySignature() {
        return this.publicKeySignature;
    }

    public PublicKey getPublicKey() {
            return this.keys.getPublic();
    }

    public String sign(byte[] toSign, boolean withCC) throws Exception {
        if (withCC && this.withCC) {
            System.out.println("Going to sign with Citizen Card");
            return Crypto.getInstance().byteToHex(CitizenCard.getInstance().sign(toSign));
        }
        System.out.println("Going to sign with autogenerated privateKey");
        return Crypto.getInstance().sign(this.keys.getPrivate(), toSign);
    }

    public void addUser(User u) {
        users.add(u);
        this.save();
    }

    public synchronized void addTransaction(String goodID, String buyerID, String sellerID, String time, String signWrite, String nonceBuyer) {
        for (Transaction t : this.transactions) {
            if (t.getGood().getID().equals(goodID) && t.getBuyer().getID().equals(buyerID) && t.getSeller().getID().equals(sellerID) && t.getTime().equals(time)) {
                throw new TransactionAlreadyExistsException(goodID, buyerID, sellerID);
            }
        }
        Good g = this.getGood(goodID);
        User buyer = this.getUser(buyerID);
        User seller = this.getUser(sellerID);
        if (!this.getUser(sellerID).getGoods().contains(this.getGood(goodID))) {
            throw new UserDoesNotOwnGood(sellerID, goodID);
        }
        if (!g.onSale()) {
            throw new GoodNotOnSale(goodID);
        }

        Checker.getInstance().checkSW(goodID, buyerID, nonceBuyer, false, signWrite);

        Write write = new Write(buyerID, goodID, Long.valueOf(nonceBuyer).longValue(), false, signWrite);

        this.writeRegister.put(nonceBuyer, write);


        if(Long.valueOf(time).longValue() > this.getGood(goodID).getTimestamp()) {
            seller.removeGood(g);
            g.setOnSale(false);
            g.setTimestamp(Long.valueOf(nonceBuyer).longValue());
            g.setSignWrite(signWrite);
            buyer.addGood(g);
            transactions.add(new Transaction(this.getGood(goodID), this.getUser(sellerID), this.getUser(buyerID), time));
            this.save();
        }
    }

    public void doIntegrityCheck(String goodID, String buyerID, String sellerID) {
        Good g = this.getGood(goodID);
        User buyer = this.getUser(buyerID);
        User seller = this.getUser(sellerID);
        if (sellerID.equals(buyerID)) {
            throw new InvalidTransactionException("Buyer and Seller cant be the same");
        }
        if (!this.getUser(sellerID).getGoods().contains(this.getGood(goodID))) {
            throw new UserDoesNotOwnGood(sellerID, goodID);
        }
        if (!g.onSale()) {
            throw new GoodNotOnSale(goodID);
        }
    }

    public synchronized void setIntentionToSell(String goodID, String sellerID, String nonce, String sigWrite) {
        if (!this.getUser(sellerID).getGoods().contains(this.getGood(goodID))) {
            throw new UserDoesNotOwnGood(sellerID, goodID);
        }
        Checker.getInstance().checkSW(goodID, sellerID, nonce, true, sigWrite);

        if(Long.valueOf(nonce).longValue() > this.getGood(goodID).getTimestamp()) {
            this.getGood(goodID).setOnSale(true);
            this.getGood(goodID).setTimestamp(Long.valueOf(nonce).longValue());
            this.getGood(goodID).setSignWrite(sigWrite);
        }
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

    public synchronized State getStateOfGood(String goodID) {

        for (User u : users) {
            for (Good g : u.getGoods()) {
                if (g.getID().equals(goodID)) {
                    return new State(u.getID(), g.onSale(), g.getTimestamp(), g.getSignWrite());
                }
            }
        }
        throw new GoodNotFoundException(goodID);

    }

    protected Object readResolve() {
        return getInstance();
    }


    public  boolean verifyPOW(String hashCash, String userID, String nonce) {
        System.out.println("Received Proof of work: " + hashCash);
        String[] parts = hashCash.split(":");
        String[] calculatedStringPow = parts[3].split("\\|\\|");
        if(!calculatedStringPow[0].equals(nonce)){
            return false;
        }
        if(!calculatedStringPow[1].equals(userID)){
            return false;
        }
        String sha1 = new String();
        try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(hashCash.getBytes());
            sha1 = Crypto.getInstance().byteToHex(md.digest());
            System.out.println("Received Proof of work SHA1: " + sha1);
        }catch (Exception e){

        }

        if(!sha1.substring(0,5).equals("00000")){
            return false;
        }
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");

        if(!dateFormat.format(now.getTime()).equals(parts[2])){
            return false;
        }
        if(getUser(userID).inPows(sha1)){
            return false;
        } else {
            getUser(userID).addPow(sha1);
        }
        System.out.println("Proof of work is Valid!");
        return true;
    }


    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, GeneralSecurityException {
        ois.defaultReadObject();
        uniqueInstance = this;
//        this.keys = KeyGen.getInstance().generateRSAKey();
//        setWithCC(true);
    }


    public boolean validateWrite(Write write) {
        //colocar thread em sleep???
        //send write for everyone + signature

        //check responses se >(N+f)/2 - 1, fazer write
        return true;
    }
}