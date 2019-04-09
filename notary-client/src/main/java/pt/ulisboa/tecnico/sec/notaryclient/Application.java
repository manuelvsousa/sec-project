package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.util.KeyReader;

import java.security.PrivateKey;
import java.io.File;

public class Application {
    public static void main(String[] args) throws Exception {
        //CitizenCard cc = CitizenCard.getInstance();
        //cc.sign();

//        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//        kpg.initialize(1024);
//        KeyPair asd = kpg.genKeyPair();
////        NotaryClient nc = new NotaryClient("user1",kpg.genKeyPair().getPrivate());
////        //System.out.print(nc.getStateOfGood("good12").getOwnerID());
////
////        //nc.intentionToSell("good1","user1");
////        nc.transferGood("good1", "user2");
//        String asdd = Crypto.getInstance().sign(asd.getPrivate(), "lalalallaallaa".getBytes());
//        System.out.println(asdd);
//        System.out.println(Crypto.getInstance().checkSignature(asd.getPublic(), "lalalallaallaa".getBytes(), asdd));
//        CitizenCard.getInstance().getCertificate();

        String path = new File(System.getProperty("user.dir")).getParent();
        PrivateKey pk = KeyReader.getInstance().readPrivateKey("user1", "password1", path);
        NotaryClient nc = new NotaryClient("user1", pk);
        System.out.println(nc.getStateOfGood("good2").getOwnerID());
        System.out.println(nc.intentionToSell("good2"));
        System.out.println(nc.transferGood("good1","user2"));
        //System.out.println(nc.intentionToSell("good2"));
        // System.out.println(nc.intentionToSell("good2"));

    }
}
