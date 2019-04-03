package pt.ulisboa.tecnico.sec.notary.util;

import pteidlib.PTEID_Certif;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


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

    // Returns the n-th certificate, starting from 0
    private static byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
//            System.out.println("Number of certs found: " + certs.length);
//            int i = 0;
//            for (PTEID_Certif cert : certs) {
//                System.out.println("-------------------------------\nCertificate #" + (i++));
//                System.out.println(cert.certifLabel);
//            }
            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif

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

    public X509Certificate getCertificate(){
        X509Certificate cert = null;
        try{
            System.out.println("[CITIZENCARD] PTEidlibj Loading...");
            String javaLibPath = System.getProperty("java.library.path");
            System.out.println(javaLibPath);
            System.loadLibrary("pteidlibj");
            System.out.println("[CITIZENCARD] PTEidlibj Loaded");
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)

            // access the ID and Address data via the pteidlib
            System.out.println("            -- accessing the ID  data via the pteidlib interface");

             cert = getCertFromByteArray(getCertificateInBytes(0));
            this.terminate();
            return cert;
        } catch(Exception e){
            e.printStackTrace();
        }
        return cert;
    }

    public byte[] sign(byte[] contentToSign){
        byte[] sig = null;
        try {

            System.out.println("[CITIZENCARD] PTEidlibj Loading...");
            String javaLibPath = System.getProperty("java.library.path");
            System.out.println("java.library.path = " + javaLibPath);
            System.loadLibrary("pteidlibj");
            System.out.println("[CITIZENCARD] PTEidlibj Loaded");
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

            // Get available keys
            System.out.println("            //Get available keys");
            CK_ATTRIBUTE[] attributes_lab = new CK_ATTRIBUTE[1];
            attributes_lab[0] = new CK_ATTRIBUTE();
            attributes_lab[0].type = PKCS11Constants.CKA_CLASS;
            attributes_lab[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

            pkcs11.C_FindObjectsInit(p11_session, attributes_lab);
            long[] keyHandles_lab = pkcs11.C_FindObjects(p11_session, 5);

            long signatureKey_lab = keyHandles_lab[0];        //test with other keys to see what you get

            // initialize the signature method
            System.out.println("            //initialize the signature method");
            CK_MECHANISM mechanism_lab = new CK_MECHANISM();
            mechanism_lab.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
            mechanism_lab.pParameter = null;
            pkcs11.C_SignInit(p11_session, mechanism_lab, signatureKey_lab);
            sig = pkcs11.C_Sign(p11_session, contentToSign);
            this.terminate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sig;

    }

    public boolean checkSignature(byte[] contentToSign,byte[] sigFromCC, X509Certificate cert2){
        boolean result = false;
        try{
            Signature signature1 = Signature.getInstance("SHA1withRSA");
            java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

            System.out.println("            //signature my public key:" + encoder.encode(sigFromCC));

            signature1.initVerify(cert2);
            signature1.update(contentToSign);
            result = signature1.verify(sigFromCC);
        } catch(Exception e){
            e.printStackTrace();
        }
        if(result){
            return true;
        } else {
            return false;
        }
    }

    private void terminate(){
        try{
            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}