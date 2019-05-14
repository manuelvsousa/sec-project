package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InvalidSignatureWrite extends WebApplicationException {
    public  InvalidSignatureWrite() {
        super(Response.status(Response.Status.BAD_REQUEST).entity("Invalid Write Signature").build());
    }

}
