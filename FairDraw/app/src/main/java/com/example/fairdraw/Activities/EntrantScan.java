package com.example.fairdraw.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fairdraw.Others.BarType;
import com.example.fairdraw.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

/**
 * Activity that provides a continuous barcode/QR code scanner for entrants.
 * <p>
 * Handles runtime camera permission, starts a continuous decode callback and
 * routes scanned content (as a URL) to an ACTION_VIEW Intent.
 */
public class EntrantScan extends BaseTopBottomActivity {

    private DecoratedBarcodeView barcodeScanner;
    private boolean handled; // prevent double navigation
    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startScanning();
                else //Toast.makeText(this, "Camera permission is required to scan.", Toast.LENGTH_LONG).show();
                    Snackbar.make(findViewById(android.R.id.content), "Camera permission is required to scan.", Snackbar.LENGTH_LONG).show();
            });

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (handled || result == null) return;
            String contents = result.getText();
            if (contents == null || contents.isEmpty()) return;

            handled = true; // avoid multiple triggers

            // For now: just show and navigate to MainActivity (pass the raw text for testing)
            Snackbar.make(barcodeScanner, "Scanned: " + contents, Snackbar.LENGTH_SHORT).show();

            Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
            startActivity(view);
            finish();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // no-op
        }
    };

    /**
     * Setup UI and request camera permission if needed.
     * @param savedInstanceState saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barcodeScanner = findViewById(R.id.barcodeScanner);
        barcodeScanner.setStatusText("");
        ensureCameraPermissionAndStart();
        // Init shared top and bottom nav
        initTopNav(BarType.ENTRANT);
        initBottomNav(BarType.ENTRANT, findViewById(R.id.home_bottom_nav_bar));

        BottomNavigationView bottomNav = findViewById(R.id.home_bottom_nav_bar);
        if (bottomNav != null) bottomNav.setSelectedItemId(R.id.scan_activity);
    }

    /**
     * Verify camera permission and request it if not already granted.
     */
    private void ensureCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Begins continuous decoding and resumes the scanner view.
     */
    private void startScanning() {
        handled = false;
        barcodeScanner.decodeContinuous(callback);
        barcodeScanner.resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If returning from settings or permission dialog
        if (barcodeScanner != null) barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        if (barcodeScanner != null) barcodeScanner.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (barcodeScanner != null) barcodeScanner.pause();
        super.onDestroy();
    }
}