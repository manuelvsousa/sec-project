package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.NotFoundExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.UserDoesNotOwnResourceExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.exception.*;
import pt.ulisboa.tecnico.sec.util.Crypto;

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
    public State getStateOfGood(@QueryParam("id") String id) {
        State s;
        if (id == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("id is null").build());
        }
        try {
            s = Notary.getInstance().getStateOfGood(id);
        } catch (GoodNotFoundException e) {
            throw new NotFoundExceptionResponse(e.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID);
        if (goodID == null || goodID == null || sellerID == null || sig == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature are null").build());
        }
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID).getBytes();
            if(!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(sellerID).getPublicKey(),toSign,sig)){
                throw new WebApplicationException(Response.status(400) // 400 Bad Request
                        .entity("Signature was forged").build());
            }

            Notary.getInstance().addTransaction(goodID, buyerID, sellerID);
        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage());
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage());
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage());
        } catch (TransactionAlreadyExistsException e4) {
            throw new InvalidTransactionExceptionResponse(e4.getMessage());
        } catch (InvalidTransactionException e5) {
            throw new InvalidTransactionExceptionResponse(e5.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return Response.ok().build();
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("goodID") String goodID, @QueryParam("sellerID") String sellerID) {
        System.out.println(goodID + " " + sellerID + " ");
        if (goodID == null || sellerID == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or sellerID are null").build());
        }
        try {
            Notary.getInstance().setIntentionToSell(goodID, sellerID);
        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage());
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage());
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return Response.ok().build();
    }
}
