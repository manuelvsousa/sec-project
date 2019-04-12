package pt.ulisboa.tecnico.sec.user.jaxrs.application;

import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;


public class Bootstrap implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Exiting");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String port = System.getProperty("port");
            String userServID = "user" + port;
            String password = "password" + port;

            String path = new File(System.getProperty("user.dir")).getParent();
            PrivateKey privateKey = KeyReader.getInstance().readPrivateKey(userServID, password, path);
            UserServ user = UserServ.getInstance();
            user.setUserID(userServID);
            user.setPrivateKey(privateKey);

            System.out.println("User " + port + " created.");

        } catch (IOException ex) {
            System.out.println("IOException is caught");
            ex.printStackTrace();
        }  catch (GeneralSecurityException gse) {
            System.out.println("GeneralSecurityException is caught");
        }
    }
}