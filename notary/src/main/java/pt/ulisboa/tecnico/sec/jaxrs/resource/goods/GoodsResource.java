package pt.ulisboa.tecnico.sec.jaxrs.resource.goods;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import pt.ulisboa.tecnico.sec.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.model.Status;

@Path("/goods")
public class GoodsResource {

    @Path("/getStatus")
    @GET
    @Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public Status getPerson(@QueryParam("id") String id){
        //Notary.getInstance().addUser(u);
        //return Response.ok().entity(p).build();
        System.out.println(Notary.getInstance().getGoodStatus(id).toString());
        return Notary.getInstance().getGoodStatus(id);
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
