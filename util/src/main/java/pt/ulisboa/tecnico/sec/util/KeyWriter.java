package pt.ulisboa.tecnico.sec.util;

import pt.ulisboa.tecnico.sec.util.exception.PrivateKeyWrongPassword;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class KeyWriter {

    private static KeyWriter uniqueInstance;


    private KeyWriter() {
    }

    public static KeyWriter getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new KeyWriter();
        }
        return uniqueInstance;
    }

    public void write(PublicKey publicKey,String user) throws GeneralSecurityException, IOException {
        String path = new File("../").getCanonicalPath();
        System.out.println(path +  "/keys/users/" + ".pub");
        FileOutputStream pubFos = new FileOutputStream(path +  "/keys/users/" + user + ".pub");
        pubFos.write(publicKey.getEncoded());
        pubFos.close();
    }

}
