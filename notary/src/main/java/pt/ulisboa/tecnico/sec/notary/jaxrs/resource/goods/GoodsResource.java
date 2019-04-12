package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.GoodNotOnSaleResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.NotFoundExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.UserDoesNotOwnResourceExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.exception.*;
import pt.ulisboa.tecnico.sec.notary.util.Checker;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;

@Path("/goods")
public class GoodsResource {

    @GET
    @Path("/getStatus")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getStateOfGood(@QueryParam("id") String id, @QueryParam("userID") String userID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce) throws Exception {
        System.out.println("\n\nReceived Paramenters:\n");
        System.out.println("goodID: " + id + "\nuserID: " + userID + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce);
        if (id == null || userID == null || sig == null || nonce == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("id and/or userID and/or sig and/or nonce  are null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
        String nonceNotary = String.valueOf(System.currentTimeMillis());
        byte[] toSignToSend = (type + "||" + id + "||" + userID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignToSend, false);
        try {
            State s = Notary.getInstance().getStateOfGood(id);
            byte[] toSign = (type + "||" + id + "||" + userID + "||" + nonce).getBytes();

            Checker.getInstance().checkResponse(toSign, userID, sig, nonce, nonceNotary, sigNotary);

            toSignToSend = (type + "||" + id + "||" + userID + "||" + nonce + "||" + s.getOnSale() + "||" + s.getOwnerID() + "||" + nonceNotary).getBytes();
            sigNotary = Notary.getInstance().sign(toSignToSend, false);

            System.out.println("\n\n\nAbout to Send:\n");
            System.out.println("Notary-Signature: " + sigNotary + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignToSend));
            Response response = Response.status(200).
                    entity(s).
                    header("Notary-Signature", sigNotary).
                    header("Notary-Nonce", nonceNotary).build();
            return response;
        } catch (GoodNotFoundException e) {
            throw new NotFoundExceptionResponse(e.getMessage(), sigNotary, nonceNotary);
        } catch (Exception e) {
            throw e;
        }
    }


    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce, @QueryParam("nonceBuyer") String nonceBuyer, @QueryParam("sigBuyer") String sigBuyer) throws Exception {
        System.out.println("\n\nReceived Paramenters:\n");
        System.out.println("goodID: " + goodID + "\nbuyerID: " + buyerID + "\nsellerID: " + sellerID + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce + "\nnonce (from buyer): " + nonceBuyer + "\nsignature (from buyer): " + sigBuyer);
        if (goodID == null || goodID == null || sellerID == null || sig == null || nonce == null || nonceBuyer == null || sigBuyer == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature and/or nonce and/or nonceBuyer and/or sigBuyer null").build());
        }
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
        String nonceNotary = String.valueOf((System.currentTimeMillis()));
        byte[] toSignResponse = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceBuyer + "||" + sigBuyer + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse, true);
        try {
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceBuyer + "||" + sigBuyer).getBytes();

            Notary.getInstance().doIntegrityCheck(goodID, buyerID, sellerID);

            Checker.getInstance().checkResponse(toSign, sellerID, sig, nonce, nonceNotary, sigNotary); // Check integrity of message and nonce validaty

            byte[] toSign2 = (goodID + "||" + buyerID + "||" + sellerID + "||" + nonceBuyer).getBytes();

            Checker.getInstance().checkResponse(toSign2, buyerID, sigBuyer, nonceBuyer, nonceNotary, sigNotary); // Check integrity of message send by the buyer to the seller


            /* Doing this might invalidate the transfer in case the buyer makes a request that arrives first then this one.
             * But this verification wont allow a malicious seller to reuse a previously buyer transfer request in another
             * future transfer for the same good (in case a certain seller sells the good, then gets it back, and tries to preform a transfer request again without the buyer knowing)
             * */
            Notary.getInstance().addTransaction(goodID, buyerID, sellerID, nonceNotary);

            System.out.println("\n\n\nAbout to Send:\n");
            System.out.println("Notary-Signature: " + sigNotary + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignResponse));
            Response response = Response.ok().
                    header("Notary-Signature", sigNotary).
                    header("Notary-Nonce", nonceNotary).build();
            return response;

        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage(), sigNotary, nonceNotary);
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage(), sigNotary, nonceNotary);
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage(), sigNotary, nonceNotary);
        } catch (GoodNotOnSale gg) {
            throw new GoodNotOnSaleResponse(gg.getMessage(), sigNotary, nonceNotary);
        } catch (TransactionAlreadyExistsException e4) {
            throw new InvalidTransactionExceptionResponse(e4.getMessage(), sigNotary, nonceNotary);
        } catch (InvalidTransactionException e5) {
            throw new InvalidTransactionExceptionResponse(e5.getMessage(), sigNotary, nonceNotary);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("goodID") String goodID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce) throws Exception {
        System.out.println("\n\nReceived Paramenters:\n");
        System.out.println("goodID: " + goodID + "\nsellerID: " + sellerID + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce);
        if (goodID == null || sellerID == null || sig == null || nonce == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or sellerID and/or signature and/or nonce are null").build());
        }
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/intention".getBytes());
        String nonceNotary = String.valueOf((System.currentTimeMillis()));
        byte[] toSignResponse = (type + "||" + goodID + "||" + sellerID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse, false);
        try {
            byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + nonce).getBytes();

            Checker.getInstance().checkResponse(toSign, sellerID, sig, nonce, nonceNotary, sigNotary); // Check integrity of message and nonce validaty

            Notary.getInstance().setIntentionToSell(goodID, sellerID);


            System.out.println("\n\n\nAbout to Send:\n");
            System.out.println("Notary-Signature: " + sigNotary + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignResponse));
            Response response = Response.ok().
                    header("Notary-Signature", sigNotary).
                    header("Notary-Nonce", nonceNotary).build();
            return response;
        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage(), sigNotary, nonceNotary);
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage(), sigNotary, nonceNotary);
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage(), sigNotary, nonceNotary);
        } catch (Exception e) {
            throw e;
        }
    }
}
