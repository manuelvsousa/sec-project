package pt.ulisboa.tecnico.sec.usercli;

import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;

import java.util.ArrayList;
import java.util.List;


public class UserClient {
    public List<String> goods = new ArrayList<>();
    private NotaryClient notaryClient;

    public UserClient(String userID) {
        this.notaryClient = new NotaryClient(userID);
    }

    public Boolean getStateOfgood(String goodID) {
        State state = this.notaryClient.getStateOfGood(goodID);
        return state.getOnSale();
    }

    public Boolean intentionToSell(String goodID) {
        return this.notaryClient.intentionToSell(goodID);
    }

    public Boolean transferGood(String goodID, String buyerID) {
        return this.notaryClient.transferGood(goodID, buyerID);
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
