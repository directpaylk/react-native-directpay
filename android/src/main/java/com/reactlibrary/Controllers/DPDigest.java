package com.reactlibrary.Controllers;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

class DPDigest {

    private static final String TAG = "DPDIGEST";

    public static String encode(String jsonString) {
        jsonString = jsonString == null ? "" : jsonString;
        try {
            String merchantIdentifier = Constants.MERCHANT_ID + Constants.API_KEY;
            String appKey = Base64.encodeToString(merchantIdentifier.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
            String formattedString = jsonString + merchantIdentifier + appKey;

//            Log.d(TAG, "encode: MERCHANT_IDENTIFIER: " + merchantIdentifier);
//            Log.d(TAG, "encode: APP_KEY: " + appKey);
//            Log.i(TAG, "encode: FORMATTED_STRING: " + formattedString);

            String digest = getSHA(formattedString.trim());
//            Log.d(TAG, "ENCODE: DIGEST: " + digest);
            return digest;
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String getSHA(String input) {
//        Log.d(TAG, "getSHA() called with: input = " + input + "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (Exception ignored) {
        }
        return null;

//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//
//            byte[] messageDigest = md.digest(input.getBytes());
//
//            BigInteger no = new BigInteger(1, messageDigest);
//
//            StringBuilder hashtext = new StringBuilder(no.toString(16));
//
//            while (hashtext.length() < 32) {
//                hashtext.insert(0, "0");
//            }
//
//            return hashtext.toString();
//        } catch (NoSuchAlgorithmException e) {
//            Log.e(TAG, "getSHA: ERROR" + Arrays.toString(e.getStackTrace()));
//
//            return null;
//        }
    }
}
