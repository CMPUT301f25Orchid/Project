package com.example.fairdraw.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.fairdraw.R;
import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.material.button.MaterialButton;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Canvas;
import android.content.ContentValues;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.net.Uri;
import android.media.MediaScannerConnection;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * DialogFragment that displays a QR code image for an event and allows the
 * user to download the QR image to their device.
 *
 * <p>The fragment fetches the QR image from Firebase Storage using the
 * {@link com.example.fairdraw.ServiceUtility.FirebaseImageStorageService} and
 * displays it with Glide. The QR image may be saved to the Pictures/FairDraw
 * folder on the device; for Android Q+ the MediaStore API is used.</p>
 */
public class QrCodeFragment extends DialogFragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";
    private String eventId;
    private String eventName;

    private ImageView imgQrCode;
    private MaterialButton btnDownload;
    private ImageButton btnClose;
    private TextView tvQrEventName;

    /**
     * Create a new instance of QrCodeFragment for the supplied event.
     *
     * @param eventId id of the event whose QR code will be shown
     * @param eventName friendly event name to display in the dialog
     * @return configured QrCodeFragment
     */
    public static QrCodeFragment newInstance(String eventId, String eventName) {
        QrCodeFragment fragment = new QrCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflate the fragment layout.
     *
     * @param inflater layout inflater
     * @param container parent view container
     * @param savedInstanceState saved state bundle
     * @return inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code, container, false);
    }

    /**
     * Initialize UI controls, load the QR image, and wire up button handlers.
     *
     * @param view created view hierarchy
     * @param savedInstanceState saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            // Ensure eventName is also populated from the arguments if provided
            eventName = getArguments().getString(ARG_EVENT_NAME);
        }

        imgQrCode = view.findViewById(R.id.imgQrCode);
        btnDownload = view.findViewById(R.id.btnDownloadQr);
        btnClose = view.findViewById(R.id.btnCloseQr);
        tvQrEventName = view.findViewById(R.id.tvQrEventName);

        // Fetch the QR code bitmap using the FirebaseImageStorageService and the eventid
        FirebaseImageStorageService storageService = new FirebaseImageStorageService();
        storageService.getEventQrDownloadUrl(eventId).addOnSuccessListener(uri -> {
            // Load the image into the ImageView using Glide
            Glide.with(requireContext())
                    .load(uri)
                    .into(imgQrCode);
            Log.d("QrCodeFragment", "Successfully loaded QR code image");
        }).addOnFailureListener(e -> {
            Log.d("QrCodeFragment", "Failed to load QR code image", e);
            // Show Snackbar to inform user of failure and to check logs
            Snackbar.make(view, "Failed to load QR code image. Please try again later.", Snackbar.LENGTH_LONG).show();

             // Close the fragment since QR code cannot be displayed
             dismiss();
         });

        // Close fragment when "X" is clicked
        btnClose.setOnClickListener(v -> dismiss());

        tvQrEventName.setText(eventName != null ? eventName : "Event");

        // Handle download
        btnDownload.setOnClickListener(v -> {
            // Convert ImageView drawable to Bitmap safely
            Drawable drawable = imgQrCode.getDrawable();
            if (drawable == null) {
                Snackbar.make(view, "No QR code to save", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Bitmap qrBitmap;
            if (drawable instanceof BitmapDrawable) {
                qrBitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : imgQrCode.getWidth();
                int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : imgQrCode.getHeight();
                if (width <= 0 || height <= 0) {
                    // fallback size
                    width = 512;
                    height = 512;
                }
                qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(qrBitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            if (qrBitmap == null) {
                Snackbar.make(view, "Could not obtain QR code image.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            saveBitmapToGallery(qrBitmap);
        });
    }

    /**
     * Save a bitmap to the user's Pictures/FairDraw folder. Uses MediaStore on Android Q+ and
     * falls back to writing to external storage on older devices and triggers a media scan.
     *
     * @param bitmap the QR bitmap to save
     */
    private void saveBitmapToGallery(Bitmap bitmap) {
        String filename = "fairdraw_qr_" + (eventId != null ? eventId : "") + "_" + System.currentTimeMillis() + ".png";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "FairDraw");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Failed to create new MediaStore record.");

                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new IOException("Failed to get output stream.");
                    boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    if (!compressed) throw new IOException("Failed to compress bitmap.");
                }

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                requireContext().getContentResolver().update(uri, values, null, null);

                Snackbar.make(requireActivity().findViewById(android.R.id.content), "QR code saved to Pictures/FairDraw", Snackbar.LENGTH_LONG).show();

            } else {
                // Older devices: write to external pictures folder (requires WRITE_EXTERNAL_STORAGE on < Q)
                File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FairDraw");
                if (!picturesDir.exists()) {
                    boolean created = picturesDir.mkdirs();
                    if (!created) Log.w("QrCodeFragment", "Failed to create Pictures/FairDraw directory");
                }
                File outFile = new File(picturesDir, filename);
                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    if (!compressed) throw new IOException("Failed to compress bitmap.");
                }

                // Make the image visible in gallery
                MediaScannerConnection.scanFile(requireContext(), new String[]{outFile.getAbsolutePath()}, new String[]{"image/png"}, (path, uri) -> {
                    // scan completed
                });

                Snackbar.make(requireActivity().findViewById(android.R.id.content), "QR code saved to Pictures/FairDraw", Snackbar.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("QrCodeFragment", "Error saving QR code", e);
            Snackbar.make(requireActivity().findViewById(android.R.id.content), "Failed to save QR code. Please check storage permissions.", Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Return the dialog theme used for this fragment's Dialog.
     *
     * @return resource id of the dialog theme
     */
    @Override
    public int getTheme() {
        // Use existing AppDialog style which is defined in styles.xml
        return R.style.AppDialog;
    }
}
