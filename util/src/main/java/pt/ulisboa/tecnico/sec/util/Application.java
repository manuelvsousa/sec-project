package pt.ulisboa.tecnico.sec.util;

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
        System.out.print(KeyReader.getInstance().readPublicKey("user1"));
    }
}
