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

    private long lastNonce;

    private List<String> pows = new ArrayList<>();

    public User(@JsonProperty("userID") String userID, @JsonProperty("publicKey") PublicKey publicKey) {
        this.userID = userID;
        this.publicKey = publicKey;
        this.lastNonce = System.currentTimeMillis();
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

    public long getLastNonce() {
        return this.lastNonce;
    }

    public void addPow(String pow) {
        this.pows.add(pow);
    }

    public boolean inPows(String pow) {
        return this.pows.contains(pow);
    }

    public void setLastNonce(long lastNonce) {
        this.lastNonce = lastNonce;
        Notary.getInstance().save();
    }

    public void removeGood(Good g) {
        this.goods.remove(g);
        Notary.getInstance().save();
    }
}
