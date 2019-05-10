package pt.ulisboa.tecnico.sec.notaryclient;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;

public class ResponseCallback implements InvocationCallback<Response> {
    private final CountDownLatch latch;
    Throwable throwable;

    ResponseCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void completed(Response r) {
        System.out.println(latch.getCount());
        latch.countDown();
    }

    @Override
    public void failed(Throwable t) {
        t.printStackTrace();
        throwable = t;
        latch.countDown();
    }
}
