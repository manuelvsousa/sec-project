package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InvalidTransactionExceptionResponse extends WebApplicationException {

    public InvalidTransactionExceptionResponse(String message) {
        super(Response.status(Response.Status.CONFLICT).entity(message).build());
    }
}