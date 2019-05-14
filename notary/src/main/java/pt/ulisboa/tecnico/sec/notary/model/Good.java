package pt.ulisboa.tecnico.sec.notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ulisboa.tecnico.sec.util.Crypto;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Good implements Serializable {

    private String goodID;
    private long timestamp;
    private String signWrite;
    private boolean onSale = false;

    public Good(@JsonProperty("goodID") String goodID, @JsonProperty("onSale") boolean onSale, @JsonProperty("owner") String owner) {
        this.goodID = goodID;
        this.onSale = onSale;
        this.timestamp = 0;
        this.signWrite = goodID + " || "+ onSale + " || " +  timestamp + " || " + owner;
    }

    public Good(@JsonProperty("goodID") String goodID, @JsonProperty("onSale") boolean onSale, @JsonProperty("owner") String owner, @JsonProperty("signWrite") String signWrite) {
        this.goodID = goodID;
        this.onSale = onSale;
        this.timestamp = 0;
        this.signWrite = signWrite;
    }

    //It isn't used, otherwise fix signWrite
    public Good(String goodID) {
        this.goodID = goodID;
        this.timestamp = 0;
        this.signWrite = "";
    }

    public boolean onSale() {
        return onSale;
    }


    public String getID() {
        return goodID;
    }

    public void setOnSale(boolean s) {
        onSale = s;
    }

    public long getTimestamp() { return timestamp; }

    public String getSignWrite() { return signWrite; }

    public void setTimestamp(long t) { timestamp = t; }

    public void setSignWrite(String sw) { signWrite = sw; }
}
