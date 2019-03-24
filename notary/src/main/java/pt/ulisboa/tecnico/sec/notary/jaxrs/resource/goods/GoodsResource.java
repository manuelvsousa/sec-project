package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.GoodNotFoundExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.GroupNotFoundExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.exception.GoodNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/goods")
public class GoodsResource {

    @GET
    @Path("/getStatus")
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public State getStateOfGood(@QueryParam("id") String id) throws GroupNotFoundExceptionResponse {
        State s;
        if (id == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("error").build());
        }
        try {
            s = Notary.getInstance().getStateOfGood(id);
        } catch (GoodNotFoundException e) {
            throw new GoodNotFoundExceptionResponse(e.getMessage());
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("id") String goodID, @QueryParam("buyer") String buyerID, @QueryParam("seller") String sellerID) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID);
        if (goodID == null || buyerID == null || sellerID == null) {
            throw new WebApplicationException(400); // 400 Bad Request
        }

        Notary.getInstance().addTransaction(goodID, buyerID, sellerID);
        return Response.ok().build();
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("id") String goodID, @QueryParam("seller") String sellerID) throws Exception {
        System.out.println(goodID + " " + sellerID + " ");
        if (goodID == null || sellerID == null) {
            throw new WebApplicationException(400); // 400 Bad Request
        }
        Notary.getInstance().setIntentionToSell(goodID, sellerID);
        return Response.ok().build();
    }
}
