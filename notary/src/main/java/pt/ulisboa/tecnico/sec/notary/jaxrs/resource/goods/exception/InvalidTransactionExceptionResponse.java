package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InvalidTransactionExceptionResponse extends WebApplicationException {

    public InvalidTransactionExceptionResponse(String message, String sig, String nonce) {
        super(Response.status(Response.Status.CONFLICT).
                header("Notary-Signature", sig).
                header("Notary-Nonce", nonce).entity(message).build());
    }
}