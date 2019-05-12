package pt.ulisboa.tecnico.sec.util;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.util.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.security.NoSuchAlgorithmException;

/**
 * Class for generation and parsing of <a href="http://www.hashcash.org/">HashCash</a><br>
 * Copyright 2006 Gregory Rubin <a href="mailto:grrubin@gmail.com">grrubin@gmail.com</a><br>
 *  Permission is given to use, modify, and or distribute this code so long as this message remains attached<br>
 * Please see the spec at: <a href="http://www.hashcash.org/">http://www.hashcash.org/</a>
 * @author grrubin@gmail.com
 * @version 1.1
 */
public class HashCash implements Comparable<HashCash> {
    public static final int DefaultVersion = 1;
    private static final int hashLength = 160;
    private static final String dateFormatString = "yyMMdd";
    private static long milliFor16 = -1;

    private String myToken;
    private int myValue;
    private Calendar myDate;
    private Map<String, List<String> > myExtensions;
    private int myVersion;
    private String myResource;


    /**
     * Mints a version 1 HashCash using now as the date
     * @param resource the string to be encoded in the HashCash
     * @throws NoSuchAlgorithmException If SHA1 is not a supported Message Digest
     */
    public static HashCash mintCash(String resource, int value) throws NoSuchAlgorithmException {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        return mintCash(resource, null, now, value, DefaultVersion);
    }


    public static HashCash mintCash(String resource, Map<String, List<String> > extensions, Calendar date, int value, int version)
            throws NoSuchAlgorithmException {
        if(version < 0 || version > 1)
            throw new IllegalArgumentException("Only supported versions are 0 and 1");

        if(value < 0 || value > hashLength)
            throw new IllegalArgumentException("Value must be between 0 and " + hashLength);

        if(resource.contains(":"))
            throw new IllegalArgumentException("Resource may not contain a colon.");

        HashCash result = new HashCash();

        MessageDigest md = MessageDigest.getInstance("SHA1");

        result.myResource = resource;
        result.myExtensions = (null == extensions ? new HashMap<String, List<String> >() : extensions);
        result.myDate = date;
        result.myVersion = version;

        String prefix;

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        switch(version) {
            case 0:
                prefix = version + ":" + dateFormat.format(date.getTime()) + ":" + resource + ":" +
                        serializeExtensions(extensions) + ":";
                result.myToken = generateCash(prefix, value, md);
                md.reset();
                md.update(result.myToken.getBytes());
                result.myValue = numberOfLeadingZeros(md.digest());
                break;

            case 1:
                result.myValue = value;
                prefix = version + ":" + value + ":" + dateFormat.format(date.getTime()) + ":" + resource + ":" +
                        serializeExtensions(extensions) + ":";
                result.myToken = generateCash(prefix, value, md);
                break;

            default:
                throw new IllegalArgumentException("Only supported versions are 0 and 1");
        }

        return result;
    }

    // Accessors
    /**
     * Two objects are considered equal if they are both of type HashCash and have an identical string representation
     */
    public boolean equals(Object obj) {
        if(obj instanceof HashCash)
            return toString().equals(obj.toString());
        else
            return super.equals(obj);
    }

    /**
     * Returns the canonical string representation of the HashCash
     */
    public String toString() {
        return myToken;
    }

    /**
     * Extra data encoded in the HashCash
     */
    public Map<String, List<String> > getExtensions() {
        return myExtensions;
    }

    /**
     * The primary resource being protected
     */
    public String getResource() {
        return myResource;
    }

    /**
     * The minting date
     */
    public Calendar getDate() {
        return myDate;
    }

    /**
     * The value of the HashCash (e.g. how many leading zero bits it has)
     */
    public int getValue() {
        return myValue;
    }

    /**
     * Which version of HashCash is used here
     */
    public int getVersion() {
        return myVersion;
    }

    // Private utility functions
    /**
     * Actually tries various combinations to find a valid hash.  Form is of prefix + random_hex + ":" + random_hex
     * @throws NoSuchAlgorithmException If SHA1 is not a supported Message Digest
     */
    private static String generateCash(String prefix, int value, MessageDigest md)
            throws NoSuchAlgorithmException {
        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        byte[] tmpBytes = new byte[4];
        rnd.nextBytes(tmpBytes);
        long random = unsignedIntToLong(tmpBytes);
        rnd.nextBytes(tmpBytes);
        long counter = unsignedIntToLong(tmpBytes);

        prefix = prefix + Long.toHexString(random) + ":";

        String temp;
        int tempValue;
        byte[] bArray;
        do {
            counter++;
            temp = prefix + Long.toHexString(counter);
            md.reset();
            md.update(temp.getBytes());
            bArray = md.digest();
            tempValue = numberOfLeadingZeros(bArray);
        } while ( tempValue < value);

        return temp;
    }

    /**
     * Converts a 4 byte array of unsigned bytes to an long
     * @param b an array of 4 unsigned bytes
     * @return a long representing the unsigned int
     */
    private static long unsignedIntToLong(byte[] b) {
        long l = 0;
        l |= b[0] & 0xFF;
        l <<= 8;
        l |= b[1] & 0xFF;
        l <<= 8;
        l |= b[2] & 0xFF;
        l <<= 8;
        l |= b[3] & 0xFF;
        return l;
    }

    /**
     * Serializes the extensions with (key, value) seperated by semi-colons and values seperated by commas
     */
    private static String serializeExtensions(Map<String, List<String> > extensions) {
        if(null == extensions || extensions.isEmpty())
            return "";

        StringBuffer result = new StringBuffer();
        List<String> tempList;
        boolean first = true;

        for(String key: extensions.keySet()) {
            if(key.contains(":") || key.contains(";") || key.contains("="))
                throw new IllegalArgumentException("Extension key contains an illegal character. " + key);
            if(!first)
                result.append(";");
            first = false;
            result.append(key);
            tempList = extensions.get(key);

            if(null != tempList) {
                result.append("=");
                for(int i = 0; i < tempList.size(); i++) {
                    if(tempList.get(i).contains(":") || tempList.get(i).contains(";") || tempList.get(i).contains(","))
                        throw new IllegalArgumentException("Extension value contains an illegal character. " + tempList.get(i));
                    if(i > 0)
                        result.append(",");
                    result.append(tempList.get(i));
                }
            }
        }
        return result.toString();
    }

    /**
     * Inverse of {@link #serializeExtensions(Map)}
     */
    private static Map<String, List<String> > deserializeExtensions(String extensions) {
        Map<String, List<String> > result = new HashMap<String, List<String> >();
        if(null == extensions || extensions.length() == 0)
            return result;

        String[] items = extensions.split(";");

        for(int i = 0; i < items.length; i++) {
            String[] parts = items[i].split("=", 2);
            if(parts.length == 1)
                result.put(parts[0], null);
            else
                result.put(parts[0], Arrays.asList(parts[1].split(",")));
        }

        return result;
    }

    /**
     * Counts the number of leading zeros in a byte array.
     */
    private static int numberOfLeadingZeros(byte[] values) {
        int result = 0;
        int temp = 0;
        for(int i = 0; i < values.length; i++) {

            temp = numberOfLeadingZeros(values[i]);

            result += temp;
            if(temp != 8)
                break;
        }

        return result;
    }

    /**
     * Returns the number of leading zeros in a bytes binary represenation
     */
    private static int numberOfLeadingZeros(byte value) {
        if(value < 0)
            return 0;
        if(value < 1)
            return 8;
        else if (value < 2)
            return  7;
        else if (value < 4)
            return 6;
        else if (value < 8)
            return 5;
        else if (value < 16)
            return 4;
        else if (value < 32)
            return 3;
        else if (value < 64)
            return 2;
        else if (value < 128)
            return 1;
        else
            return 0;
    }

    /**
     * Seeds the estimates by determining how long it takes to calculate a 16bit collision on average.
     * @throws NoSuchAlgorithmException If SHA1 is not a supported Message Digest
     */
    private static void initEstimates() throws NoSuchAlgorithmException {
        if(milliFor16 == -1) {
            long duration;
            duration = Calendar.getInstance().getTimeInMillis();
            for(int i = 0; i < 11; i++) {
                mintCash("estimation", 16);
            }
            duration = Calendar.getInstance().getTimeInMillis() - duration;
            milliFor16 = (duration /10);
        }
    }

    /**
     * Compares the value of two HashCashes
     * @param other
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(HashCash other) {
        if(null == other)
            throw new NullPointerException();

        return Integer.valueOf(getValue()).compareTo(Integer.valueOf(other.getValue()));
    }
}