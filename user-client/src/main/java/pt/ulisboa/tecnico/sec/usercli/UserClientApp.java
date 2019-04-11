package pt.ulisboa.tecnico.sec.usercli;

import pt.ulisboa.tecnico.sec.util.KeyReader;
import pt.ulisboa.tecnico.sec.util.exception.PrivateKeyWrongPassword;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Scanner;

//mvn exec:java -Dexec.args="user1"

public class UserClientApp {

    public static void main(String[] args) throws GeneralSecurityException, IOException, Exception {

        /**TODO Pôr as mensagens recebidas mais bonitas e excepções???**/
        if (args.length != 1) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + UserClient.class.getName() + " userID");
            return;
        }
        String userID = args[0];
        Boolean flag = true;
        PrivateKey privKey = null;

        while (flag) {
            System.out.println("Insert password and press enter:");
            Scanner scanner = new Scanner(System.in);
            String password = scanner.nextLine();
            try {
                privKey = KeyReader.getInstance().readPrivateKey(userID, password);
                flag = false;
            } catch (PrivateKeyWrongPassword e) {
                System.out.println("Wrong password ");
            }
        }

        UserClient userClient = new UserClient(userID, privKey);
        userClient.addGood("good1");
        userClient.addGood("good2");
        userClient.addGood("good3");
        flag = true;
        Scanner scanner = new Scanner(System.in);

        while (flag) {
            System.out.println("Choose an operation (number + parameters): \n" +
                    "1 - intentionToSell: goodID\n" +
                    "2 - getStateOfGood: goodID\n" +
                    "3 - buyGood: goodID sellerID\n" +
                    "4 - listGoods\n" +
                    "Press Q to quit");

            String method = scanner.nextLine();
            String arr[] = method.split(" ");

            if (arr.length > 0) {
                switch (arr[0]) {
                    case "1":
                        if (arr.length == 2) {
                            System.out.println("intentionToSell: " + arr[1]);
                            System.out.println(userClient.intentionToSell(arr[1]));
                        } else {
                            System.out.println("Please insert the correct parameters");
                        }
                        break;
                    case "2":
                        if (arr.length == 2) {
                            System.out.println("getStateOfGood: " + arr[1]);
                            System.out.println(userClient.getStateOfgood(arr[1]));
                        } else {
                            System.out.println("Please insert the correct parameters");
                        }
                        break;
                    case "3":
                        if (arr.length == 3) {
                            System.out.println("buyGood: " + arr[1] + ", " + arr[2]);
                            System.out.println(userClient.buyGood(arr[1], userID, arr[2]));
                        } else {
                            System.out.println("Please insert the correct parameters");
                        }
                        break;
                    case "4":
                        userClient.printGoods();
                        break;
                    case "q":
                    case "Q":
                        flag = false;
                        break;
                    default:
                        System.out.println("Please insert a valid method");
                }
            } else {
                System.out.println("Wrong input: You didn't select any method");
            }
            System.out.println("\n");
        }
    }

    public static byte[] read(String path) throws GeneralSecurityException, IOException {
        System.out.println("Reading from file " + path + " ...");
        FileInputStream fis = new FileInputStream(path);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return encoded;
    }
}

