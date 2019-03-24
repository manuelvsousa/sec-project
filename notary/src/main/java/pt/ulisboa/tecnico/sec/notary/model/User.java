package pt.ulisboa.tecnico.sec.notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//@XmlRootElement       //only needed if we also want to generate XML
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User implements Serializable {
    private String userID;

    private List<Good> goods = new ArrayList<>();

    private String publicKey;

    public User(@JsonProperty("userID") String userID, @JsonProperty("publicKey") String publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
    }

    public String getID() {
        return this.userID;
    }

    public void addGood(@JsonProperty("good") Good g) {
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
