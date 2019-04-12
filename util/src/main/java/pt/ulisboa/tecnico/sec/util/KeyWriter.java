package pt.ulisboa.tecnico.sec.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PublicKey;

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

    public void write(PublicKey publicKey, String user) throws IOException {
        String path = new File("../").getCanonicalPath();
        System.out.println(path + "/keys/users/" + user + ".pub");
        FileOutputStream pubFos = new FileOutputStream(path + "/keys/users/" + user + ".pub");
        pubFos.write(publicKey.getEncoded());
        pubFos.close();
    }

}
