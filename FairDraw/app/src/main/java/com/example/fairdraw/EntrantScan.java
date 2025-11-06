package com.example.fairdraw;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class EntrantScan extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScanner;
    private boolean handled; // prevent double navigation
    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startScanning();
                else Toast.makeText(this, "Camera permission is required to scan.", Toast.LENGTH_LONG).show();
            });

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (handled || result == null) return;
            String contents = result.getText();
            if (contents == null || contents.isEmpty()) return;

            handled = true; // avoid multiple triggers

            // For now: just show and navigate to MainActivity (pass the raw text for testing)
            Toast.makeText(EntrantScan.this, "Scanned: " + contents, Toast.LENGTH_SHORT).show();

            Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(contents));
            startActivity(view);
            finish();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // no-op
        }
    };

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
    }

    private void ensureCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

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