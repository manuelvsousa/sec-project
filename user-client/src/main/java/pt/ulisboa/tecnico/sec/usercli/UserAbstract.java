package pt.ulisboa.tecnico.sec.usercli;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pt.ulisboa.tecnico.sec.usercli.exception.UserNotFoundException;
import pt.ulisboa.tecnico.sec.usercli.exception.InvalidSignature;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.util.*;
import java.io.IOException;
import java.io.File;

public class UserAbstract {
    private Client client = ClientBuilder.newClient();
    private PrivateKey privateKey;
    List<HashMap<String, String >> listOfNotaryCertificates;


    public UserAbstract(PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.listOfNotaryCertificates = new ArrayList<>();
    }

    public void buyGood(String goodID, String buyerID, String sellerID) throws Exception {
        String arr[] = sellerID.split("user");
        String port = arr[1];
        String REST_URI = "http://localhost:909" + port + "/user/user/transfer/buy";
        System.out.println(REST_URI);
        try {
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (goodID + "||" + buyerID + "||" + sellerID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            System.out.println("Signature UserAbstract: " + sig);
            Response r = client.target(REST_URI).queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).queryParam("signatureBuyer", sig).queryParam("nonceBuyer", nonce).request(MediaType.APPLICATION_JSON).get();
            if(r.getStatus() != 200){
                throw new Exception(r.readEntity(String.class));
            }
            this.verifyResponse(r, toSign, sellerID);
            String json = r.readEntity(String.class);
            HashMap<String, String> notaryCertificate = new Gson().fromJson(
                    json, new TypeToken<HashMap<String, String>>() {}.getType()
            );
            this.listOfNotaryCertificates.add(notaryCertificate);
        } catch (Exception e) {
            throw e;
        }
    }

    
    private void verifyResponse(Response r, byte[] toSign, String sellerID) {
        try {
            String sig = r.getHeaderString("Seller-Signature");
            if (sig == null) {
                throw new InvalidSignature("Signature from user was null");
            } else {
                String path = new File(System.getProperty("user.dir")).getParent();
                PublicKey publicKey = KeyReader.getInstance().readPublicKey(sellerID, path);
                if (!Crypto.getInstance().checkSignature(publicKey, toSign, sig)) {
                    throw new InvalidSignature("Signature from user was forged");
                }
            }
        }catch (GeneralSecurityException gse) {
            System.out.println("GeneralSecurityException caught");
        } catch (IOException io) {
            System.out.println("IOException caught");
        }

        if (r.getStatus() == 200) {
            return;
        } else {
            String cause = r.readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error " + r.getStatus() + " is null");
            }
            if (r.getStatus() == 404) {
                throw new UserNotFoundException(cause);
            } else {
                throw new RuntimeException("Unable to process request, ERROR " + r.getStatus() + " Received!");
            }
        }
    }
}
