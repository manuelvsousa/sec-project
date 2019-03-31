package pt.ulisboa.tecnico.sec.usercli;

import pt.ulisboa.tecnico.sec.citizencard.NotaryClient;
import pt.ulisboa.tecnico.sec.notary.model.State;
/** TODO Fazer import?*/

public class UserClient {
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
}
