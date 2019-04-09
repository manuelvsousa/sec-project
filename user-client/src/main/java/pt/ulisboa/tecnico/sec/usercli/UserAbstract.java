package pt.ulisboa.tecnico.sec.usercli;

import pt.ulisboa.tecnico.sec.notaryclient.exception.UserNotFoundException;
import pt.ulisboa.tecnico.sec.util.Crypto;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.util.Base64;

public class UserAbstract {
    private Client client = ClientBuilder.newClient();
    private PrivateKey privateKey;

    public UserAbstract(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public void buyGood(String goodID, String buyerID, String sellerID) throws Exception {
        String arr[] = sellerID.split("user");
        String port = arr[1];
        String REST_URI = "http://localhost:909" + port + "/user/user/transfer/buy";
        System.out.println(REST_URI);
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/user/user/transfer/buy".getBytes());
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            Response r = client.target(REST_URI).queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).queryParam("signature", sig).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r);
        } catch (Exception e) {
            throw e;
        }
    }

    private void verifyResponse(Response r) {
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
