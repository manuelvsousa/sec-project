package pt.ulisboa.tecnico.sec.model;

import java.io.Serializable;
import java.util.Date;

//@XmlRootElement       //only needed if we also want to generate XML
public class Good implements Serializable {

    private String goodID;
    private boolean onSale = false;

    public Good(String goodID, boolean onSale) {
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
