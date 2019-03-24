package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.InvalidTransactionExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.NotFoundExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.UserDoesNotOwnResourceExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.exception.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.notary.model.exception.InvalidTransactionException;
import pt.ulisboa.tecnico.sec.notary.model.exception.UserDoesNotOwnGood;
import pt.ulisboa.tecnico.sec.notary.model.exception.UserNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Response transferGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID);
        if (goodID == null || goodID == null || sellerID == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID are null").build());
        }

        try {
            Notary.getInstance().addTransaction(goodID, buyerID, sellerID);
        } catch (GoodNotFoundException e1) {
            throw new NotFoundExceptionResponse(e1.getMessage());
        } catch (UserNotFoundException e2) {
            throw new NotFoundExceptionResponse(e2.getMessage());
        } catch (UserDoesNotOwnGood e3) {
            throw new UserDoesNotOwnResourceExceptionResponse(e3.getMessage());
        } catch (InvalidTransactionException e4) {
            throw new InvalidTransactionExceptionResponse(e4.getMessage());
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
