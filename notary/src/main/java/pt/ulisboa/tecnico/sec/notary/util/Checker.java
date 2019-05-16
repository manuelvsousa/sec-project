package pt.ulisboa.tecnico.sec.notary.util;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidSignatureWrite;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.util.Crypto;

import java.security.PublicKey;


public class Checker {
    private static Checker uniqueInstance;

    private Checker() {
    }

    public static Checker getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Checker();
        }
        return uniqueInstance;
    }

    public void checkResponse(byte[] receivedContent, String userID, String sig, String nonce, String nonceNotary, String sigNotary) {
        if (!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(userID).getPublicKey(), receivedContent, sig)) {
            throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!", sigNotary, nonceNotary);
        }
        long nonceL = Long.valueOf(nonce).longValue();
        if (nonceL > Notary.getInstance().getUser(userID).getLastNonce()) {
            Notary.getInstance().getUser(userID).setLastNonce(nonceL);
        } else {
            throw new InvalidTransactionExceptionResponse("Invalid Nonce", sigNotary, nonceNotary);
        }
    }

    public void checkSW(String goodID, String userID, String time, boolean onSale, String signWrite) {
        byte[] toSW = (goodID + " || " + onSale + " || " +  time + " || " + userID).getBytes();
        if (!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(userID).getPublicKey(), toSW, signWrite)) {
            throw new InvalidSignatureWrite();
        }
    }


    public void checkResponseWrite(String type, String typeM, String goodID, String buyerID,  String sellerID, String nonceM, String signWrite,
                                   String onSale, String nonce, String notaryID, String sig) {
        byte[] toSign = (type + "||" + typeM + "||" + goodID + "||" + buyerID +
                "||" + sellerID + nonceM + "||" + signWrite +
                "||" + onSale + nonce + notaryID).getBytes();

        PublicKey publicKey = Notary.getInstance().getPublicKeyNotarioID(Integer.parseInt(notaryID));
        if(publicKey == null) {
            Notary.getInstance().retrievePublicKey(Integer.parseInt(notaryID));
            publicKey =  Notary.getInstance().getPublicKeyNotarioID(Integer.parseInt(notaryID));
        }


        if (!Crypto.getInstance().checkSignature(publicKey, toSign, sig)) {
            throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!", sig, nonce);
        }

        byte[] toSW = (goodID+ " || " + onSale + " || " +  nonceM + " || " + buyerID).getBytes();
        if (!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(buyerID).getPublicKey(), toSW, signWrite)) {
            throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!", sig, nonce);
        }

        /* It isn't necessary to verify the message freshness, because the notary will only add to the echos list if it didn't receive any
           valid echo before with that buyerID and timestamp
         */
    }
}