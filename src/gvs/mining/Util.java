package gvs.mining;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/*
Create an object of this class to validate your solutions for the computational race GVS challenge.
 */
public class Util {
    /*
    Use this function to convert bytes to a String
     */
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private MessageDigest md;

    public Util() throws java.security.NoSuchAlgorithmException {
        this.md = MessageDigest.getInstance("SHA-256");
    }

    public boolean validate(String challenge, String attempt) {
        md.update(attempt.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(md.digest()).startsWith(challenge);
    }
}