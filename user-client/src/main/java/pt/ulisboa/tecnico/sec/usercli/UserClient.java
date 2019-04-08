package pt.ulisboa.tecnico.sec.usercli;


import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;


public class UserClient {
    public List<String> goods = new ArrayList<>();
    private UserAbstract ua = new UserAbstract();
    private NotaryClient notaryClient;
    //EXCEPÇÕES


    public UserClient(String userID, PrivateKey privKey) {
        this.notaryClient = new NotaryClient(userID, privKey);
    }

    public Boolean getStateOfgood(String goodID) {
        State state = this.notaryClient.getStateOfGood(goodID);
        return state.getOnSale();
    }

    public Boolean intentionToSell(String goodID) throws Exception {
        return this.notaryClient.intentionToSell(goodID);
    }

    /**
     * TODO Tirar isto daqui
     **/
    public Boolean transferGood(String goodID, String buyerID) throws Exception {
        return this.notaryClient.transferGood(goodID, buyerID);
    }

    public Boolean buyGood(String goodID, String buyerID, String sellerID) {
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
