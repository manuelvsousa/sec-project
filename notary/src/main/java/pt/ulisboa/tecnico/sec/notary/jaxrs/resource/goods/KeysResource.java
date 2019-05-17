package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.NotFoundExceptionResponse;
import pt.ulisboa.tecnico.sec.notary.model.exception.GoodNotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PublicKey;
import java.util.Base64;

@Path("/keys")
public class KeysResource {

    @GET
    @Path("/getPublicKey")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPublicKey() throws Exception {
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/keys/getPublicKey".getBytes());
        String nonceNotary = String.valueOf(System.currentTimeMillis());
        String publicKeySignature = Notary.getInstance().getPublicKeySignature();
        PublicKey publicKey = Notary.getInstance().getPublicKey();
        System.out.println(type + "||" + publicKeySignature + "||" + nonceNotary);
        byte[] toSignToSend = (type + "||" + publicKeySignature + "||" + Base64.getEncoder().encodeToString(publicKey.getEncoded()) + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignToSend, false);
        try {
            Response response = Response.status(200).
                    entity(Base64.getEncoder().encodeToString(publicKey.getEncoded())).
                    header("Notary-Signature", sigNotary).
                    header("PublicKey-Signature", publicKeySignature).
                    header("Notary-Nonce", nonceNotary).build();

            return response;
        } catch (GoodNotFoundException e) {
            throw new NotFoundExceptionResponse(e.getMessage(), sigNotary, nonceNotary, String.valueOf(System.getProperty("port")));
        } catch (Exception e) {
            throw e;
        }
    }
}
