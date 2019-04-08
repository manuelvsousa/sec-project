package pt.ulisboa.tecnico.sec.notary.jaxrs.application;


import pt.ulisboa.tecnico.sec.notary.model.Good;
import pt.ulisboa.tecnico.sec.notary.model.User;
import pt.ulisboa.tecnico.sec.notary.util.CitizenCard;
import pt.ulisboa.tecnico.sec.util.KeyReader;
import pt.ulisboa.tecnico.sec.util.KeyWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;


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

            // TODO ADD THIS FIRST RUN
//            PublicKey publicKey = CitizenCard.getInstance().getPublicKey();
//            KeyWriter.getInstance().write(publicKey, "notary");

            ObjectInput in = new ObjectInputStream(new FileInputStream(serializeFileName));
            Notary notary = (Notary) in.readObject();
            in.close();
//            PrivateKey privateKey = KeyReader.getInstance().readPrivateKey("notary","notary");
//            notary.setPrivateKey(privateKey);
            System.out.println("Object has been deserialized ");

            System.out.println("User 0 created");
            User user1 = new User("user1", KeyReader.getInstance().readPublicKey("user1"));
            user1.addGood(new Good("good1", true));
            user1.addGood(new Good("good2", false));
            notary.addUser(user1);
            System.out.println("User 1 created");

            User user2 = new User("user2", KeyReader.getInstance().readPublicKey("user2"));
            user2.addGood(new Good("good3", true));
            notary.addUser(user2);
            System.out.println("User 2 created");

            notary.addUser(new User("user3", KeyReader.getInstance().readPublicKey("user3")));
            System.out.println("User 3 created");
        } catch (IOException ex) {
            System.out.println("IOException is caught");
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        } catch (GeneralSecurityException gse) {
            System.out.println("GeneralSecurityException is caught");
        }


    }
}