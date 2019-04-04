package pt.ulisboa.tecnico.sec.user.jaxrs.resource.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class NotFoundExceptionResponse extends WebApplicationException {

    public NotFoundExceptionResponse(String message) {
        super(Response.status(Response.Status.NOT_FOUND).entity(message).build());
    }
}