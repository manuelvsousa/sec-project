package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.notaryclient.exception.GoodNotFoundException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.InvalidSignature;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserDoesNotOwnGoodException;
import pt.ulisboa.tecnico.sec.notaryclient.exception.UserNotFoundException;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;


class NotaryAbstract {

    private static final String REST_URI = "http://localhost:9191/notary/notary";
    private static  final int F = 1;
    private static  final int N = 4 * F;
    private Client client = ClientBuilder.newClient();
    private PrivateKey privateKey;
    private long lastNotaryNonce;
    private ArrayList<PublicKey> notarySignedPublicKey = new ArrayList<PublicKey>();
    private PublicKey notaryCCPublicKey;
    private boolean withCC;

    public NotaryAbstract(PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.lastNotaryNonce = System.currentTimeMillis();
        this.withCC = false;

        try {
            String path = new File(System.getProperty("user.dir")).getParent();
            if (this.withCC) {
                this.notaryCCPublicKey = KeyReader.getInstance().readPublicKey("notaryCC", path);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not Load Notary CC public key");
        }

        try {
            retrievePublicKey();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not Load Notary generated public key");
        }
    }

    public State getStateOfGood(String id, String userID) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (type + "||" + id + "||" + userID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            String REST_URI_C;
            int n = (int) Math.ceil((N+F)/2.0);
            final CountDownLatch latch = new CountDownLatch(n);
            ResponseCallback responseCallback = new ResponseCallback(latch);
            HashMap<Integer, Response> r = new HashMap<>();
            for(int i = 1; i <= N; i++) {
                REST_URI_C = "http://localhost:919" + i + "/notary/notary";
                Future<Response> f = client.target(REST_URI_C + "/goods/getStatus").queryParam("id", id).queryParam("userID", userID).queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).async().get(responseCallback);
                r.put(i, (Response) f.get());
            }

            latch.await();

            long maxTimestamp = 0;
            int faults = 0;
            State s = null;
            for(Integer i : r.keySet()) {
                if(r.get(i).getStatus() == 200) {
                    s = r.get(i).readEntity(State.class);
                    toSign = (type + "||" + id + "||" + userID + "||" + nonce + "||" + s.getOnSale() + "||" + s.getOwnerID()).getBytes();
                }
                String code = codeResponse(r.get(i), toSign, withCC, i);
                if(code.equals("200")) {
                    //Long.valueOf(nonceS).longValue();
                }
                else {
                    faults++; /**TODO Improve**/
                }
            }

            return s;
        } catch (NotFoundException e) {
            String cause = e.getResponse().readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error 404 is null");
            }
            if (cause.toLowerCase().contains("good".toLowerCase())) {
                throw new GoodNotFoundException(cause);
            }
        }
        throw new RuntimeException("Unknown Error");
    }

    private void retrievePublicKey() {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/keys/getPublicKey".getBytes());
            String REST_URI_C;
            for(int i = 1; i <= N; i++) {
                REST_URI_C = "http://localhost:919" + i + "/notary/notary";
                Response r = client.target(REST_URI_C + "/keys/getPublicKey").request(MediaType.APPLICATION_JSON).get();
                String publicKeySignature = r.getHeaderString("PublicKey-Signature");
                String publicKeybase64 = r.readEntity(String.class);
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeybase64.getBytes()));
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey publicKey = kf.generatePublic(publicKeySpec);
                this.notarySignedPublicKey.add(publicKey);
                byte[] toSign = (type + "||" + publicKeySignature + "||" + Base64.getEncoder().encodeToString(publicKey.getEncoded())).getBytes();
                this.verifyResponse(r, toSign, false, i-1); // General request verification. Check integrity hole message
            /*  The check bellow is really important
                Check if signature from CC actually matches the sent public key
                An attacker may encript a simillar message with a equally generated key.
                This verification is going ensure that the public key actually came from notary and not from another place
                This is done this away to avoid unnecessary CC signatures everytime someone asks for the public key.
             */

            /**TODO fix this**/
                if (this.withCC && !Crypto.getInstance().checkSignature(this.notaryCCPublicKey, publicKey.getEncoded(), publicKeySignature)) {
                    throw new InvalidSignature("Public Key sent from notary was forged. Signature made with CC is wrong");
                }
            }
            return;
        } catch (NotFoundException e) {
            String cause = e.getResponse().readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error 404 is null");
            }
            if (cause.toLowerCase().contains("good".toLowerCase())) {
                throw new GoodNotFoundException(cause);
            }
        } catch (InvalidKeySpecException asd) {
            // TODO
        } catch (NoSuchAlgorithmException asd) {
            // TODO
        }
        throw new RuntimeException("Unknown Error");
    }


    public Map<String, String> transferGood(String goodID, String buyerID, String sellerID, String nonceBuyer, String sigBuyer) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/transfer".getBytes());
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (type + "||" + goodID + "||" + buyerID + "||" + sellerID + "||" + nonce + "||" + nonceBuyer + "||" + sigBuyer).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            byte[] toSW = (goodID + " || false || " +  nonce + " || " + sellerID).getBytes();
            String sigWrite = Crypto.getInstance().sign(privateKey, toSW);
            HashMap<Integer, Response> r = new HashMap<>();
            String REST_URI_C;
            int n = (int) Math.ceil((N+F)/2.0);
            final CountDownLatch latch = new CountDownLatch(n);
            ResponseCallback responseCallback = new ResponseCallback(latch);
            for(int i = 1; i <= N; i++) {
                REST_URI_C = "http://localhost:919" + i + "/notary/notary";
                Future<Response> f = client.target(REST_URI_C + "/goods/transfer").queryParam("goodID", goodID).queryParam("buyerID", buyerID).queryParam("sellerID", sellerID).queryParam("signature", sig).queryParam("nonce", nonce).queryParam("nonceBuyer", nonceBuyer).queryParam("sigBuyer", sigBuyer).queryParam("sigWrite", sigWrite).request(MediaType.APPLICATION_JSON).async().get(responseCallback);
                r.put(i, (Response) f.get());
            }

            latch.await();
            HashMap<String, Integer> codes = this.processResponses(r, toSign);

            for(String code_aux : codes.keySet()) {
                if(codes.get(code_aux) > (N + F)/2) {
                    System.out.println(code_aux);
                    //it only returns if everything went well
                    checkCode(code_aux);
                    String notarySig = "";
                    String nonceS = "";
                    for(Integer i : r.keySet()) {
                        if(r.get(i).getStatus() == 200) {
                            /**TODO Alterar isto **/
                            notarySig = r.get(i).getHeaderString("Notary-Signature");
                            nonceS = r.get(i).getHeaderString("Notary-Nonce");
                            break;
                        }
                    }
                    //this.verifyResponse(r, toSign, true);
                    Map<String, String> map = new HashMap<>();
                    map.put("Notary-Signature", notarySig);
                    map.put("Original-Message", new String(toSign) + "||" + nonceS);
                    map.put("Good", goodID);
                    map.put("Buyer", buyerID);
                    map.put("Seller", sellerID);
                    map.put("Notary-Time", nonceS);
                    return map;
                }
            }
            throw new RuntimeException("Unknown Error");
        } catch (Exception e) {
            throw e;
        }
    }

    private void verifyResponse(Response r, byte[] toSign, boolean withCC, int index) {
        withCC = withCC && this.withCC;
        String sig = r.getHeaderString("Notary-Signature");
        String nonceS = r.getHeaderString("Notary-Nonce");
        long nonce = Long.valueOf(nonceS).longValue();
        if (sig == null) {
            throw new InvalidSignature("Signature from notary was null");
        } else {
            toSign = (new String(toSign) + "||" + nonceS).getBytes();
            if (!Crypto.getInstance().checkSignature(withCC ? this.notaryCCPublicKey : this.notarySignedPublicKey.get(index), toSign, sig)) {
                retrievePublicKey();
                if (!Crypto.getInstance().checkSignature(withCC ? this.notaryCCPublicKey : this.notarySignedPublicKey.get(index), toSign, sig)) {
                    throw new InvalidSignature("Signature from notary was forged");
                } else {
                    throw new InvalidSignature("Notary has new Public Key. Please Redo Request"); //can be optimized, but might open a security hole
                }
            }
        }
        if (nonce > this.lastNotaryNonce) {
            this.lastNotaryNonce = nonce;
        } else {
            throw new InvalidSignature("Nonce from notary is invalid");
        }

        if (r.getStatus() == 200) {
            return;
        } else {
            String cause = r.readEntity(String.class);
            if (cause == null) {
                throw new RuntimeException("Cause of error " + r.getStatus() + " is null");
            }
            if (r.getStatus() == 404) {
                if (cause.toLowerCase().contains("good".toLowerCase())) {
                    throw new GoodNotFoundException(cause);
                } else if (cause.toLowerCase().contains("user".toLowerCase())) {
                    throw new UserNotFoundException(cause);
                }
            } else if (r.getStatus() == 417) {
                throw new UserDoesNotOwnGoodException(cause);
            } else if (r.getStatus() == 409) { //conflict
                throw new UserDoesNotOwnGoodException(cause);
            } else if (r.getStatus() == 406) { //not acceptable
                throw new UserDoesNotOwnGoodException(cause);
            } else {
                throw new RuntimeException("Unable to process request, ERROR " + r.getStatus() + " Received!");
            }
        }
    }

    public void intentionToSell(String goodID, String sellerID) throws Exception {
        try {
            String type =
                    Base64.getEncoder().withoutPadding().encodeToString("/goods/intention".getBytes());
            String nonce = String.valueOf((System.currentTimeMillis()));
            byte[] toSign = (type + "||" + goodID + "||" + sellerID + "||" + nonce).getBytes();
            String sig = Crypto.getInstance().sign(privateKey, toSign);
            byte[] toSW = (goodID + " || false || " +  nonce + " || " + sellerID).getBytes();
            String sigWrite = Crypto.getInstance().sign(privateKey, toSW);
            HashMap<Integer, Response> r = new HashMap<>();
            String REST_URI_C;
            int n = (int) Math.ceil((N+F)/2.0);
            final CountDownLatch latch = new CountDownLatch(n);
            ResponseCallback responseCallback = new ResponseCallback(latch);
            for(int i = 1; i <= N; i++) {
                REST_URI_C = "http://localhost:919" + i + "/notary/notary";
                Future<Response> f = client.target(REST_URI_C + "/goods/intention").queryParam("goodID", goodID).queryParam("sellerID", sellerID).queryParam("signature", sig).queryParam("nonce", nonce).queryParam("sigWrite", sigWrite).request(MediaType.APPLICATION_JSON).async().get(responseCallback);
                r.put(i, (Response) f.get());
                //this.verifyResponse(r, toSign, false);
            }

            latch.await();
            HashMap<String, Integer> codes = this.processResponses(r, toSign);

            for(String code_aux : codes.keySet()) {
                if(codes.get(code_aux) > (N + F)/2) {
                    System.out.println(code_aux);
                    checkCode(code_aux);
                    return;
                }
            }

            throw new RuntimeException("Unknown Error");
        } catch (Exception e) {
            throw e;
        }
    }


    private String codeResponse(Response r, byte[] toSign, boolean withCC, int index) {
        withCC = withCC && this.withCC;
        String sig = r.getHeaderString("Notary-Signature");
        String nonceS = r.getHeaderString("Notary-Nonce");
        long nonce = Long.valueOf(nonceS).longValue();
        if (sig == null) {
            return "Signull";
        } else {
            toSign = (new String(toSign) + "||" + nonceS).getBytes();
            if (!Crypto.getInstance().checkSignature(withCC ? this.notaryCCPublicKey : this.notarySignedPublicKey.get(index), toSign, sig)) {
                retrievePublicKey();
                if (!Crypto.getInstance().checkSignature(withCC ? this.notaryCCPublicKey : this.notarySignedPublicKey.get(index), toSign, sig)) {
                    return "Sigforged";
                } else {
                    return "KeySync"; //can be optimized, but might open a security hole
                }
            }
        }
        if (nonce > this.lastNotaryNonce) {
            this.lastNotaryNonce = nonce;
        } else {
            return "Siginval";
        }

        if (r.getStatus() == 200) {
            return "200";
        } else {
            String cause = r.readEntity(String.class);
            if (cause == null) {
                return "null ||" + r.getStatus();
            }
            if (r.getStatus() == 404) {
                if (cause.toLowerCase().contains("good".toLowerCase())) {
                    return "goodNotFound ||" + cause;
                } else if (cause.toLowerCase().contains("user".toLowerCase())) {
                    return "userNotFound";
                }
            } else if (r.getStatus() == 417) {
                return "417 ||" + cause;
            } else if (r.getStatus() == 409) { //conflict
                return "409 ||" + cause;
            } else if (r.getStatus() == 406) { //not acceptable
                return "406 ||" + cause;
            } else {
                return "error ||" + r.getStatus();
            }
        }
        return "error";
    }

    private void checkCode(String code) {
        if(code.equals("Signull")) {
            throw new InvalidSignature("Signature from notary was null");
        }
        else if(code.equals("Sigforged")) {
            throw new InvalidSignature("Signature from notary was forged");
        }
        else if(code.equals("Siginval")){
            throw new InvalidSignature("Nonce from notary is invalid");
        }
        else if(code.equals("200")) {
            return;
        }
        else if(code.startsWith("null")) {
            String[] cause = code.split("||");
            throw new RuntimeException("Cause of error " +  cause[1] + " is null");
        }
        else if(code.startsWith("goodNotFound")) {
            String[] cause = code.split("||");
            throw new GoodNotFoundException(cause[1]);
        }
        else if(code.startsWith("userNotFound")) {
            String[] cause = code.split("||");
            throw new UserNotFoundException(cause[1]);
        }
        else if(code.startsWith("417")) {
            String[] cause = code.split("||");
            throw new UserDoesNotOwnGoodException(cause[1]);
        }
        else if(code.startsWith("409")) {
            String[] cause = code.split("||");
            throw new UserDoesNotOwnGoodException(cause[1]);
        }
        else if(code.startsWith("406")) {
            String[] cause = code.split("||");
            throw new UserDoesNotOwnGoodException(cause[1]);
        }
        else if(code.startsWith("error")) {
            String[] cause = code.split("||");
            throw new RuntimeException("Unable to process request, ERROR " + cause + " Received!");
        }
    }

    private HashMap<String, Integer> processResponses(HashMap<Integer, Response> r, byte[] toSign) {
        HashMap<String, Integer> codes = new HashMap<String, Integer>();
        String code;
        int value;
        for(Integer i : r.keySet()) {
            code = this.codeResponse(r.get(i), toSign, false, i-1);
            if(codes.containsKey(code)) {
                value = codes.get(code) + 1;
                codes.replace(code, value);
            }
            else {
                codes.put(code, 1);
            }
        }
        return  codes;
    }


}