package pt.ulisboa.tecnico.sec.notary.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User implements Serializable {
    private String userID;

    private List<Good> goods = new ArrayList<>();

    private PublicKey publicKey;

    public User(@JsonProperty("userID") String userID, @JsonProperty("publicKey") PublicKey publicKey) {
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

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public List<Good> getGoods() {
        return this.goods;
    }
}
