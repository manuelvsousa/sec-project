package pt.ulisboa.tecnico.sec.user.jaxrs.resource;

import pt.ulisboa.tecnico.sec.notaryclient.NotaryClient;
import pt.ulisboa.tecnico.sec.user.jaxrs.application.UserServ;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@Path("/transfer")
public class TransferResource {

    @GET
    @Path("/buy")
    public Response buyGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID, @QueryParam("signatureBuyer") String signatureBuyer, @QueryParam("nonceBuyer") String nonceBuyer) throws Exception {
        System.out.println(goodID + " " + buyerID + " " + sellerID + " " + signatureBuyer);
        if (goodID == null || goodID == null || sellerID == null || signatureBuyer == null || nonceBuyer == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature are null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/user/user/transfer/buy".getBytes());
        byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonceBuyer).getBytes();

        String path = new File(System.getProperty("user.dir")).getParent();
        PublicKey publicKey = KeyReader.getInstance().readPublicKey(buyerID, path);
        Crypto.getInstance().checkSignature(publicKey, toSign, signatureBuyer);

        String port = System.getProperty("port");
        String userServID = "user" + port;
        if (!userServID.equals(sellerID)) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("serverID don't correspond to sellerID").build());
        }
        try {
            PrivateKey privateKey = UserServ.getInstance().getPrivateKey();
            NotaryClient notaryClient = new NotaryClient(buyerID, privateKey);
            notaryClient.transferGood(goodID, buyerID, nonceBuyer, signatureBuyer);

            String sig = Crypto.getInstance().sign(privateKey, toSign);

            Response response = Response.ok().
                    header("Seller-Signature", sig).build();
            return Response.ok().build();

        } catch (Exception e) {
            throw e;
        }
    }
}
