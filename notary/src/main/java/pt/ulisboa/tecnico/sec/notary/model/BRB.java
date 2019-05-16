package pt.ulisboa.tecnico.sec.notary.model;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;

import java.util.HashMap;

public class BRB {
    private boolean sentecho = false;
    private boolean sentready = false;
    private boolean delivered = false;
    private String userID;
    private String timestamp;
    private Message myMessage;
    private HashMap<Integer, Message> echos = new HashMap<>();
    private HashMap<Integer, Message> readys = new HashMap<>();

    public BRB(String userID, String timestamp) {
        this.userID = userID;
        this.timestamp = timestamp;
    }

    public BRB(Message message) {
        this.myMessage = message;
        this.userID = message.getBuyerID();
        this.timestamp = message.getTimestamp();
    }

    public void addEchos(int notaryID, Message message) {
        if(!echos.containsKey(notaryID)) {
            echos.put(notaryID, message);
        }
    }

    public void addReady(int notaryID, Message message) {
        if(!readys.containsKey(notaryID)) {
            readys.put(notaryID, message);
        }
    }

    public String getUserID() {
        return userID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Message getMyMessage() {
        return myMessage;
    }

    public void setMyMessage(Message myMessage) {
        this.myMessage = myMessage;
    }

    public boolean getSentecho() {
        return sentecho;
    }

    public void setSentecho(boolean sentecho) {
        this.sentecho = sentecho;
    }

    public int sizeReadys() {
        return this.readys.size();
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean getSentready() {
        return sentready;
    }

    public void setSentready(boolean sentready) {
        this.sentready = sentready;
    }

    public synchronized Message consensusEcho() {
        System.out.println("Consensus Echo");
        HashMap<Message, Integer> responses = this.totalMessages(this.echos);

        int  N = Notary.getInstance().getN();
        int F = Notary.getInstance().getF();
        for(Message m : responses.keySet()) {
            System.out.println(responses.get(m));
            if(responses.get(m) > (int) Math.ceil((N+F)/2)) {
                return m;
            }
        }
        return null;
    }

    public synchronized Message consesusReady() {
        System.out.println("Consensus Ready");
        System.out.println(this.readys.size());
        HashMap<Message, Integer> responses = this.totalMessages(this.readys);

        int F = Notary.getInstance().getF();
        for(Message m : responses.keySet()) {
            if(responses.get(m) > F) {
                return m;
            }
        }
        return null;
    }

    public synchronized Message consensusDeliver() {
        HashMap<Message, Integer> responses = this.totalMessages(this.readys);

        int F = Notary.getInstance().getF();
        for(Message m : responses.keySet()) {
            if(responses.get(m) > 2 * F) {
                return m;
            }
        }
        return null;
    }

    private HashMap<Message, Integer> totalMessages(HashMap<Integer,Message> hashMap) {
        int total;
        HashMap<Message, Integer> responses = new HashMap<>();
        for(Message m : hashMap.values()){
            if(responses.containsKey(m)) {
                total = responses.get(m) + 1;
                responses.replace(m, total);
            }
            else {
                responses.put(m, 0);
            }
        }
        return responses;
    }

}
