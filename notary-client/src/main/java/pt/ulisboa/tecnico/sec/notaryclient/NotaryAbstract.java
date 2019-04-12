package pt.ulisboa.tecnico.sec.notaryclient;

import javafx.util.Pair;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.exception.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.InvalidSignature;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserDoesNotOwnGoodException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserNotFoundException;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.management.relation.RoleUnresolved;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


class NotaryAbstract {

    private static final String REST_URI = "http://localhost:9090/notary/notary";
    private Client client = ClientBuilder.newClient();
    private PrivateKey privateKey;
    private long lastNotaryNonce;
    private PublicKey notarySignedPublicKey;
    private PublicKey notaryCCPublicKey;
    private boolean withCC;

    public NotaryAbstract(PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.lastNotaryNonce = System.currentTimeMillis();
        this.withCC = false;

        try {
            String path = new File(System.getProperty("user.dir")).getParent();
            this.notaryCCPublicKey = KeyReader.getInstance().readPublicKey("notaryCC", path);
        } catch (Exception e) {
            throw new RuntimeException("Could not Load Notary CC public key");
        }

        try {
            getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not Load Notary generated public key");
        }
    }

    public State getStateOfGood(String id, String userID) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (type + "||" + id + "||" + userID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI + "/goods/getStatus").queryParam("id", id).queryParam("userID", userID).queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r, toSign, false);
            State s = r.readEntity(State.class);
            return s;
        } catch (NotFoundException e) {
            String cause = e.getResponse().readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error 404 is null");
            }
            if (cause.toLowerCase().contains("good".toLowerCase())) {
                throw new GoodNotFoundException(cause);
            }
        }
        throw new RuntimeException("Unknown Error");
    }

    private void getPublicKey() {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/keys/getPublicKey".getBytes());
            Response r = client.target(REST_URI + "/keys/getPublicKey").request(MediaType.APPLICATION_JSON).get();
            String publicKeySignature = r.getHeaderString("PublicKey-Signature");
            String publicKeybase64 = r.readEntity(String.class);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeybase64.getBytes()));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(publicKeySpec);
            System.out.println(publicKey);
            this.notarySignedPublicKey = publicKey;
            byte[] toSign = (type + "||" + publicKeySignature + "||" + Base64.getEncoder().encodeToString(publicKey.getEncoded())).getBytes();
            this.verifyResponse(r, toSign, false); // General request verification. Check integrity hole message
            /*  The check bellow is really important
                Check if signature from CC actually matches the sent public key
                An attacker may encript a simillar message with a equally generated key.
                This verification is going ensure that the public key actually came from notary and not from another place
                This is done this away to avoid unnecessary CC signatures everytime someone asks for the public key.
             */
            if (this.withCC && !Crypto.getInstance().checkSignature(this.notaryCCPublicKey, publicKey.getEncoded(), publicKeySignature)) {
                throw new InvalidSignature("Public Key sent from notary was forged. Signature made with CC is wrong");
            }
            return;
        } catch (NotFoundException e) {
            String cause = e.getResponse().readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error 404 is null");
            }
            if (cause.toLowerCase().contains("good".toLowerCase())) {
                throw new GoodNotFoundException(cause);
            }
        } catch (InvalidKeySpecException asd) {
            // TODO
        } catch (NoSuchAlgorithmException asd) {
            // TODO
        }
        throw new RuntimeException("Unknown Error");
    }


    public Map<String, String> transferGood(String goodID, String buyerID, String sellerID, String nonceBuyer, String sigBuyer) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceBuyer + "||" + sigBuyer).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI + "/goods/transfer").queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).queryParam("signature", sig).queryParam("nonce", nonce).queryParam("nonceBuyer", nonceBuyer).queryParam("sigBuyer", sigBuyer).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r, toSign, true);
            String notarySig = r.getHeaderString("Notary-Signature");
            String nonceS = r.getHeaderString("Notary-Nonce");
            Map<String, String> map = new HashMap<>();
            map.put("Notary-Signature", notarySig);
            map.put("Original-Message", new String(toSign) + "||" + nonceS);
            map.put("Good", goodID);
            map.put("Buyer", buyerID);
            map.put("Seller", sellerID);
            map.put("Notary-Time", nonceS);
            return map;
        } catch (Exception e) {
            throw e;
        }
    }

    private void verifyResponse(Response r, byte[] toSign, boolean withCC) {
        withCC = withCC && this.withCC;
        String sig = r.getHeaderString("Notary-Signature");
        String nonceS = r.getHeaderString("Notary-Nonce");
        long nonce = Long.valueOf(nonceS).longValue();
        if (sig == null) {
            throw new InvalidSignature("Signature from notary was null");
        } else {
            toSign = (new String(toSign) + "||" + nonceS).getBytes();
            System.out.println(new String(toSign) + "||" + nonceS);
            if (!Crypto.getInstance().checkSignature(withCC ? this.notaryCCPublicKey : this.notarySignedPublicKey, toSign, sig)) {
                throw new InvalidSignature("Signature from notary was forged");
            }
        }
        if (nonce > this.lastNotaryNonce) {
            this.lastNotaryNonce = nonce;
        } else {
            throw new InvalidSignature("Nonce from notary is invalid");
        }

        if (r.getStatus() == 200) {
            return;
        } else {
            String cause = r.readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error " + r.getStatus() + " is null");
            }
            if (r.getStatus() == 404) {
                if (cause.toLowerCase().contains("good".toLowerCase())) {
                    throw new GoodNotFoundException(cause);
                } else if (cause.toLowerCase().contains("user".toLowerCase())) {
                    throw new UserNotFoundException(cause);
                }
            } else if (r.getStatus() == 417) {
                throw new UserDoesNotOwnGoodException(cause);
            } else if (r.getStatus() == 409) { //conflict
                throw new UserDoesNotOwnGoodException(cause);
            } else {
                throw new RuntimeException("Unable to process request, ERROR " + r.getStatus() + " Received!");
            }
        }
    }

    public void intentionToSell(String goodID, String sellerID) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/intention".getBytes());
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI + "/goods/intention").queryParam("goodID", goodID).queryParam("sellerID", sellerID).queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r, toSign, false);
            return;
        } catch (Exception e) {
            throw e;
        }
    }

}