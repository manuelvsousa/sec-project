package pt.ulisboa.tecnico.sec.notary.model;

public class Write {
    private String userID;
    private String goodID;
    private long timestamp;
    private String signWrite;
    private boolean onSale;

    public Write(String userID, String goodID, long timestamp, boolean onSale, String signWrite) {
        this.userID = userID;
        this.goodID = goodID;
        this.timestamp = timestamp;
        this.signWrite = signWrite;
        this.onSale = onSale;
    }

    public String getUserID() {
        return userID;
    }

    public String getGoodID() {
        return goodID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSignWrite() {
        return signWrite;
    }

    public boolean isOnSale() {
        return onSale;
    }
}
