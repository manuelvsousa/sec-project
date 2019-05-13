package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;

import java.security.PrivateKey;
import java.util.Map;


public class NotaryClient {
    private String userID;
    private NotaryAbstract na;

    public NotaryClient(String userID, PrivateKey privateKey) {
        this.userID = userID;
        this.na = new NotaryAbstract(privateKey);
    }

    public State getStateOfGood(String id) throws Exception {
        return na.getStateOfGood(id, this.userID);
    }

    public Map<String, String> transferGood(String goodID, String buyerID, String nonceBuyer, String sigBuyer, String sigWrite) throws Exception {
        return na.transferGood(goodID, buyerID, this.userID, nonceBuyer, sigBuyer, sigWrite);
    }

    public boolean intentionToSell(String goodID) throws Exception {
        na.intentionToSell(goodID, this.userID);
        return true;
    }
}