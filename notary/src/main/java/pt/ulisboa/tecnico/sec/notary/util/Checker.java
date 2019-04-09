package pt.ulisboa.tecnico.sec.notary.util;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


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

    public void checkResponse(byte[] receivedContent, String userID, String sig, String nonce){
        if (!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(userID).getPublicKey(), receivedContent, sig)) {
            throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!");
        }
        long nonceL = Long.valueOf(nonce).longValue();
        if(nonceL > Notary.getInstance().getUser(userID).getLastNonce()){
            Notary.getInstance().getUser(userID).setLastNonce(nonceL);
        } else {
            throw new InvalidTransactionExceptionResponse("Invalid Nonce");
        }
    }
}