package pt.ulisboa.tecnico.sec.usercli;

import com.sun.org.apache.xpath.internal.operations.Bool;
import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;
import pt.ulisboa.tecnico.sec.notary.model.State;

import java.util.ArrayList;
import java.util.List;


public class UserClient {
    private UserAbstract ua = new UserAbstract();
    private NotaryClient notaryClient;
    public List<String> goods = new ArrayList<>();
    //EXCEPÇÕES

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

    /**TODO Tirar isto daqui**/
    public Boolean transferGood(String goodID, String buyerID) {
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
        for(String goodID : this.goods){
            System.out.println(goodID);
        }
    }
}
