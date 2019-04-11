package pt.ulisboa.tecnico.sec.util;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.*;

public class KeyGen {

    private static KeyGen uniqueInstance;


    private KeyGen() {
    }

    public static KeyGen getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new KeyGen();
        }
        return uniqueInstance;
    }

    public KeyPair generateRSAKey() throws GeneralSecurityException {
        // get an AES private key
        System.out.println("Generating RSA key ...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keys = keyGen.generateKeyPair();
        return keys;
    }


    private void protectRSAKey(KeyPair kp, String password) throws GeneralSecurityException, IOException {
        Key privKey = kp.getPrivate();
        byte[] encodedprivKey = privKey.getEncoded();
        String PBEALG = "PBEWithSHA1AndDESede";


        int count = 20; //hash iteration count
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        //Create PBE parameter set
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBEALG);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(PBEALG);

        //Initialize PBE cipher with key and parameters
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

        //Encrypt the encoded Private Key with the PBE key
        byte[] ciphertext = pbeCipher.doFinal(encodedprivKey);

        //Constructs PKCS#8 EncryptedPrivateKeyInfo object
        AlgorithmParameters algparms = AlgorithmParameters.getInstance(PBEALG);
        algparms.init(pbeParamSpec);
        EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);
    }
}
