package pt.ulisboa.tecnico.sec.usermal;

import pt.ulisboa.tecnico.sec.util.Crypto;
import pt.ulisboa.tecnico.sec.util.KeyReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Scanner;

public class UserMalApp {
    private static final String REST_URI = "http://localhost:9090/notary/notary";
    private static Client clientMal = ClientBuilder.newClient();

    public static void main(String[] args) throws Exception {
        Boolean flag = true;
        Scanner scanner = new Scanner(System.in);

        while (flag) {
            System.out.println("Choose an attack: \n" +
                    "1 - Replay Attack\n" +
                    "2 - Authenticity Attack\n" +
                    "3 - Integrity Attack\n" +
                    "Press Q to quit");
            String attack = scanner.nextLine();

            switch (attack) {
                case "1":
                    PrivateKey privKey = KeyReader.getInstance().readPrivateKey("user1", "password1");
                    String type =
                            Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
                    String nonce = String.valueOf((System.currentTimeMillis()));
                    byte[] toSign = (type + "||good1||user1||" + nonce).getBytes();
                    String sig = Crypto.getInstance().sign(privKey, toSign);
                    System.out.println("First Request:" + type + "||good1||user1||" + nonce + "||" + sig);
                    Response r = clientMal.target(REST_URI + "/goods/getStatus").queryParam("id", "good1").queryParam("userID", "user1").queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
                    System.out.println("Request Done Succefully\n");
                    System.out.println("Second Request:" + type + "||good1||user1||" + nonce + "||" + sig);
                    try {
                        Response r1 = clientMal.target(REST_URI + "/goods/getStatus").queryParam("id", "good1").queryParam("userID", "user1").queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
                        System.out.println(r1.getStatus());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println("Atack, Muahahaha");
                    }
                    break;

                case "2":
                    privKey = KeyReader.getInstance().readPrivateKey("user2", "password2");
                    type =
                            Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
                    nonce = String.valueOf((System.currentTimeMillis()));
                    toSign = (type + "||good1||user1||" + nonce).getBytes();
                    sig = Crypto.getInstance().sign(privKey, toSign);
                    System.out.println("First Request:" + type + "||good1||user1||" + nonce + "||" + sig);
                    try {
                        r = clientMal.target(REST_URI + "/goods/getStatus").queryParam("id", "good1").queryParam("userID", "user1").queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
                        System.out.println(r.getStatus());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println("Atack, Muahahaha");
                    }
                    break;
                case "3":
                    privKey = KeyReader.getInstance().readPrivateKey("user1", "password1");
                    type =
                            Base64.getEncoder().withoutPadding().encodeToString("/goods/getStatus".getBytes());
                    nonce = String.valueOf((System.currentTimeMillis()));
                    toSign = (type + "||good1||user1||" + nonce).getBytes();
                    sig = Crypto.getInstance().sign(privKey, toSign);
                    System.out.println("First Request:" + type + "||good1||user1||" + nonce + "||" + sig);
                    r = clientMal.target(REST_URI + "/goods/getStatus").queryParam("id", "good1").queryParam("userID", "user2").queryParam("signature", sig).queryParam("nonce", nonce).request(MediaType.APPLICATION_JSON).get();
                    System.out.println(r.getStatus());
            }
        }
    }
}
