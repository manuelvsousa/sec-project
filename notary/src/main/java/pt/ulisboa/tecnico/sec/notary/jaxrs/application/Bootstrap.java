package pt.ulisboa.tecnico.sec.notary.jaxrs.application;


import pt.ulisboa.tecnico.sec.notary.model.Good;
import pt.ulisboa.tecnico.sec.notary.model.User;
import pt.ulisboa.tecnico.sec.notary.util.CitizenCard;
import pt.ulisboa.tecnico.sec.util.KeyReader;
import pt.ulisboa.tecnico.sec.util.KeyWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.PublicKey;


public class Bootstrap implements ServletContextListener {
    private String serializeFileName = "notary";
    private final static String SERIALIZE_FILE_EXTENSION = ".ser";
    private boolean CITIZEN_CARD_ACTIVATED = false;

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.print("Exiting");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            String Filename = serializeFileName + System.getProperty("port") + SERIALIZE_FILE_EXTENSION;
            File f = new File("notary.ser");
            if (f.exists()) {
                ObjectInput in = new ObjectInputStream(new FileInputStream(serializeFileName));
                Notary notary = (Notary) in.readObject();
                in.close();
                System.out.println("Notary has been deserialized ");
            } else {
                String path = new File(System.getProperty("user.dir")).getParent();
                Notary notary = Notary.getInstance();
                if (this.CITIZEN_CARD_ACTIVATED) {
                    PublicKey publicKey = CitizenCard.getInstance().getPublicKey();
                    KeyWriter.getInstance().write(publicKey, "notaryCC");
                    notary.setWithCC(true);
                }
                System.out.println("User 0 created");
                User user1 = new User("user1", KeyReader.getInstance().readPublicKey("user1", path));
                user1.addGood(new Good("good1", true, "user1"));
                user1.addGood(new Good("good2", false, "user1"));
                notary.addUser(user1);
                System.out.println("User 1 created");

                User user2 = new User("user2", KeyReader.getInstance().readPublicKey("user2", path));
                user2.addGood(new Good("good3", true, "user2"));
                notary.addUser(user2);
                System.out.println("User 2 created");

                notary.addUser(new User("user3", KeyReader.getInstance().readPublicKey("user3", path)));
                System.out.println("User 3 created");
            }
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