package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InvalidProofOfWork extends WebApplicationException {

    public InvalidProofOfWork() {
        super(Response.status(Response.Status.BAD_REQUEST).entity("Invalid Proof of Work").build());
    }
}