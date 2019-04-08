package pt.ulisboa.tecnico.sec.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class Crypto {
    private static Crypto uniqueInstance;


    private Crypto() {
    }

    public static Crypto getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Crypto();
        }
        return uniqueInstance;
    }

    private static byte[] hexToByte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String sign(PrivateKey privK, byte[] data) throws Exception {
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(privK);
        sig.update(data);
        return byteToHex(sig.sign());
    }

    public boolean checkSignature(PublicKey publicKey, byte[] signedContent, String sig) {
        boolean result = false;
        try {
            Signature signature1 = Signature.getInstance("SHA1withRSA");
            signature1.initVerify(publicKey);
            signature1.update(signedContent);
            result = signature1.verify(hexToByte(sig));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result) {
            return true;
        } else {
            return false;
        }
    }

    public String byteToHex(byte[] sig) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[sig.length * 2];
        for (int j = 0; j < sig.length; j++) {
            int v = sig[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}