package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class GoodNotFoundExceptionResponse extends WebApplicationException {

    public GoodNotFoundExceptionResponse(String message) {
        super(Response.status(Response.Status.NOT_FOUND).entity(message).build());
    }
}