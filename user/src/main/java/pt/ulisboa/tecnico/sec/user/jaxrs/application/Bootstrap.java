package pt.ulisboa.tecnico.sec.user.jaxrs.application;


//import pt.ulisboa.tecnico.sec.notary.model.Good;
//import pt.ulisboa.tecnico.sec.notary.model.User;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class Bootstrap implements ServletContextListener {
    private String serializeFileName = "notary.ser";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.print("Exiting");
        //Notary.save();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        /**TODO**/
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
         User asd1 = new User("user1", "public1");
         asd1.addGood(new Good("good1", true));
         Notary.getInstance().addUser(asd1);
         Notary.getInstance().addUser(new User("user2", "public2"));
         Notary.getInstance().addUser(new User("user3", "public3"));**/
    }

}