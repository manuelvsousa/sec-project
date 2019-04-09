package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class NotFoundExceptionResponse extends WebApplicationException {

    public NotFoundExceptionResponse(String message, String sig, String nonce) {
        super(Response.status(Response.Status.NOT_FOUND).
                header("Notary-Signature", sig).
                header("Notary-Nonce", nonce).entity(message).build());
    }
}