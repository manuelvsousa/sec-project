package pt.ulisboa.tecnico.sec.citizencard;

import pt.ulisboa.tecnico.sec.notary.model.State;


public class NotaryClient {
    private String userID;
    private NotaryAbstract na = new NotaryAbstract();

    public NotaryClient(String userID){
        this.userID = userID;
    }
    public State getStateOfGood(String id) {
        return na.getStateOfGood(id);
    }

    public boolean transferGood(String goodID, String buyerID) {
        na.transferGood(goodID,buyerID,this.userID);
        return true;
    }

    public boolean intentionToSell(String goodID) {
        na.intentionToSell(goodID,this.userID);
        return true;
    }
//TODO what do I sign
}