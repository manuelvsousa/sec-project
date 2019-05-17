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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Path("/write")
public class WriteResource {
    private final Lock lock = new ReentrantLock();
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



        Message m = new Message(typeM, goodID, buyerID, sellerID, nonceM, signWrite, Boolean.valueOf(onSale));


        BRB brb = Notary.getInstance().addBRBEcho(m, Integer.parseInt(notaryID));
        m = brb.consensusEcho();
        if(!brb.getSentready() && m!=null) {
                int i = Notary.getInstance().checksReceivedWriteFromOtherReplics(m.getBuyerID(), m.getTimestamp());
                try {
                    Notary.getInstance().sendReady(i);
                } catch (Exception e) {
                    System.out.println("Unable to send ready");
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
        System.out.println("\n\nReceived Parameters Ready:\n");

        if (typeM == null || goodID == null || sellerID == null || buyerID == null || nonceM == null || signWrite == null || onSale == null || nonce == null || sig == null || notaryID == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature and/or nonce and/or nonceBuyer and/or sigBuyer and/or pow null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/write/sendReady".getBytes());

        Checker.getInstance().checkResponseWrite(type, typeM, goodID, buyerID, sellerID, nonceM, signWrite, onSale, nonce, notaryID, sig);

        Message m = new Message(typeM, goodID, buyerID, sellerID, nonceM, signWrite, Boolean.valueOf(onSale));
        int i = Notary.getInstance().checksReceivedWriteFromOtherReplics(buyerID, nonceM);


         BRB brb =  Notary.getInstance().addBRBReady(m, Integer.parseInt(notaryID));
         m = brb.consesusReady();
        if(m != null && !brb.getSentready()) {
            try {
                Notary.getInstance().sendReady(i);
            }
            catch (Exception e) {
                System.out.println("Unable to send ready");
            }
        }
        /**
        m = brb.consensusDeliver();
        if(m != null && !brb.isDelivered()) {
            if(m.getType().equals("intentionToSell")) {
                Notary.getInstance().setIntentionToSell(m.getGoodID(), m.getBuyerID(), m.getTimestamp(), m.getSignWrite());
            }
            else{
                Notary.getInstance().addTransaction(goodID, buyerID, sellerID, nonce, signWrite, nonceM);
            }

        }
        **/
        Response response = Response.ok().build();

        return response;
    }
}
