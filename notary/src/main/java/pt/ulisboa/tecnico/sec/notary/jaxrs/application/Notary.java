package pt.ulisboa.tecnico.sec.notary.jaxrs.application;

import org.apache.commons.io.FileUtils;
import pt.ulisboa.tecnico.sec.notary.model.*;
import pt.ulisboa.tecnico.sec.notary.model.exception.*;
import pt.ulisboa.tecnico.sec.notary.util.Checker;
import pt.ulisboa.tecnico.sec.notary.util.CitizenCard;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyGen;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Notary implements Serializable {
    private final static String SERIALIZE_FILE_NAME = "notary";
    private final static String SERIALIZE_FILE_EXTENSION = ".ser";
    private final static long SESSION = TimeUnit.SECONDS.toMillis(5);
    private final static int F = 1;
    private final static  int N = 3 * F + 1;
    private final Lock lock = new ReentrantLock();

    private static Notary uniqueInstance;

    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private HashMap<Integer, BRB> brbs = new HashMap<>();
    private int lastValidWrite = 0;
    private Client client = ClientBuilder.newClient();

    private transient KeyPair keys;
    private transient String publicKeySignature;
    private transient boolean withCC = false;
    private transient HashMap<Integer, PublicKey> notarySignedPublicKeys = new HashMap<>();

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
        String saveFilename = SERIALIZE_FILE_NAME + System.getProperty("port") + SERIALIZE_FILE_EXTENSION;
        String savebackupFileName = SERIALIZE_FILE_NAME + System.getProperty("port") + "BACKUP" + SERIALIZE_FILE_EXTENSION;
        try {
            Notary notary = Notary.getInstance();
            ObjectOutput out;
            out = new ObjectOutputStream(new FileOutputStream(saveFilename));
            out.writeObject(notary);
            out.close();

            System.out.println("Object has been serialized");
        } catch (IOException ioe) {
            File source = new File(saveFilename);
            File backup = new File(savebackupFileName);
            if(backup.exists() && !backup.isDirectory()) {
                if(source.exists() && !source.isDirectory()) {
                    source.delete();
                }
                try {
                    FileUtils.copyFile(backup, source);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new RuntimeException("Backup file was never generated");
            }
        } finally {
            File backup = new File(savebackupFileName);
            if(backup.exists() && !backup.isDirectory()) {
                backup.delete();
            }
            File source = new File(saveFilename);
            try {
                FileUtils.copyFile(source, backup);
            } catch (IOException e) {
                e.printStackTrace();
            }

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

    public synchronized void setStateOfGood(String goodID, String sellerID, Boolean onSale, String nonce, String sigWrite){
        Checker.getInstance().checkSW(goodID, sellerID, nonce, onSale, sigWrite);
        if(Long.valueOf(nonce) > this.getGood(goodID).getTimestamp()) {
            if (!this.getUser(sellerID).getGoods().contains(this.getGood(goodID))) {
                Good good = getGood(goodID);
                User currentOwner = this.getOwner(goodID);

                if(!currentOwner.getID().equals(sellerID)){
                    this.getUser(sellerID).addGood(good);
                    currentOwner.removeGood(good);
                }
                good.setOnSale(onSale);
                good.setTimestamp(Long.valueOf(nonce));
                good.setSignWrite(sigWrite);
            }
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

    private User getOwner(String goodID) {
        for (User u : users) {
            for (Good g : u.getGoods()) {
                if (g.getID().equals(goodID)) {
                    return u;
                }
            }
        }
        throw new GoodNotFoundException(goodID);
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


    public  boolean verifyPOW(String hashCash, String userID, byte[] byteArr) {
        System.out.println("Received Proof of work: " + hashCash);
        String[] parts = hashCash.split(":");
        if(!parts[3].equals(new String(byteArr))){
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
        System.out.println("Proof of work is Valid!");
        return true;
    }


    public PublicKey getPublicKeyNotarioID(int index) {
        return this.notarySignedPublicKeys.get(index);
    }
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, GeneralSecurityException {
        ois.defaultReadObject();
        uniqueInstance = this;
//        this.keys = KeyGen.getInstance().generateRSAKey();
//        setWithCC(true);
    }

    public Message validateWrite(String type, String goodID, String buyerID, String sellerID, String sigWrite, String nonceBuyer, boolean onSale) throws Exception{
        System.out.println("Validate write");

        /**TODO fazer check se o timestamp está dentro de um intervalo determinado**/
        Checker.getInstance().checkSW(goodID, buyerID, nonceBuyer, onSale, sigWrite);

        BRB brb = this.addBRB(buyerID, nonceBuyer, type, goodID, sigWrite, sellerID, onSale);
        int indexBRBS = checksReceivedWriteFromOtherReplics(buyerID, nonceBuyer);

        if(!brb.getSentecho()) {
            sendEcho(indexBRBS);
        }


        boolean flag = true;
        boolean valid = false;
        /**TODO CHECK TIMER**/
        long now = System.currentTimeMillis();
        long end = now + SESSION;
        Message message_aux;
        while(flag) {
            synchronized (this.brbs) {
                message_aux = this.brbs.get(indexBRBS).consensusDeliver();
            }
            if(message_aux!=null) {
                valid = true;
                flag = false;
            }
            if(System.currentTimeMillis() > end) {
                flag = false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        if(valid) {
            //Waits for a possible valid write
            if((this.lastValidWrite != (indexBRBS - 1)) && (this.lastValidWrite != indexBRBS)) {
                now = System.currentTimeMillis();
                end = now + SESSION;
                System.out.println("Now" + now);
                System.out.println("End" + end);
                flag = true;
                while (flag) {
                    if(this.lastValidWrite == indexBRBS - 1 || this.lastValidWrite != indexBRBS) {
                        flag = false;
                    }
                    if(System.currentTimeMillis() > end) {
                        flag = false;
                    }
                }
            }

            brb = this.brbs.get(indexBRBS);
            message_aux = brb.consensusDeliver();

            if(message_aux!=null) {
                brb.setDelivered(true);
                this.brbs.replace(indexBRBS, brb);
                this.lastValidWrite = indexBRBS;
                Message m = this.getBRB(indexBRBS).getMyMessage();
                return m;
            }
        }

        return null;
    }

    public BRB getBRB(int index) {
        return this.brbs.get(index);
    }

    private synchronized BRB addBRB(String buyerID, String nonceBuyer, String type, String goodID, String sigWrite, String sellerID, boolean onSale) {
        int indexBRBS = checksReceivedWriteFromOtherReplics(buyerID, nonceBuyer);
        Message message = new Message(type, goodID, buyerID, sellerID, nonceBuyer, sigWrite, onSale);


        BRB brb;
        if(indexBRBS == -1) {
            indexBRBS = this.brbs.size() + 1;
            brb = new BRB(message);
            brbs.put(indexBRBS, brb);
        }
        else {
            brb = this.brbs.get(indexBRBS);
            brb.setMyMessage(message);
            this.brbs.replace(indexBRBS, brb);
        }
        return  brb;
    }

    public synchronized int checksReceivedWriteFromOtherReplics(String buyerID, String timestamp) {
        for(int i : this.brbs.keySet()) {
            BRB brb = this.brbs.get(i);
            if(brb.getUserID().equals(buyerID) && brb.getTimestamp().equals(timestamp)) {
                return i;
            }
        }
        return -1;
    }

    private void sendEcho(int index) throws Exception{
        System.out.println("echo");
        BRB brb = this.brbs.get(index);
        brb.setSentecho(true);
        int notaryID = Integer.parseInt(System.getProperty("port"));
        brb.addEchos(notaryID, brb.getMyMessage());
        this.brbs.replace(index, brb);
        Message m = brb.getMyMessage();
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/write/sendEcho".getBytes());
        String nonce = String.valueOf((System.currentTimeMillis()));
        byte[] toSign = (type + "||" + m.getType() + "||" + m.getGoodID() + "||" + m.getBuyerID() +
                "||" + m.getSellerID() + "||" +  m.getTimestamp() + "||" + m.getSignWrite() +
                "||" + m.isOnSale() + "||" + nonce + "||" +  notaryID).getBytes();

        String sig = Crypto.getInstance().sign(this.keys.getPrivate(), toSign);
        String REST_URI_C;
        for (int i = 1; i <= N; i++) {
            if(i != notaryID) {
                REST_URI_C = "http://localhost:919" + i + "/notary/notary";
                try{
                    Response r = client.target(REST_URI_C + "/write/echo").queryParam("typeM", m.getType()).queryParam("goodID", m.getGoodID()).
                            queryParam("sellerID", m.getSellerID()).queryParam("buyerID", m.getBuyerID()).queryParam("nonceM", m.getTimestamp()).
                            queryParam("signWrite" , m.getSignWrite()).queryParam("onSale", m.isOnSale()).queryParam("nonce", nonce).
                            queryParam("sig", sig).queryParam("notaryID", Integer.toString(notaryID)).request(MediaType.APPLICATION_JSON).get();
                }
                catch (Exception e) {
                    System.out.println("Error: Echo to notary with port " + i);
                }
            }
        }

    }

    private synchronized BRB setReady(int index) {
        BRB brb = this.brbs.get(index);
        brb.setSentready(true);
        int notaryID = Integer.parseInt(System.getProperty("port"));
        brb.addReady(notaryID, brb.getMyMessage());
        this.brbs.replace(index, brb);
        return brb;
    }

    public void sendReady(int index) throws Exception{
        int notaryID = Integer.parseInt(System.getProperty("port"));
        System.out.println("Ready");
        BRB brb = this.setReady(index);
        Message m = brb.consesusReady();
        if(m==null || !brb.getSentecho()) {
            m = brb.consensusEcho();
        }
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/write/sendReady".getBytes());
        String nonce = String.valueOf((System.currentTimeMillis()));
        byte[] toSign = (type + "||" + m.getType() + "||" + m.getGoodID() + "||" + m.getBuyerID() +
                "||" + m.getSellerID() + "||" + m.getTimestamp() + "||" + m.getSignWrite() +
                "||" + m.isOnSale() + "||" + nonce + "||" + notaryID).getBytes();
        String sig = Crypto.getInstance().sign(this.keys.getPrivate(), toSign);
        String REST_URI_C;
        for (int i = 1; i <= N; i++) {
            if(i != notaryID) {
                REST_URI_C = "http://localhost:919" + i + "/notary/notary";
                try {
                    Response r = client.target(REST_URI_C + "/write/ready").queryParam("typeM", m.getType()).queryParam("goodID", m.getGoodID()).
                            queryParam("sellerID", m.getSellerID()).queryParam("buyerID", m.getBuyerID()).queryParam("nonceM", m.getTimestamp()).
                            queryParam("signWrite" , m.getSignWrite()).queryParam("onSale", m.isOnSale()).queryParam("nonce", nonce).
                            queryParam("sig", sig).queryParam("notaryID", Integer.toString(notaryID)).request(MediaType.APPLICATION_JSON).get();
                }
                catch (Exception e) {
                    System.out.println("Error: Ready to notary with port " + i);
                }
            }
        }

    }

    public void retrievePublicKey(int i) {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/keys/getPublicKey".getBytes());
            String REST_URI_C;
            REST_URI_C = "http://localhost:919" + i + "/notary/notary";
            Response r = client.target(REST_URI_C + "/keys/getPublicKey").request(MediaType.APPLICATION_JSON).get();
            String publicKeySignature = r.getHeaderString("PublicKey-Signature");
            String publicKeybase64 = r.readEntity(String.class);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeybase64.getBytes()));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(publicKeySpec);
            this.notarySignedPublicKeys.put(i, publicKey);
            byte[] toSign = (type + "||" + publicKeySignature + "||" + Base64.getEncoder().encodeToString(publicKey.getEncoded())).getBytes();
            this.verifyResponse(r, toSign, false, i); // General request verification. Check integrity hole message
            /*  The check bellow is really important
                Check if signature from CC actually matches the sent public key
                An attacker may encript a similar message with a equally generated key.
                This verification is going ensure that the public key actually came from notary and not from another place
                This is done this away to avoid unnecessary CC signatures every time someone asks for the public key.
             */


            /**TODO fix this**/
            /***
            if (this.withCC && !Crypto.getInstance().checkSignature(this.notaryCCPublicKey, publicKey.getEncoded(), publicKeySignature)) {
                throw new InvalidSignature("Public Key sent from notary was forged. Signature made with CC is wrong");
            }***/
            return;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private void verifyResponse(Response r, byte[] toSign, boolean withCC, int index){
        withCC = withCC && this.withCC;
        String sig = r.getHeaderString("Notary-Signature");
        String nonceS = r.getHeaderString("Notary-Nonce");
        long nonce = Long.valueOf(nonceS).longValue();
        if (sig == null) {
            throw new InvalidSignature("Signature from notary was null");
        } else {
            //TODO Put this working with CC
            toSign = (new String(toSign) + "||" + nonceS).getBytes();
            if (!Crypto.getInstance().checkSignature(this.notarySignedPublicKeys.get(index), toSign, sig)) {
                throw new InvalidSignature("Signature from notary was forged");
            }
        }
    }



    public synchronized BRB addBRBEcho(Message m, int notaryID) {
        int index =  this.checksReceivedWriteFromOtherReplics(m.getBuyerID(), m.getTimestamp());
        BRB brb;
        if(index == -1) {
            int indexBRBS = this.brbs.size() + 1;
            brb = new BRB(m.getBuyerID(), m.getTimestamp());
            brb.addEchos(notaryID, m);
            brbs.put(indexBRBS, brb);
        }
        else{
            brb = this.brbs.get(index);
            brb.addEchos(notaryID, m);
            this.brbs.replace(index, brb);
        }
        return  brb;
    }

    public synchronized BRB addBRBReady(Message m, int notaryID) {
        int index =  this.checksReceivedWriteFromOtherReplics(m.getBuyerID(), m.getTimestamp());
        BRB brb;
        if(index == -1) {
            int indexBRBS = this.brbs.size() + 1;
            brb = new BRB(m.getBuyerID(), m.getTimestamp());
            brb.addReady(notaryID, m);
            brbs.put(indexBRBS, brb);
        }
        else {
            brb = this.brbs.get(index);
            brb.addReady(notaryID, m);
            this.brbs.replace(index, brb);
        }
        return brb;
    }

    public int getN() {
        return this.N;
    }

    public int getF() {
        return this.F;
    }


}