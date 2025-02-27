package pt.ulisboa.tecnico.sec.util;

import pt.ulisboa.tecnico.sec.util.exception.PrivateKeyWrongPassword;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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

    public PublicKey readPublicKey(String userID) throws GeneralSecurityException, IOException {
        String path = new File(System.getProperty("user.dir")).getParent();
        System.out.println(path);
        return readPublicKey(userID, path);
    }

    public PublicKey readPublicKey(String userID, String path) throws GeneralSecurityException, IOException {
        byte[] encoded = read(path + "/keys/users/" + userID + ".pub");
        PublicKey publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        return publicKey;
    }

    public PrivateKey readPrivateKey(String userID, String password) throws GeneralSecurityException, IOException {
        String path = new File(System.getProperty("user.dir")).getParent();
        System.out.println(path);
        return readPrivateKey(userID, password, path);
    }

    public PrivateKey readPrivateKey(String userID, String password, String path) throws GeneralSecurityException, IOException {
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

    public PrivateKey readPrivateKey(byte[] encoded, byte[] salt, String password) throws GeneralSecurityException, IOException {
        try {
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

    private byte[] read(String path) throws IOException {
        System.out.println("Reading from file " + path + " ...");
        FileInputStream fis = new FileInputStream(path);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return encoded;
    }

}
