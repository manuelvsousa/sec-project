package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.exception.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserDoesNotOwnGoodException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserNotFoundException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class NotaryClient {

    private static final String REST_URI = "http://localhost:9091/notary/notary";
    private Client client = ClientBuilder.newClient();

    public State getStateOfGood(String id) {
        try {
            State s = client.target(REST_URI + "/goods/getStatus").queryParam("id", id).request(MediaType.APPLICATION_JSON).get(State.class);
            return s;
        } catch (NotFoundException e) {
            String cause = e.getResponse().readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error 404 is null");
            }
            if (cause.toLowerCase().contains("good".toLowerCase())) {
                throw new GoodNotFoundException(cause);
            }
        }
        throw new RuntimeException("Unknown Error");
    }

    public void transferGood(String goodID, String buyerID, String sellerID) {
        try {
            Response r = client.target(REST_URI + "/goods/transfer").queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r);
            return;
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
                if (cause.toLowerCase().contains("good".toLowerCase())) {
                    throw new GoodNotFoundException(cause);
                } else if (cause.toLowerCase().contains("user".toLowerCase())) {
                    throw new UserNotFoundException(cause);
                }
            } else if (r.getStatus() == 417) {
                throw new UserDoesNotOwnGoodException(cause);
            } else if (r.getStatus() == 409) { //conflict
                throw new UserDoesNotOwnGoodException(cause);
            } else {
                throw new RuntimeException("Unable to process request, ERROR " + r.getStatus() + " Received!");
            }
        }
    }

    public void intentionToSell(String goodID, String sellerID) {
        try {
            Response r = client.target(REST_URI + "/goods/intention").queryParam("goodID", goodID).queryParam("sellerID", sellerID).request(MediaType.APPLICATION_JSON).get();
            this.verifyResponse(r);
            return;
        } catch (Exception e) {
            throw e;
        }
    }

}