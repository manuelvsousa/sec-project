package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.model.BRB;
import pt.ulisboa.tecnico.sec.notary.model.Message;
import pt.ulisboa.tecnico.sec.notary.util.Checker;
import pt.ulisboa.tecnico.sec.util.Crypto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PublicKey;
import java.util.Base64;

@Path("/write")
public class WriteResource {
    @GET
    @Path("/echo")
    @Produces({MediaType.APPLICATION_JSON})
    public Response sendEcho(@QueryParam("typeM") String typeM, @QueryParam("goodID") String goodID,
                             @QueryParam("sellerID") String sellerID, @QueryParam("buyerID") String buyerID, @QueryParam("nonceM") String nonceM,
                             @QueryParam("signWrite") String signWrite, @QueryParam("onSale") String onSale, @QueryParam("nonce") String nonce,
                             @QueryParam("sig") String sig,@QueryParam("notaryID") String notaryID) {
        System.out.println("\n\nReceived Parameters Echo:\n");

        if (typeM == null || goodID == null || sellerID == null || buyerID == null || nonceM == null || signWrite == null || onSale == null || nonce == null || sig == null || notaryID == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature and/or nonce and/or nonceBuyer and/or sigBuyer and/or pow null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/write/sendEcho".getBytes());

        Checker.getInstance().checkResponseWrite(type, typeM, goodID, buyerID, sellerID, nonceM, signWrite, onSale, nonce, notaryID, sig);



        Message m = new Message(typeM, goodID, buyerID, sellerID, nonceM, signWrite);
        int i = Notary.getInstance().checksReceivedWriteFromOtherReplics(buyerID, nonceM);
        if(i == -1) {
            Notary.getInstance().createBRBEcho(m, Integer.parseInt(notaryID));
        }
        else {
            Notary.getInstance().addBRBEcho(m, Integer.parseInt(notaryID), i);
            BRB brb = Notary.getInstance().getBRB(i);
            m = brb.consensusEcho();
            if(m != null && !brb.getSentready()) {
                Notary.getInstance().sendReady(i);
            }
        }

        Response response = Response.ok().build();

        return response;
    }

    @GET
    @Path("/ready")
    @Produces({MediaType.APPLICATION_JSON})
    public Response sendReady(@QueryParam("typeM") String typeM, @QueryParam("goodID") String goodID,
                             @QueryParam("sellerID") String sellerID, @QueryParam("buyerID") String buyerID, @QueryParam("nonceM") String nonceM,
                             @QueryParam("signWrite") String signWrite, @QueryParam("onSale") String onSale, @QueryParam("nonce") String nonce,
                             @QueryParam("sig") String sig,@QueryParam("notaryID") String notaryID) {
        System.out.println("\n\nReceived Parameters Echo:\n");

        if (typeM == null || goodID == null || sellerID == null || buyerID == null || nonceM == null || signWrite == null || onSale == null || nonce == null || sig == null || notaryID == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature and/or nonce and/or nonceBuyer and/or sigBuyer and/or pow null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/write/sendReady".getBytes());

        Checker.getInstance().checkResponseWrite(type, typeM, goodID, buyerID, sellerID, nonceM, signWrite, onSale, nonce, notaryID, sig);

        Message m = new Message(typeM, goodID, buyerID, sellerID, nonceM, signWrite);
        int i = Notary.getInstance().checksReceivedWriteFromOtherReplics(buyerID, nonceM);

        if(i == -1) {
            Notary.getInstance().createBRBReady(m, Integer.parseInt(notaryID));
        }
        else {
            Notary.getInstance().addBRBReady(m, Integer.parseInt(notaryID), i);
            BRB brb = Notary.getInstance().getBRB(i);
            m = brb.consesusReady();
            if(m != null && !brb.getSentready()) {
                Notary.getInstance().sendReady(i);
            }
        }

        Response response = Response.ok().build();

        return response;
    }
}
