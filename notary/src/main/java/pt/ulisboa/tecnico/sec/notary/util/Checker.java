package pt.ulisboa.tecnico.sec.notary.util;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidSignatureWrite;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.util.Crypto;


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
}