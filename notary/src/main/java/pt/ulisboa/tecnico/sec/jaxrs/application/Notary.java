package pt.ulisboa.tecnico.sec.jaxrs.application;

import javafx.util.Pair;
import pt.ulisboa.tecnico.sec.model.Good;
import pt.ulisboa.tecnico.sec.model.Status;
import pt.ulisboa.tecnico.sec.model.Transaction;
import pt.ulisboa.tecnico.sec.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Notary implements Serializable {

    private static Notary uniqueInstance;

    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    private Notary() {}

    public static Notary getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Notary();
        }
        return uniqueInstance;
    }

    public void addUser(User u){
        users.add(u);
        this.save();
    }

    public void addTransaction(Transaction t){
        transactions.add(t);
        this.save();
    }

    public Status getGoodStatus(String goodID){
        for (User u : users) {
            for (Good g : u.getGoods()) {
                if(g.getID().equals(goodID)){
                    return new Status(u,g.onSale());
                }
            }
        }
        //throw NOT FOUND;
        return null;
    }
    protected Object readResolve() {
        return getInstance();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        uniqueInstance = this;
    }

    public static void save(){
        String serializeFileName = "notary.ser";

        try {
            Notary notary = Notary.getInstance();
            ObjectOutput out = null;

            out = new ObjectOutputStream(new FileOutputStream(serializeFileName));
            out.writeObject(notary);
            out.close();

            System.out.println("Object has been serialized");
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

}