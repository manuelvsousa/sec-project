package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.exception.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.InvalidSignature;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserDoesNotOwnGoodException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserNotFoundException;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;


class NotaryAbstract {

    private static final String REST_URI = "http://localhost:9090/notary/notary";
    private Client client = ClientBuilder.newClient();
    private PrivateKey privateKey;
    private long lastNotaryNonce;

    public NotaryAbstract(PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.lastNotaryNonce = (System.currentTimeMillis() - 200) / 1000L;
    }

    public State getStateOfGood(String id, String userID) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
            String nonce =  String.valueOf((System.currentTimeMillis() / 1000L));
            byte[] toSign = (type + "||" + id + "||" + userID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI + "/goods/getStatus").queryParam("id", id).queryParam("userID", userID).queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r, toSign);
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


    public void transferGood(String goodID, String buyerID, String sellerID) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
            String nonce =  String.valueOf((System.currentTimeMillis() / 1000L));
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI + "/goods/transfer").queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r, toSign);
            return;
        } catch (Exception e) {
            throw e;
        }
    }

    private void verifyResponse(Response r, byte[] toSign) {
        if (r.getStatus() == 200) {
            try {
                String sig = r.getHeaderString("Notary-Signature");
                String nonceS = r.getHeaderString("Notary-Nonce");
                long nonce = Long.valueOf(nonceS).longValue();
                if (sig == null) {
                    throw new InvalidSignature("Signature from notary was null");
                } else {
                    toSign = (new String(toSign) + "||" + nonceS).getBytes();
                    System.out.println(new String(toSign));
                    PublicKey publicKey = KeyReader.getInstance().readPublicKey("notary");
                    if (!Crypto.getInstance().checkSignature(publicKey, toSign, sig)) {
                        throw new InvalidSignature("Signature from notary was forged");
                    }
                }
                if(nonce > this.lastNotaryNonce){
                    this.lastNotaryNonce = nonce;
                } else {
                    throw new InvalidSignature("Nonce from notary is invalid");
                }
            } catch (GeneralSecurityException gse) {
                System.out.println("GeneralSecurityException catched");
            } catch (IOException io) {
                System.out.println("IOException catched");

            }
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
            String nonce =  String.valueOf((System.currentTimeMillis() / 1000L));
            byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI + "/goods/intention").queryParam("goodID", goodID).queryParam("sellerID", sellerID).queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r, toSign);
            return;
        } catch (Exception e) {
            throw e;
        }
    }

}