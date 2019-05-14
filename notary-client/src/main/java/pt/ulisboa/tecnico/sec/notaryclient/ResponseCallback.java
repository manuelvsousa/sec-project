package pt.ulisboa.tecnico.sec.notaryclient;

import pt.ulisboa.tecnico.sec.notary.model.State;
import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class ResponseCallback implements InvocationCallback<Response> {
    private final CountDownLatch latch;
    private boolean read;
    private String goodID;
    private HashMap<Integer, Response> responses = new HashMap<>();
    Throwable throwable;

    ResponseCallback(CountDownLatch latch, boolean read, String goodID) {
        this.latch = latch;
        this.read = read;
        this.goodID = goodID;
    }

    ResponseCallback(CountDownLatch latch) {
        this.latch = latch;
        this.read = false;
    }

    @Override
    public void completed(Response r) {
        this.responses.put(Integer.parseInt(r.getHeaderString("Notary-id")), r);
        if (read) {
            if(r.getStatus() == 200) {
                r.bufferEntity();
                State s = r.readEntity(State.class);
                String path = new File(System.getProperty("user.dir")).getParent();
                try {
                    PublicKey publicKey = KeyReader.getInstance().readPublicKey(s.getOwnerID(), path);
                    byte[] toSW = (this.goodID + " || " + s.getOnSale() + " || " +  s.getTimestamp() + " || " + s.getOwnerID()).getBytes();
                    System.out.println(this.goodID + " || " + s.getOnSale() + " || " +  s.getTimestamp() + " || " + s.getOwnerID());
                    System.out.println("Signature write" + s.getSignWrite());
                    if(Crypto.getInstance().checkSignature(publicKey, toSW, s.getSignWrite())) {
                        System.out.println(latch.getCount());
                        latch.countDown();
                    }
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println(latch.getCount());
                latch.countDown();
            }
        }
        else {
            System.out.println(latch.getCount());
            latch.countDown();
        }
    }

    @Override
    public void failed(Throwable t) {
        System.out.println("ola");
        //t.printStackTrace();
        throwable = t;
    }

    public HashMap<Integer, Response> getResponses() {
        return this.responses;
    }

}
