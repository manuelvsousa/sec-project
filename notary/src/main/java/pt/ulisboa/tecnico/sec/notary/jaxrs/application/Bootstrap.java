package pt.ulisboa.tecnico.sec.notary.jaxrs.application;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;


public class Bootstrap implements ServletContextListener {
    private String serializeFileName = "notary.ser";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.print("Exiting");
        Notary.save();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            ObjectInput in = new ObjectInputStream(new FileInputStream(serializeFileName));
            Notary notary = (Notary) in.readObject();
            in.close();

            System.out.println("Object has been deserialized ");
//
//            System.out.println("User 0 created");
//            System.out.print(KeyReader.getInstance().readPublicKey("user1"));
//            User user1 = new User("user1", KeyReader.getInstance().readPublicKey("user1"));
//            user1.addGood(new Good("good1", true));
//            user1.addGood(new Good("good2", false));
//            Notary.getInstance().addUser(user1);
//            System.out.println("User 1 created");
//
//            User user2 = new User("user2", KeyReader.getInstance().readPublicKey("user2"));
//            user2.addGood(new Good("good3", true));
//            Notary.getInstance().addUser(user2);
//            System.out.println("User 2 created");

//            Notary.getInstance().addUser(new User("user3", KeyReader.getInstance().readPublicKey("user3")));
//    System.out.print(Notary.getInstance().getUser("user1").getPublicKey());
//                System.out.println("User 3 created");

//        CitizenCard cc = CitizenCard.getInstance();
//        byte[] sig = cc.sign("THE_CONTENT_THAT_IS_BEING_SIGNED".getBytes());
//        System.out.print("Signature: " + sig);
//        X509Certificate ccpub = cc.getCertificate();

        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException ex) {
            System.out.println("GeneralSecurityException is caught");
        }


    }
}