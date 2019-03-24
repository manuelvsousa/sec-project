package pt.ulisboa.tecnico.sec.notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Good implements Serializable {

    private String goodID;
    private boolean onSale = false;

    public Good(@JsonProperty("goodID") String goodID, @JsonProperty("onSale") boolean onSale) {
        this.goodID = goodID;
        this.onSale = onSale;
    }

    public Good(String goodID) {
        this.goodID = goodID;
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


}
