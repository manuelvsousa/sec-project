import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;


public class EncryptPrivKey {

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        // check args
        if (args.length != 3) {
            System.err.println("Usage: EncryptPrivKey <key path> <key output path> <password>");
            return;
        }

        //path: users/user1.key
        String keyPath = args[0];
        String keyoutput = args[1];
        String password = args[2];


        Key privKey = read(keyPath);
        byte[] encodedprivKey = privKey.getEncoded();
        String PBEALG = "PBEWithSHA1AndDESede";


        int count = 20; //hash iteration count
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        //Create PBE parameter set
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance(PBEALG);
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        Cipher pbeCipher = Cipher.getInstance(PBEALG);

        //Initialize PBE cipher with key and parameters
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

        //Encrypt the encoded Private Key with the PBE key
        byte[] ciphertext = pbeCipher.doFinal(encodedprivKey);

        //Constructs PKCS#8 EncryptedPrivateKeyInfo object
        AlgorithmParameters algparms = AlgorithmParameters.getInstance(PBEALG);
        algparms.init(pbeParamSpec);
        EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);

        write(keyoutput, salt, encinfo);

    }

    public static Key read(String keyPath) throws GeneralSecurityException, IOException {
        System.out.println("Reading key from file " + keyPath + " ...");
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return new SecretKeySpec(encoded, "RSA");
    }

    public static void write(String keyPath, byte[] salt, EncryptedPrivateKeyInfo encinfo) throws IOException {
        byte[] encryptedPkcs8 = encinfo.getEncoded();
        System.out.println("Writing Private key to '" + keyPath + "' ...");
        FileOutputStream privFos = new FileOutputStream(keyPath);
        privFos.write(encryptedPkcs8);
        privFos.close();

        String arr[] = keyPath.split("\\.");
        String saltPath = arr[0] + "salt.txt";
        System.out.println("Writing salt to '" + saltPath + "' ...");
        FileOutputStream saltFos = new FileOutputStream(saltPath);
        saltFos.write(salt);
        saltFos.close();
    }
}