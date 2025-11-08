package com.example.fairdraw.Activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Minimal test activity used to display all Intent extras for debugging deep links.
 * The UI is generated programmatically and lists each extra key/value pair.
 */
public class TestDeepLinkActivity extends AppCompatActivity {

    /**
     * Build a small scrollable UI showing all extras received via the Intent.
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Minimal UI that lists all extras received
        ScrollView sc = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("TestDeepLinkActivity");
        title.setTextSize(20f);
        root.addView(title);

        Bundle ex = getIntent() != null ? getIntent().getExtras() : null;
        if (ex == null || ex.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No extras received.");
            root.addView(tv);
        } else {
            for (String k : ex.keySet()) {
                Object v = ex.get(k);
                TextView tv = new TextView(this);
                tv.setText(k + " = " + (v == null ? "null" : v.toString()));
                root.addView(tv);
            }
        }

        sc.addView(root);
        setContentView(sc);
    }
}