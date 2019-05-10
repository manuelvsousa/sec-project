package pt.ulisboa.tecnico.sec.notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class State {
    private String ownerID;
    private Boolean onSale;
    private long timestamp;
    private String signWrite;

    public State(@JsonProperty("ownerID") String ownerID, @JsonProperty("onSale") Boolean onSale, @JsonProperty("timestamp")  long timestamp, @JsonProperty("signWrite") String signWrite) {
        this.ownerID = ownerID;
        this.onSale = onSale;
        this.timestamp = timestamp;
        this.signWrite = signWrite;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public Boolean getOnSale() {
        return onSale;
    }

    public long getTimestamp() { return  timestamp; }

    public String getSignWrite() { return  signWrite; }
}
