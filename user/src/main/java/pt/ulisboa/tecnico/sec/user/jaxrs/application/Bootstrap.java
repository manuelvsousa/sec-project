package pt.ulisboa.tecnico.sec.user.jaxrs.application;


//import pt.ulisboa.tecnico.sec.notary.model.Good;
//import pt.ulisboa.tecnico.sec.notary.model.UserServ;

import pt.ulisboa.tecnico.sec.user.model.User;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Bootstrap implements ServletContextListener {
    private String serializeFileName = "user.ser";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Exiting");
        //Notary.save();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        /**TODO**/
        UserServ.getInstance().addUser(new User("user2", "public2"));
        UserServ.getInstance().addUser(new User("user3", "public3"));
        /**try {
            ObjectInput in = new ObjectInputStream(new FileInputStream(serializeFileName));
            Notary notary = (Notary) in.readObject();
            in.close();

            System.out.println("Object has been deserialized ");
        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        }
        UserServ asd1 = new UserServ("user1", "public1");
        asd1.addGood(new Good("good1", true));
        Notary.getInstance().addUser(asd1);
        Notary.getInstance().addUser(new UserServ("user2", "public2"));
        Notary.getInstance().addUser(new UserServ("user3", "public3"));**/


    }

}