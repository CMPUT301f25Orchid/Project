package com.example.fairdraw;

import android.net.Uri;
import android.os.Bundle;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

public final class DeepLinkUtil {
    private DeepLinkUtil() {}

    /**
     * This function
     * How to use function
     * testButton.setOnClickListener(v -> {
     *             Bundle extras = new Bundle();
     *             extras.putString("eventId", "EVT_42");
     *             extras.putString("who", "Entrant");
     *             Uri link = DeepLinkUtil.buildLink(TestDeepLinkActivity.class, extras);
     *         });
     * */
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

    private static String safe(String s) {
        try {
            // Encode to avoid spaces/utf issues in query values
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}
