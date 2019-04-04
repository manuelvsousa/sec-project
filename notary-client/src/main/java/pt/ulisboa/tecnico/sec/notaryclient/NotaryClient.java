package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;

import java.security.PrivateKey;


public class NotaryClient {
    private String userID;
    private NotaryAbstract na;
    private PrivateKey privateKey;

    public NotaryClient(String userID, PrivateKey privateKey) {
        this.userID = userID;
        this.privateKey = privateKey;
        this.na = new NotaryAbstract(privateKey);
    }

    public State getStateOfGood(String id) {
        return na.getStateOfGood(id);
    }

    public boolean transferGood(String goodID, String buyerID) throws Exception {
        na.transferGood(goodID, buyerID, this.userID);
        return true;
    }

    public boolean intentionToSell(String goodID) {
        na.intentionToSell(goodID, this.userID);
        return true;
    }
}