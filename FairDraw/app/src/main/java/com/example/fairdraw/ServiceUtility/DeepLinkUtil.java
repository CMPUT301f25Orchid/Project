package com.example.fairdraw.ServiceUtility;

import android.net.Uri;
import android.os.Bundle;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

/**
 * Utility class for building deep link URIs in the FairDraw application.
 * Deep links allow navigation to specific activities with parameters encoded in the URI.
 * This class is final and cannot be instantiated.
 */
public final class DeepLinkUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private DeepLinkUtil() {}

    /**
     * Builds a deep link URI for navigating to a specific activity with optional extras.
     * The URI uses the format: fairdraw://open?dest=ActivityName&key1=value1&key2=value2
     * 
     * Example usage:
     * <pre>
     * Bundle extras = new Bundle();
     * extras.putString("eventId", "EVT_42");
     * extras.putString("who", "Entrant");
     * Uri link = DeepLinkUtil.buildLink(TestDeepLinkActivity.class, extras);
     * </pre>
     * 
     * @param targetActivity The target Activity class to navigate to
     * @param extras Optional Bundle containing key-value pairs to include as query parameters
     * @return A URI that can be used as a deep link
     */
    public static Uri buildLink(Class<?> targetActivity, Bundle extras) {
        Uri.Builder b = new Uri.Builder()
                .scheme("fairdraw")
                .authority("open")
                .appendQueryParameter("dest", targetActivity.getName());

        if (extras != null) {
            Set<String> keys = extras.keySet();
            for (String k : keys) {
                Object v = extras.get(k);
                if (v == null) continue;
                b.appendQueryParameter(k, safe(v.toString()));
            }
        }
        return b.build();
    }

    /**
     * URL-encodes a string to safely include it in a URI query parameter.
     * Falls back to the original string if encoding fails.
     * 
     * @param s The string to encode
     * @return The URL-encoded string, or the original string if encoding fails
     */
    private static String safe(String s) {
        try {
            // Encode to avoid spaces/utf issues in query values
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
