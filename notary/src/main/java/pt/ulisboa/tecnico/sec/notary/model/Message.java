package pt.ulisboa.tecnico.sec.notary.model;

import java.util.Objects;

public class Message {
    private String type;
    private String goodID;
    private String buyerID;
    private String sellerID;
    private String timestamp;
    private String signWrite;
    private boolean onSale;

    public Message(String type, String goodID, String buyerID, String  sellerID, String timestamp, String signWrite) {
        this.type = type;
        this.goodID = goodID;
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.timestamp = timestamp;
        this.signWrite = signWrite;
        this.onSale = false;
    }

    public Message(String type, String goodID, String buyerID, String timestamp, String signWrite) {
        this.type = type;
        this.goodID = goodID;
        this.buyerID = buyerID;
        this.sellerID = "";
        this.timestamp = timestamp;
        this.signWrite = signWrite;
        this.onSale = true;
    }

    public String getType() {
        return type;
    }

    public String getBuyerID() {
        return buyerID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSignWrite() {
        return signWrite;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public String getGoodID() {
        return goodID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return onSale == message.onSale &&
                type.equals(message.type) &&
                goodID.equals(message.goodID) &&
                buyerID.equals(message.buyerID) &&
                sellerID.equals(message.sellerID) &&
                timestamp.equals(message.timestamp) &&
                signWrite.equals(message.signWrite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, goodID, buyerID, sellerID, timestamp, signWrite, onSale);
    }
}
