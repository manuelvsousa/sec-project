package pt.ulisboa.tecnico.sec.notary.util;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
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

    public void checkResponse(byte[] receivedContent, String userID, String sig, String nonce) {
        if (!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(userID).getPublicKey(), receivedContent, sig)) {
            throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!",sig,nonce);
        }
        long nonceL = Long.valueOf(nonce).longValue();
        if (nonceL > Notary.getInstance().getUser(userID).getLastNonce()) {
            Notary.getInstance().getUser(userID).setLastNonce(nonceL);
        } else {
            throw new InvalidTransactionExceptionResponse("Invalid Nonce",sig,nonce);
        }
    }
}