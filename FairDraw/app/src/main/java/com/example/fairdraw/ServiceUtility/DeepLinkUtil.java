package com.example.fairdraw.ServiceUtility;

import android.net.Uri;
import android.os.Bundle;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Utility class for building and handling application deep links.
 *
 * <p>Deep links produced by this helper use the custom scheme <code>fairdraw://open</code>
 * and include a <code>dest</code> query parameter that carries the fully-qualified
 * activity class name to open. Additional extras may be encoded as query parameters.
 *
 * <p>All methods are static and the class is not instantiable.
 */
public final class DeepLinkUtil {
    private DeepLinkUtil() {}

    /**
     * Build a deep link URI targeting the provided activity class and including
     * any provided extras as URL-encoded query parameters.
     *
     * <p>Example usage:
     * <pre>
     * Bundle extras = new Bundle();
     * extras.putString("eventId", "EVT_42");
     * Uri link = DeepLinkUtil.buildLink(TestDeepLinkActivity.class, extras);
     * </pre>
     *
     * @param targetActivity the Activity class to launch when the deep link is opened
     * @param extras optional Bundle of extras to include as query parameters (may be null)
     * @return a Uri representing the deep link that can be opened by the app
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
     * URL-encodes the provided string using UTF-8 and falls back to the original
     * value if the encoding is not supported.
     *
     * @param s the string to encode
     * @return the URL-encoded string or the original value if encoding fails
     */
    private static String safe(String s) {
        try {
            // Encode to avoid spaces/utf issues in query values
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
