package pt.ulisboa.tecnico.sec.util;

import java.io.File;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Application {
    public static void main(String[] args) throws Exception {
//        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//        kpg.initialize(1024);
//        KeyPair asd =  kpg.genKeyPair();
//        String asdd = Crypto.getInstance().sign(asd.getPrivate(),"lalalallaallaa".getBytes());
//        System.out.println(asdd);
//        System.out.println(Crypto.getInstance().checkSignature(asd.getPublic(),"lalalallaallaa".getBytes(),asdd));
//        CitizenCard.getInstance().getCertificate();
//        CitizenCard.getInstance().getCertificate();
        //System.out.println(KeyReader.getInstance().readPublicKey("user4"));


        //PrivateKey privateKey = KeyReader.getInstance().readPrivateKey("user1", "password1");
        //PublicKey publicKey = KeyReader.getInstance().readPublicKey("user1");
        //String sig = Crypto.getInstance().sign(privateKey, "L2dvb2RzL3RyYW5zZmVy||good1||user2||user1".getBytes());
        //System.out.print(sig);
        //Boolean correct = Crypto.getInstance().checkSignature(publicKey, ("L2dvb2RzL3RyYW5zZmVy||good1||user2||user1").getBytes(), sig);
        //System.out.println(correct);

        /**TODO Melhorar isto**/
        PrivateKey privateKey = KeyReader.getInstance().readPrivateKey("user2", "password2");
        String sig = Crypto.getInstance().sign(privateKey, "good3 || true || 0 || user2".getBytes());
        FileOutputStream pubFos = new FileOutputStream("user2_good3_sw.txt");
        pubFos.write(sig.getBytes());
        pubFos.close();
    }
}
