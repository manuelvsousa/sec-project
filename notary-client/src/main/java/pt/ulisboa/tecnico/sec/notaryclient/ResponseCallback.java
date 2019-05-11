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
import java.util.concurrent.CountDownLatch;


public class ResponseCallback implements InvocationCallback<Response> {
    private final CountDownLatch latch;
    private boolean read;
    private String goodID;
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
        if (read) {
            if(r.getStatus() == 200) {
                State s = r.readEntity(State.class);
                String path = new File(System.getProperty("user.dir")).getParent();
                try {
                    PublicKey publicKey = KeyReader.getInstance().readPublicKey(s.getOwnerID(), path);
                    byte[] toSW = (this.goodID + " || " + s.getOnSale() + " || " +  s.getTimestamp() + " || " + s.getOwnerID()).getBytes();
                    System.out.println(this.goodID + " || " + s.getOnSale() + " || " +  s.getTimestamp() + " || " + s.getOwnerID());
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
        t.printStackTrace();
        throwable = t;
        latch.countDown();
    }
}
