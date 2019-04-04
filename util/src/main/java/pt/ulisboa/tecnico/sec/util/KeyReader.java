package pt.ulisboa.tecnico.sec.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
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
        String path = new File("../").getCanonicalPath();
        System.out.print(path);
        FileInputStream fis = new FileInputStream(path + "/keys/users/" + userID + ".pub");
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();
        PublicKey publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        return publicKey;
    }

//    public PrivateKey readPrivateKey(String userID,String password) throws GeneralSecurityException, IOException {
//        String path = new File("../").getCanonicalPath();
//        System.out.print(path);
//        FileInputStream fis = new FileInputStream(path + "/keys/users/" + userID + ".key");
//        byte[] encoded = new byte[fis.available()];
//        fis.read(encoded);
//        fis.close();
//        PrivateKey privateKey =
//                KeyFactory.getInstance("RSA").generatePrivate(new X509EncodedKeySpec(encoded));
//        return privateKey;
//    }

}
