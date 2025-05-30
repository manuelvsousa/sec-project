package pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods;

import pt.ulisboa.tecnico.sec.notary.jaxrs.application.Notary;
import pt.ulisboa.tecnico.sec.notary.jaxrs.resource.goods.exception.*;
import pt.ulisboa.tecnico.sec.notary.model.Message;
import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notary.model.exception.*;
import pt.ulisboa.tecnico.sec.notary.util.Checker;
import pt.ulisboa.tecnico.sec.util.HashCash;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/goods")
public class GoodsResource {
    private static ExecutorService executor = Executors.newFixedThreadPool(10); //max 10 threads

    @GET
    @Path("/getStatus")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getStateOfGood(@QueryParam("id") String id, @QueryParam("userID") String userID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce, @QueryParam("pow") String pow, @Suspended AsyncResponse ar) throws Exception {
        System.out.println("\n\nReceived Parameters:\n");
        System.out.println("goodID: " + id + "\nuserID: " + userID + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce+ "\npow -> " + pow);
        if (id == null || userID == null || sig == null || nonce == null || pow == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("id and/or userID and/or sig and/or nonce and/or pow are null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
        byte[] toSign = (type + "||" + id + "||" + userID + "||" + nonce).getBytes();

        if(!Notary.getInstance().verifyPOW(pow,userID,toSign)){
            throw new InvalidProofOfWork();
        }

        String nonceNotary = String.valueOf(System.currentTimeMillis());
        byte[] toSignToSend = (type + "||" + id + "||" + userID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignToSend, false);
        String notaryId = System.getProperty("port");
        Response response = Response.status(200).
                header("Notary-Signature", sigNotary).
                header("Notary-Nonce", nonceNotary).
                header("Notary-id", notaryId). build();
        try {
            executor.execute( () -> {
            State s = Notary.getInstance().getStateOfGood(id);

            Checker.getInstance().checkResponse(toSign, userID, sig, nonce, nonceNotary, sigNotary);

               byte[] toSignToSend1 = (type + "||" + id + "||" + userID + "||" + nonce + "||" + s.getOnSale() + "||" + s.getOwnerID() + "||" + nonceNotary).getBytes();

                try {
                    String sigNotary1 = Notary.getInstance().sign(toSignToSend1, false);
                    System.out.println("\n\n\nAbout to Send:\n");
                    System.out.println("Notary-Signature: " + sigNotary1 + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignToSend1));
                    Response response1 = Response.status(200).
                            entity(s).
                            header("Notary-Signature", sigNotary1).
                            header("Notary-Nonce", nonceNotary).
                            header("Notary-id", notaryId). build();
                    ar.resume(response1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return response;
        } catch (GoodNotFoundException e) {
            throw new NotFoundExceptionResponse(e.getMessage(), sigNotary, nonceNotary, notaryId);
        } catch (Exception e) {
            throw e;
        }
    }


    @GET
    @Path("/transfer")
    public Response transferGood(@QueryParam("goodID") String goodID, @QueryParam("buyerID") String buyerID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce, @QueryParam("nonceBuyer") String nonceBuyer, @QueryParam("sigBuyer") String sigBuyer, @QueryParam("sigWrite") String sigWrite, @QueryParam("pow") String pow ,@Suspended AsyncResponse ar) throws Exception {
        System.out.println("\n\nReceived Parameters:\n");
        System.out.println("goodID: " + goodID + "\nbuyerID: " + buyerID + "\nsellerID: " + sellerID + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce + "\nnonce (from buyer): " + nonceBuyer + "\nsignature (from buyer): " + sigBuyer+ "\npow -> " + pow);
        if (goodID == null || goodID == null || sellerID == null || sig == null || nonce == null || nonceBuyer == null || sigBuyer == null || pow == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or goodID and/or sellerID and/or signature and/or nonce and/or nonceBuyer and/or sigBuyer and/or pow null").build());
        }
        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
        byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceBuyer + "||" + sigBuyer).getBytes();

        if(!Notary.getInstance().verifyPOW(pow,sellerID,toSign)){
            throw new InvalidProofOfWork();
        }

        String nonceNotary = String.valueOf((System.currentTimeMillis()));
        byte[] toSignResponse = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceBuyer + "||" + sigBuyer + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse, true);
        String notaryId = System.getProperty("port");

        Notary.getInstance().doIntegrityCheck(goodID, buyerID, sellerID); //check if all users exist, if goods exist, and if users are telling the truth i.e if they own the goods they claim to own

        Checker.getInstance().checkResponse(toSign, sellerID, sig, nonce, nonceNotary, sigNotary); // Check integrity of message and nonce validity

        byte[] toSign2 = (goodID + "||" + buyerID + "||" + sellerID + "||" + nonceBuyer).getBytes();

        Checker.getInstance().checkResponse(toSign2, buyerID, sigBuyer, nonceBuyer, nonceNotary, sigNotary); // Check integrity of message send by the buyer to the seller

        Response response1 = Response.ok().
                header("Notary-Signature", sigNotary).
                header("Notary-Nonce", nonceNotary).
                header("Notary-id", notaryId).build();
        executor.execute( () -> {
            /* Doing this might invalidate the transfer in case the buyer makes a request that arrives first then this one.
             * But this verification wont allow a malicious seller to reuse a previously buyer transfer request in another
             * future transfer for the same good (in case a certain seller sells the good, then gets it back, and tries to preform a transfer request again without the buyer knowing)
             * */
            Message m;
            try {
                m = Notary.getInstance().validateWrite("transferGood", goodID, buyerID, sellerID, sigWrite, nonceBuyer, false);
            } catch (Exception e) {
                throw new InvalidTransactionExceptionResponse(e.getMessage(), sigNotary, nonceNotary);
            }
            if(m!=null) {
                try {
                    Notary.getInstance().addTransaction(goodID, buyerID, sellerID, nonceNotary, sigWrite, nonceBuyer);

                    System.out.println("\n\n\nAbout to Send:\n");
                    System.out.println("Notary-Signature: " + sigNotary + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignResponse));
                    Response response = Response.ok().
                            header("Notary-Signature", sigNotary).
                            header("Notary-Nonce", nonceNotary).
                            header("Notary-id", notaryId).build();
                    ar.resume(response);
                } catch (GoodNotFoundException e1) {
                    Response response = Response.status(Response.Status.NOT_FOUND).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).header("Notary-id", notaryId).entity(e1.getMessage()).build();
                    ar.resume(response);
                } catch (UserNotFoundException e2) {
                    Response response = Response.status(Response.Status.NOT_FOUND).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).header("Notary-id", notaryId).entity(e2.getMessage()).build();
                    ar.resume(response);
                } catch (UserDoesNotOwnGood e3) {
                    Response response = Response.status(Response.Status.EXPECTATION_FAILED).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).entity(e3.getMessage()).build();
                    ar.resume(response);
                } catch (GoodNotOnSale gg) {
                    Response response = Response.status(Response.Status.NOT_ACCEPTABLE).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).entity(gg.getMessage()).build();
                    ar.resume(response);
                } catch (TransactionAlreadyExistsException e4) {
                    throw new InvalidTransactionExceptionResponse(e4.getMessage(), sigNotary, nonceNotary);
                } catch (InvalidTransactionException e5) {
                    throw new InvalidTransactionExceptionResponse(e5.getMessage(), sigNotary, nonceNotary);
                } catch (Exception e) {
                    throw e;
                }
            }
        });

            return response1;
    }

    @GET
    @Path("/intention")
    public Response intentionToSell(@QueryParam("goodID") String goodID, @QueryParam("sellerID") String sellerID, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce, @QueryParam("sigWrite") String sigWrite,@QueryParam("pow") String pow, @Suspended AsyncResponse ar) throws Exception {
        System.out.println("\n\nReceived Parameters:\n");
        System.out.println("goodID: " + goodID + "\nsellerID: " + sellerID + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce + "\npow -> " + pow);
        if (goodID == null || sellerID == null || sig == null || nonce == null || pow == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("goodID and/or sellerID and/or signature and/or nonce and/or pow  are null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/intention".getBytes());
        byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + nonce).getBytes();

        if(!Notary.getInstance().verifyPOW(pow,sellerID,toSign)){
            throw new InvalidProofOfWork();
        }

        String nonceNotary = String.valueOf((System.currentTimeMillis()));
        byte[] toSignResponse = (type + "||" + goodID + "||" + sellerID + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse, false);
        String notaryId = System.getProperty("port");

        Checker.getInstance().checkResponse(toSign, sellerID, sig, nonce, nonceNotary, sigNotary); // Check integrity of message and nonce validaty

        Response response1 = Response.ok().header("Notary-Signature", sigNotary).
                header("Notary-Nonce", nonceNotary).build();
        executor.execute(() -> {
            Message m = null;
            try {
                m = Notary.getInstance().validateWrite("intentionToSell", goodID, sellerID, "", sigWrite, nonce, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (m != null) {
                try {
                    Notary.getInstance().setIntentionToSell(goodID, sellerID, nonce, sigWrite);
                    System.out.println("\n\n\nAbout to Send:\n");
                    System.out.println("Notary-Signature: " + sigNotary + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignResponse));
                    Response response = Response.ok().
                            header("Notary-Signature", sigNotary).
                            header("Notary-Nonce", nonceNotary).
                            header("Notary-id", notaryId).build();
                    ar.resume(response);
                } catch (GoodNotFoundException e1) {
                    Response response = Response.status(Response.Status.NOT_FOUND).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).header("Notary-id", notaryId).entity(e1.getMessage()).build();
                    ar.resume(response);
                } catch (UserNotFoundException e2) {
                    Response response = Response.status(Response.Status.NOT_FOUND).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).header("Notary-id", notaryId).entity(e2.getMessage()).build();
                    ar.resume(response);
                } catch (UserDoesNotOwnGood e3) {
                    Response response = Response.status(Response.Status.NOT_FOUND).
                            header("Notary-Signature", sig).
                            header("Notary-Nonce", nonce).header("Notary-id", notaryId).entity(e3.getMessage()).build();
                    ar.resume(response);
                } catch (Exception e) {
                    Response response = Response.serverError().header("Error", new RuntimeException(e.getMessage())).build();
                    ar.resume(response);
                }
            }
        });
        return response1;
    }




    @GET
    @Path("/update")
    public Response updateReplicas(@QueryParam("userID") String userID, @QueryParam("goodID") String goodID, @QueryParam("sellerID") String sellerID, @QueryParam("onSale") String onSale,@QueryParam("goodNonce") String goodNonce, @QueryParam("signature") String sig, @QueryParam("nonce") String nonce,@QueryParam("sigWrite") String sigWrite, @Suspended AsyncResponse ar) throws Exception{
        System.out.println("\n\nReceived Parameters:\n");
        System.out.println("userID: " + userID + "\ngoodID: " + goodID + "\nsellerID: " + sellerID + "\nonSale:" + onSale + "\ngoodNonce:" + goodNonce + "\nsignature: " + sig + "\nnonce (from notary-client): " + nonce);
        if (userID == null || goodID == null || sellerID == null || onSale == null || goodNonce ==null || sig == null || nonce == null) {
            throw new WebApplicationException(Response.status(400) // 400 Bad Request
                    .entity("userID and/or goodID and/or sellerID and/or onSale and/or goodNonce and/or sig and/or nonce are null").build());
        }

        String type =
                Base64.getEncoder().withoutPadding().encodeToString("/goods/update".getBytes());
        byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + onSale + "||" + goodNonce).getBytes();
        String nonceNotary = String.valueOf((System.currentTimeMillis()));
        byte[] toSignResponse = (type + "||" + goodID + "||" + sellerID + "||" + onSale + "||" + goodNonce + "||" + nonce + "||" + nonceNotary).getBytes();
        String sigNotary = Notary.getInstance().sign(toSignResponse, false);
        String notaryId = System.getProperty("port");

        Checker.getInstance().checkResponse(toSign, userID, sig, nonce, nonceNotary, sigNotary);
        Response response1 = Response.ok().
                header("Notary-Signature", sigNotary).
                header("Notary-Nonce", nonceNotary).build();


        executor.execute(() -> {
            Message m;
            try {
                m = Notary.getInstance().validateWrite("transferGood", goodID, sellerID, "", sigWrite, goodNonce, Boolean.valueOf(onSale));
            } catch (Exception e) {
                throw new InvalidTransactionExceptionResponse(e.getMessage(), sigNotary, nonceNotary);
            }

            try {
                Notary.getInstance().setStateOfGood(goodID,sellerID,Boolean.valueOf(onSale),goodNonce,sigWrite);

                System.out.println("\n\n\nAbout to Send (updateReplicas):\n");
                System.out.println("Notary-Signature: " + sigNotary + "\nNotary-Nonce: " + nonceNotary + "\ncontent: " + new String(toSignResponse));
                Response response = Response.ok().
                        header("Notary-Signature", sigNotary).
                        header("Notary-Nonce", nonceNotary).
                        header("Notary-id", notaryId).build();
                ar.resume(response);
            } catch (GoodNotFoundException e1) {
                Response.status(Response.Status.NOT_FOUND).
                        header("Notary-Signature", sig).
                        header("Notary-Nonce", nonce).header("Notary-id", notaryId).entity(e1.getMessage()).build();
            }

        });

            return response1;

    }
}
