package pt.ulisboa.tecnico.sec.citizencard;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static javax.xml.bind.DatatypeConverter.printHexBinary;


public class CitizenCard {
    private static CitizenCard uniqueInstance;

    private CitizenCard() {
    }

    public static CitizenCard getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new CitizenCard();
        }
        return uniqueInstance;
    }

    public void sign(){
        try {

            System.out.println("            //Load the PTEidlibj");

            System.loadLibrary("pteidlibj");
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)


            PKCS11 pkcs11;
            String osName = System.getProperty("os.name");
            String javaVersion = System.getProperty("java.version");
            System.out.println("Java version: " + javaVersion);

            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

            String libName = "libpteidpkcs11.so";

            // access the ID and Address data via the pteidlib
            System.out.println("            -- accessing the ID  data via the pteidlib interface");

            // NOTHERE showInfo();

            X509Certificate cert = getCertFromByteArray(getCertificateInBytes(0));
            System.out.println("Citized Authentication Certificate " + cert);

            // access the ID and Address data via the pteidlib
            System.out.println("            -- generating signature via the PKCS11 interface");


            if (-1 != osName.indexOf("Windows"))
                libName = "pteidpkcs11.dll";
            else if (-1 != osName.indexOf("Mac"))
                libName = "libpteidpkcs11.dylib";
            Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
            if (javaVersion.startsWith("1.5.")) {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, null, false});
            } else {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, "C_GetFunctionList", null, false});
            }

            //Open the PKCS11 session
            System.out.println("            //Open the PKCS11 session");
            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            // Token login
            System.out.println("            //Token login");
            pkcs11.C_Login(p11_session, 1, null);
            CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);

            // sign my string
            System.out.println("            //sign my stuff");
            byte[] contentToSign = "THE_CONTENT_THAT_IS_BEING_SIGNED".getBytes();


            // Get available keys
            System.out.println("            //Get available keys");
            CK_ATTRIBUTE[] attributes_lab = new CK_ATTRIBUTE[1];
            attributes_lab[0] = new CK_ATTRIBUTE();
            attributes_lab[0].type = PKCS11Constants.CKA_CLASS;
            attributes_lab[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

            pkcs11.C_FindObjectsInit(p11_session, attributes_lab);
            long[] keyHandles_lab = pkcs11.C_FindObjects(p11_session, 5);

            // points to auth_key
            System.out.println("            //points to auth_key. No. of keys:" + keyHandles_lab.length);

            long signatureKey_lab = keyHandles_lab[0];        //test with other keys to see what you get
            pkcs11.C_FindObjectsFinal(p11_session);

            // initialize the signature method
            System.out.println("            //initialize the signature method");
            CK_MECHANISM mechanism_lab = new CK_MECHANISM();
            mechanism_lab.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
            mechanism_lab.pParameter = null;
            pkcs11.C_SignInit(p11_session, mechanism_lab, signatureKey_lab);

            byte[] signature = pkcs11.C_Sign(p11_session, contentToSign);
            System.out.println("            //signature my public key:" + encoder.encode(signature));

            Signature signature1 = Signature.getInstance("SHA1withRSA");

            X509Certificate cert2 = getCertFromByteArray(getCertificateInBytes(0));

            signature1.initVerify(cert2);
            signature1.update(contentToSign); // what i signed
            boolean result = signature1.verify(signature);

            System.out.println("           Correcly signed ? " + result);

            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib


        } catch (Exception e) {


        }




    }


    private void lixo() {

//        try {
//
//            System.out.println("            //Load the PTEidlibj");
//
//            System.loadLibrary("pteidlibj");
//            pteid.Init(""); // Initializes the eID Lib
//            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
//
//
//            PKCS11 pkcs11;
//            String osName = System.getProperty("os.name");
//            String javaVersion = System.getProperty("java.version");
//            System.out.println("Java version: " + javaVersion);
//
//            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
//
//            String libName = "libpteidpkcs11.so";
//
//            // access the ID and Address data via the pteidlib
//            System.out.println("            -- accessing the ID  data via the pteidlib interface");
//
//            // NOTHERE showInfo();
//
//            X509Certificate cert = getCertFromByteArray(getCertificateInBytes(0));
//            System.out.println("Citized Authentication Certificate " + cert);
//
//            // access the ID and Address data via the pteidlib
//            System.out.println("            -- generating signature via the PKCS11 interface");
//
//
//            if (-1 != osName.indexOf("Windows"))
//                libName = "pteidpkcs11.dll";
//            else if (-1 != osName.indexOf("Mac"))
//                libName = "libpteidpkcs11.dylib";
//            Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
//            if (javaVersion.startsWith("1.5.")) {
//                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
//                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, null, false});
//            } else {
//                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[]{String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class});
//                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[]{libName, "C_GetFunctionList", null, false});
//            }
//
//            //Open the PKCS11 session
//            System.out.println("            //Open the PKCS11 session");
//            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
//
//            // Token login
//            System.out.println("            //Token login");
//            pkcs11.C_Login(p11_session, 1, null);
//            CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);
//
//            // // Get available keys
//            //     System.out.println("            //Get available keys");
//            //     CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
//            //     attributes[0] = new CK_ATTRIBUTE();
//            //     attributes[0].type = PKCS11Constants.CKA_CLASS;
//            //     attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);
//
//            //     pkcs11.C_FindObjectsInit(p11_session, attributes);
//            //     long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);
//
//            // // points to auth_key
//            //     System.out.println("            //points to auth_key. No. of keys:"+keyHandles.length);
//
//            //     long signatureKey = keyHandles[0];		//test with other keys to see what you get
//            //     pkcs11.C_FindObjectsFinal(p11_session);
//
//
//            //     // initialize the signature method
//            //     System.out.println("            //initialize the signature method");
//            //     CK_MECHANISM mechanism = new CK_MECHANISM();
//            //     mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
//            //     mechanism.pParameter = null;
//            //     pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
//
//
//            // //...
//
//            //     // sign
//            //     System.out.println("            //sign");
//            //     byte[] signature = pkcs11.C_Sign(p11_session, "data".getBytes(Charset.forName("UTF-8")));
//            //     System.out.println("            //signature:"+encoder.encode(signature));
//
//            //     //cypher some document here
//
//
//            // //...
//
//            // // address
//            // System.out.println("            //address");
//            // PTEID_ADDR idData = pteid.GetAddr();
//            // if (null != idData)
//            //     PrintIDAddress(idData);
//
//
//            System.out.println("Generating RSA key ...");
//            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//            keyGen.initialize(1024);
//            KeyPair keys = keyGen.generateKeyPair();
//            System.out.println("Finish generating RSA keys");
//            System.out.println("Private Key:");
//            PrivateKey privKey = keys.getPrivate();
//            byte[] privKeyEncoded = privKey.getEncoded();
//            System.out.println(printHexBinary(privKeyEncoded));
//            System.out.println("Public Key:");
//            PublicKey pubKey = keys.getPublic();
//            byte[] pubKeyEncoded = pubKey.getEncoded();
//            System.out.println(printHexBinary(pubKeyEncoded));
//            System.out.println(pubKeyEncoded);
//
//
//            // sign my public key
//            System.out.println("            //sign my public key");
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] thedigest = md.digest(pubKeyEncoded);
//
//
//            // Get available keys
//            System.out.println("            //Get available keys");
//            CK_ATTRIBUTE[] attributes_lab = new CK_ATTRIBUTE[1];
//            attributes_lab[0] = new CK_ATTRIBUTE();
//            attributes_lab[0].type = PKCS11Constants.CKA_CLASS;
//            attributes_lab[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);
//
//            pkcs11.C_FindObjectsInit(p11_session, attributes_lab);
//            long[] keyHandles_lab = pkcs11.C_FindObjects(p11_session, 5);
//
//            // points to auth_key
//            System.out.println("            //points to auth_key. No. of keys:" + keyHandles_lab.length);
//
//            long signatureKey_lab = keyHandles_lab[0];        //test with other keys to see what you get
//            pkcs11.C_FindObjectsFinal(p11_session);
//
//
//            // initialize the signature method
//            System.out.println("            //initialize the signature method");
//            CK_MECHANISM mechanism_lab = new CK_MECHANISM();
//            mechanism_lab.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
//            mechanism_lab.pParameter = null;
//            pkcs11.C_SignInit(p11_session, mechanism_lab, signatureKey_lab);
//
//            byte[] signaturePublicKey = pkcs11.C_Sign(p11_session, thedigest);
//            System.out.println("            //signature my public key:" + encoder.encode(signaturePublicKey));
//
//
//            // Encrypt stuff
//
//            Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.ENCRYPT_MODE, privKey);
//
//            System.out.println("            //my cyphered text:" + cipher.doFinal("olaaaaaa".getBytes()));
//
//            Signature signature1 = Signature.getInstance("SHA1withRSA");
//
//            X509Certificate cert2 = getCertFromByteArray(getCertificateInBytes(0));
//
//            signature1.initVerify(cert2);
//            signature1.update(thedigest); // what i signed
//            boolean result = signature1.verify(signaturePublicKey);
//
//            System.out.println("           Correcly signed ? " + result);
//
//            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
//
//
//        } catch (Exception e) {
//
//
//        }

    }

    // Returns the n-th certificate, starting from 0
    private static byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            System.out.println("Number of certs found: " + certs.length);
            int i = 0;
            for (PTEID_Certif cert : certs) {
                System.out.println("-------------------------------\nCertificate #" + (i++));
                System.out.println(cert.certifLabel);
            }

            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

            //pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a eID Lib
        } catch (PteidException e) {
            e.printStackTrace();
        }
        return certificate_bytes;
    }

    public static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException {
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(certificateEncoded);
        X509Certificate cert = (X509Certificate) f.generateCertificate(in);
        return cert;
    }
}