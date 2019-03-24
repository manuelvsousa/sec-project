package pt.ulisboa.tecnico.sec.jaxrs.resource.goods;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import pt.ulisboa.tecnico.sec.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.model.Status;
import pt.ulisboa.tecnico.sec.model.exception.GroupNotFoundException;

import java.util.concurrent.ExecutionException;

@Path("/goods")
public class GoodsResource {

    @Path("/getStatus")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public Status getPerson(@QueryParam("id") String id) {
        Status s;
        try {
            s = Notary.getInstance().getGoodStatus(id);
        } catch (GroupNotFoundException e) {
            throw new WebApplicationException(404);
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

//    @POST
//    @Consumes({MediaType.APPLICATION_JSON})
//    @Produces({MediaType.TEXT_PLAIN})
//    @Path("/post")
//    public String postPerson(Person pers) throws Exception{
//
//        System.out.println("First Name = "+pers.getFirstName());
//        System.out.println("Last Name  = "+pers.getLastName());
//
//        return "ok";
//    }

}
