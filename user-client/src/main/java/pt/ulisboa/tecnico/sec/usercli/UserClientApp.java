package pt.ulisboa.tecnico.sec.usercli;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

//mvn exec:java -Dexec.args="user1"

public class UserClientApp {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        if (args.length != 1) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + UserClient.class.getName() + " userID");
            return;
        }
        String userID = args[0];
        Boolean flag = true;

        String keyPath = "keys/" + userID + "enc.key";
        byte[] encoded = read(keyPath);
        String saltPath = "keys/" + userID + "encsalt.txt";
        byte[] salt = read(saltPath);

        while (flag) {
            System.out.println("Insert password and press enter:");
            Scanner scanner = new Scanner(System.in);
            String password = scanner.nextLine();
            try {
                verifyID(userID, password, encoded, salt);
                flag = false;
            } catch (BadPaddingException e) {
                System.out.println("Wrong password ");
            }
        }

        UserClient userClient = new UserClient(userID);
        flag = true;
        Scanner scanner = new Scanner(System.in);

        while (flag) {
            System.out.println("Choose an operation (number + parameters): \n" +
                    "1 - intentionToSell: goodID\n" +
                    "2 - getStateOfGood: goodID\n" +
                    "3 - transferGood: goodID buyerID\n" +
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
                            System.out.println(userClient.intentionToSell(arr[1]));
                        } else {
                            System.out.println("Please insert the correct parameters");
                        }
                        break;
                    case "3":
                        if (arr.length == 3) {
                            System.out.println("transferGood: " + arr[1] + ", " + arr[2]);
                            System.out.println(userClient.transferGood(arr[1], arr[2]));
                        } else {
                            System.out.println("Please insert the correct parameters");
                        }
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

        }
        //System.out.println(userClient.getStateOfgood("good1"));
    }

    public static Boolean verifyID(String userID, String password, byte[] encoded, byte[] salt) throws GeneralSecurityException, IOException, BadPaddingException {

        EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(encoded);
        byte[] encrypPrivKey = encinfo.getEncryptedData();

        String PBEALG = "PBEWithSHA1AndDESede";
        int count = 20; //hash iteration count

        //Create PBE parameter set
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBEALG);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(PBEALG);

        //Initialize PBE cipher with key and parameters
        pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

        byte[] encodedPrivKey = pbeCipher.doFinal(encrypPrivKey);

        //Key privKey = new SecretKeySpec(encodedPrivKey, "RSA");

        return true;
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

