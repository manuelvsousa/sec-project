package pt.ulisboa.tecnico.sec.usercli;

import pt.ulisboa.tecnico.sec.notaryclient.exception.UserNotFoundException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UserAbstract {
    private Client client = ClientBuilder.newClient();

    public void buyGood(String goodID, String buyerID, String sellerID) {
        String arr[] = sellerID.split("user");
        String port = arr[1];
        String REST_URI = "http://localhost:909" + port + "/user/user/transfer/buy";
        System.out.println(REST_URI);
        try {
            Response r = client.target(REST_URI).queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).request(MediaType.APPLICATION_JSON).get();
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
