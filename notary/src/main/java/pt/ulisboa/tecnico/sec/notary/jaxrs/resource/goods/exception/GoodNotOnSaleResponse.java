package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class GoodNotOnSaleResponse extends WebApplicationException {

    public GoodNotOnSaleResponse(String message, String sig, String nonce) {
        super(Response.status(Response.Status.NOT_ACCEPTABLE).
                header("Notary-Signature", sig).
                header("Notary-Nonce", nonce).entity(message).build());
    }
}