package pt.ulisboa.tecnico.sec.usercli;


import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;


public class UserClient {

    public List<String> goods = new ArrayList<>();
    private UserAbstract ua;
    private NotaryClient notaryClient;
    //EXCEPÇÕES


    public UserClient(String userID, PrivateKey privKey) {
        this.notaryClient = new NotaryClient(userID, privKey);
        this.ua = new UserAbstract(privKey);
    }

    public String getStateOfgood(String goodID) throws Exception {
        try {
            State state = this.notaryClient.getStateOfGood(goodID);
            String message = goodID + "->  Owner: " + state.getOwnerID() + "; On sale: ";
            if (state.getOnSale()) {
                message = message + "Yes;";
            } else {
                message = message + "No;";
            }
            return message;
        } catch (Exception e) {
            String error = "Error: " + e.getMessage();
            return error;
        }
    }

    public String intentionToSell(String goodID) throws Exception {
        try {
            this.notaryClient.intentionToSell(goodID);
            String message = "The " + goodID + " was set to sell";
            return message;
        } catch (Exception e) {
            String error = "Error: " + e.getMessage();
            return error;
        }
    }


    public String buyGood(String goodID, String buyerID, String sellerID) throws Exception {
        try {
            ua.buyGood(goodID, buyerID, sellerID);
            return "The transaction was successful";
        } catch (Exception e) {
            String error = "Error: " + e.getMessage();
            return error;
        }
    }


    public void addGood(String goodID) {
        this.goods.add(goodID);
    }

    public void printGoods() {
        System.out.println("List of Goods in the system: ");
        for (String goodID : this.goods) {
            System.out.println(" ->" + goodID);
        }
    }
}
