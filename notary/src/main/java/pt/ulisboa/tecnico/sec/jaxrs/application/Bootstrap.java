package pt.ulisboa.tecnico.sec.jaxrs.application;


import pt.ulisboa.tecnico.sec.model.Good;
import pt.ulisboa.tecnico.sec.model.User;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;


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
        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        }
        User asd = new User("asd", "olaaaaaaaa");
        Notary.getInstance().addUser(asd);
        asd.addGood(new Good("asd", true));
    }
}