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
    public Response getStateOfGood(@QueryParam("id") String id) throws Exception {
        //State s;
        if (id == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("id is null").build());
        }
        try {
            State s = Notary.getInstance().getStateOfGood(id);
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
            String toSign = type + "||" + s.getOwnerID() + "||" + s.getOnSale();
            String sig = Crypto.getInstance().sign(Notary.getInstance().getPrivateKey(),toSign.getBytes());
            Response response = Response.status(200).
                    entity(s).
                    header("Notary-Signature", sig).build();
            return response;
        } catch (GoodNotFoundException e) {
            throw new NotFoundExceptionResponse(e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }



    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID + "" + sig);
        if (goodID == null || goodID == null || sellerID == null || sig == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature are null").build());
        }
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID).getBytes();
            if(!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(sellerID).getPublicKey(),toSign,sig)){
                throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!");
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
        Response response = Response.ok().
                header("Notary-Signature", "dasdasadsdsa").build();
        return response;
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("goodID") String goodID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig) {
        System.out.println(goodID + " " + sellerID + " ");
        if (goodID == null || sellerID == null || sig == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or sellerID  and/or signature are null").build());
        }
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/intention".getBytes());
            byte[] toSign = (type + "||" + goodID + "||" + sellerID).getBytes();
            if(!Crypto.getInstance().checkSignature(Notary.getInstance().getUser(sellerID).getPublicKey(),toSign,sig)){
                throw new InvalidTransactionExceptionResponse("Content of Request Forged!!!");
            }
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

        Response response = Response.ok().
                header("Notary-Signature", "dasdasadsdsa").build();
        return response;
    }
}
