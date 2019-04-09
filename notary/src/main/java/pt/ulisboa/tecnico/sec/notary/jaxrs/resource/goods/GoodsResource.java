package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
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
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public Response getStateOfGood(@QueryParam("id") String id, @QueryParam("userID") String userID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce) throws Exception {
        System.out.println(id + " " + userID + " " + sig + " " + nonce);
        if (id == null || userID == null || sig == null || nonce == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("id and/or userID and/or sig and/or nonce  are null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
        String nonceNotary = String.valueOf((System.currentTimeMillis() / 1000L));
        byte[] toSignToSend = (type + "||" + id + "||" + userID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignToSend);

        try {
            State s = Notary.getInstance().getStateOfGood(id);
            byte[] toSign = (type + "||" + id + "||" + userID + "||" + nonce).getBytes();

            Checker.getInstance().checkResponse(toSign, userID, sig, nonce); // Check integrity of message and nonce validaty
            Response response = Response.status(200).
                    entity(s).
                    header("Notary-Signature", sigNotary).
                    header("Notary-Nonce", nonceNotary).build();
            return response;
        } catch (GoodNotFoundException e) {
            throw new NotFoundExceptionResponse(e.getMessage(),sigNotary,nonceNotary);
        } catch (Exception e) {
            throw e;
        }
    }


    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID + " " + sig + " " + nonce);
        if (goodID == null || goodID == null || sellerID == null || sig == null || nonce == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature and/or nonce are null").build());
        }
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
        String nonceNotary = String.valueOf((System.currentTimeMillis() / 1000L));
        byte[] toSignResponse = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse);
        try {
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce).getBytes();

            Checker.getInstance().checkResponse(toSign, sellerID, sig, nonce); // Check integrity of message and nonce validaty

            Notary.getInstance().addTransaction(goodID, buyerID, sellerID);


            Response response = Response.ok().
                    header("Notary-Signature", sigNotary).
                    header("Notary-Nonce", nonceNotary).build();
            return response;

        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage(),sigNotary,nonceNotary);
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage(),sigNotary,nonceNotary);
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage(),sigNotary,nonceNotary);
        } catch (TransactionAlreadyExistsException e4) {
            throw new InvalidTransactionExceptionResponse(e4.getMessage(),sigNotary,nonceNotary);
        } catch (InvalidTransactionException e5) {
            throw new InvalidTransactionExceptionResponse(e5.getMessage(),sigNotary,nonceNotary);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("goodID") String goodID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce) throws Exception {
        System.out.println(goodID + " " + sellerID + " ");
        if (goodID == null || sellerID == null || sig == null || nonce == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or sellerID and/or signature and/or nonce are null").build());
        }
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/intention".getBytes());
        String nonceNotary = String.valueOf((System.currentTimeMillis() / 1000L));
        byte[] toSignResponse = (type + "||" + goodID + "||" + sellerID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse);
        try {
            byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + nonce).getBytes();

            Checker.getInstance().checkResponse(toSign, sellerID, sig, nonce); // Check integrity of message and nonce validaty

            Notary.getInstance().setIntentionToSell(goodID, sellerID);

            Response response = Response.ok().
                    header("Notary-Signature", sigNotary).
                    header("Notary-Nonce", nonceNotary).build();
            return response;
        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage(),sigNotary,nonceNotary);
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage(),sigNotary,nonceNotary);
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage(),sigNotary,nonceNotary);
        } catch (Exception e) {
            throw e;
        }
    }
}
