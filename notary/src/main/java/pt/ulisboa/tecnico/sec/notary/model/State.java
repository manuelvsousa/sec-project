package pt.ulisboa.tecnico.sec.notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class State {
    private String ownerID;
    private Boolean onSale;

    public State(@JsonProperty("ownerID") String ownerID, @JsonProperty("onSale") Boolean onSale) {
        this.ownerID = ownerID;
        this.onSale = onSale;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public Boolean getOnSale() {
        return onSale;
    }
}
