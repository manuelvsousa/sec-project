package pt.ulisboa.tecnico.sec.usercli;


import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.exception.UserDoesNotOwnGood;
import pt.ulisboa.tecnico.sec.notary.model.exception.UserNotFoundException;
import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;
import pt.ulisboa.tecnico.sec.notaryclient.exception.GoodNotFoundException;

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

    public Boolean getStateOfgood(String goodID) throws Exception {
        State state = this.notaryClient.getStateOfGood(goodID);
        return state.getOnSale();
    }

    public String intentionToSell(String goodID) throws Exception {
        try {
            this.notaryClient.intentionToSell(goodID);
            String message = "The " + goodID + " was set to sell";
            return message;
        } catch(Exception e) {
            String error = "Error: " + e.getMessage();
            return error;
        }
    }


    public Boolean buyGood(String goodID, String buyerID, String sellerID) throws Exception {
        ua.buyGood(goodID, buyerID, sellerID);
        return true;
    }


    public void addGood(String goodID) {
        this.goods.add(goodID);
    }

    public void printGoods() {
        System.out.println("Goods");
        for (String goodID : this.goods) {
            System.out.println(goodID);
        }
    }
}
