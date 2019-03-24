package pt.ulisboa.tecnico.sec.model;

import pt.ulisboa.tecnico.sec.jaxrs.application.Notary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//@XmlRootElement       //only needed if we also want to generate XML
public class User implements Serializable {
    private String userID;

    private List<Good> goods = new ArrayList<>();

    private String publicKey;

    public User(String userID, String publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
    }

    public String getUserID() {
        return this.userID;
    }

    public void addGood(Good g) {
        goods.add(g);
        Notary.getInstance().save();
    }

    public String getPublicKey() {
        return publicKey;
    }

    public List<Good> getGoods() {
        return this.goods;
    }
}
