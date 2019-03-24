package pt.ulisboa.tecnico.sec.jaxrs.resource.goods;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import pt.ulisboa.tecnico.sec.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.model.Status;
import pt.ulisboa.tecnico.sec.model.exception.GroupNotFoundException;

@Path("/goods")
public class GoodsResource {

    @GET
    @Path("/getStatus")
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public Status getPerson(@QueryParam("id") String id) {
        Status s;
        if(id == null){
            throw new WebApplicationException(400); // 400 Bad Request
        }
        try {
            s = Notary.getInstance().getGoodStatus(id);
        } catch (GroupNotFoundException e) {
            throw new WebApplicationException(404); // 404 Not Found
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("id") String goodID, @QueryParam("buyer") String buyerID,@QueryParam("seller") String sellerID) throws Exception{
        System.out.println(goodID + " " + buyerID + " " + sellerID);
        if(goodID == null || buyerID == null || sellerID == null){
            throw new WebApplicationException(400); // 400 Bad Request
        }
        Notary.getInstance().addTransaction(goodID,buyerID,sellerID);
        return Response.ok().build();
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("id") String goodID, @QueryParam("seller") String sellerID) throws Exception{
        System.out.println(goodID + " " + sellerID + " ");
        if(goodID == null || sellerID == null){
            throw new WebApplicationException(400); // 400 Bad Request
        }
        Notary.getInstance().setIntentionToSell(goodID,sellerID);
        return Response.ok().build();
    }
}
