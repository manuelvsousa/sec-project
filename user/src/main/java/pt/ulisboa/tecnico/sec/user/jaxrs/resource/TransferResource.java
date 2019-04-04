package pt.ulisboa.tecnico.sec.user.jaxrs.resource;

import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;
import pt.ulisboa.tecnico.sec.user.jaxrs.application.UserServ;
import pt.ulisboa.tecnico.sec.user.model.exception.UserNotFoundException;

import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("/transfer")
public class TransferResource {

    @GET
    @Path("/buy")
    public Response buyGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID);
       if (goodID == null || goodID == null || sellerID == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID are null").build());
        }

        String port = System.getProperty("port");
        System.out.println("Port:" + port);
        String userServID = "user" + port;
        if (!userServID.equals(sellerID)) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("serverID don't correspond to sellerID").build());
        }
        try {
            UserServ.getInstance().getUser(buyerID);
        } catch (UserNotFoundException e) {
            throw new NotFoundException(e);
        }
        NotaryClient notaryClient = new NotaryClient(sellerID);
        notaryClient.transferGood(goodID, buyerID);
        return Response.ok().build();
    }
}
