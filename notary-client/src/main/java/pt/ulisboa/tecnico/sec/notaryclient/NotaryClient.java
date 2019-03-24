package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.exception.GoodNotFoundException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;


public class NotaryClient {

    private static final String REST_URI = "http://localhost:9091/notary/notary";
    private Client client = ClientBuilder.newClient();

//    public Response createJsonEmployee(Employee emp) {
//        return client.target(REST_URI).request(MediaType.APPLICATION_JSON).post(Entity.entity(emp, MediaType.APPLICATION_JSON));
//    }

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
        return null;
    }

//    public Response createXmlEmployee(Employee emp) {
//        return client.target(REST_URI).request(MediaType.APPLICATION_XML).post(Entity.entity(emp, MediaType.APPLICATION_XML));
//    }

//    public User getXmlEmployee(int id) {
//
//        return client.target(REST_URI).path(String.valueOf(id)).request(MediaType.APPLICATION_XML).get(Employee.class);
//    }
}