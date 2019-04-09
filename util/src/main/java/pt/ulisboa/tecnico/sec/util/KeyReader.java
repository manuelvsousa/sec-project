package pt.ulisboa.tecnico.sec.util;

import pt.ulisboa.tecnico.sec.util.exception.PrivateKeyWrongPassword;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class KeyReader {

    private static KeyReader uniqueInstance;


    private KeyReader() {
    }

    public static KeyReader getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new KeyReader();
        }
        return uniqueInstance;
    }

    public void write(String keyPath) throws GeneralSecurityException, IOException {
        // get an AES private key
        System.out.println("Generating RSA key ...");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keys = keyGen.generateKeyPair();
        System.out.println("Finish generating RSA keys");

        System.out.println("Private Key:");
        PrivateKey privKey = keys.getPrivate();
        byte[] privKeyEncoded = privKey.getEncoded();
        System.out.println(printHexBinary(privKeyEncoded));
        System.out.println("Public Key:");
        PublicKey pubKey = keys.getPublic();
        byte[] pubKeyEncoded = pubKey.getEncoded();
        System.out.println(printHexBinary(pubKeyEncoded));

        System.out.println("Writing Private key to '" + keyPath + "' ...");
        FileOutputStream privFos = new FileOutputStream(keyPath);
        privFos.write(privKeyEncoded);
        privFos.close();
        System.out.println("Writing Pubic key to '" + keyPath + "' ...");
        FileOutputStream pubFos = new FileOutputStream(keyPath);
        pubFos.write(pubKeyEncoded);
        pubFos.close();
    }

    public PublicKey readPublicKey(String userID) throws GeneralSecurityException, IOException {
        String path = System.getProperty("user.dir");
        byte[] encoded = read(path + "/keys/users/" + userID + ".pub");
        PublicKey publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        return publicKey;
    }


    public PrivateKey readPrivateKey(String userID, String password) throws GeneralSecurityException, IOException, BadPaddingException {
            String path = System.getProperty("user.dir");
            return readPrivateKey(userID, password, path);
    }

    public PrivateKey readPrivateKey(String userID, String password, String path) throws GeneralSecurityException, IOException, BadPaddingException {
        try {
            byte[] encoded = read(path + "/keys/users/" + userID + ".enc.key");
            byte[] salt = read(path + "/keys/users/" + userID + "salt.txt");
            System.out.println(path);
            EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(encoded);
            byte[] encrypPrivKey = encinfo.getEncryptedData();

            String PBEALG = "PBEWithSHA1AndDESede";
            int count = 20; //hash iteration count

            //Create PBE parameter set
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBEALG);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher pbeCipher = Cipher.getInstance(PBEALG);

            //Initialize PBE cipher with key and parameters
            pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

            byte[] encodedPrivKey = pbeCipher.doFinal(encrypPrivKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(encodedPrivKey));
            return privKey;
        } catch (BadPaddingException e) {
            throw new PrivateKeyWrongPassword();
        }
    }

    private byte[] read(String path) throws GeneralSecurityException, IOException {
        System.out.println("Reading from file " + path + " ...");
        FileInputStream fis = new FileInputStream(path);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return encoded;
    }

}
