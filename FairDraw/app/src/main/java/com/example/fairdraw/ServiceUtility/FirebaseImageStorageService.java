package com.example.fairdraw.ServiceUtility;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * A service for interacting with Firebase Cloud Storage to manage entrant profile images and event posters.
 * This class provides methods to upload, download, delete, and check for the existence of images.
 */
public class FirebaseImageStorageService {

    private static final String ENTRANTS_DIR = "entrants";
    private static final String EVENTS_DIR   = "events";
    private static final String PROFILE_NAME = "profile.jpg";
    private static final String POSTER_NAME  = "poster.jpg";

    private final FirebaseStorage storage;

    /**
     * Default constructor. Initializes the Firebase Storage instance.
     */
    public FirebaseImageStorageService() {
        this.storage = FirebaseStorage.getInstance();
    }

    // ---------- Path helpers ----------
    private StorageReference entrantProfileRef(@NonNull String entrantId) {
        return storage.getReference()
                .child(ENTRANTS_DIR)
                .child(entrantId)
                .child(PROFILE_NAME);
    }

    private StorageReference eventPosterRef(@NonNull String eventId) {
        return storage.getReference()
                .child(EVENTS_DIR)
                .child(eventId)
                .child(POSTER_NAME);
    }

    // ---------- Public API: Entrant Profile ----------

    /**
     * Uploads an entrant's profile picture from a Bitmap.
     * The bitmap is compressed to a JPEG with the specified quality and byte limit.
     *
     * @param entrantId   The ID of the entrant.
     * @param bitmap      The profile image as a Bitmap.
     * @param jpegQuality The initial JPEG compression quality (1-100).
     * @param maxBytes    The maximum allowed size of the compressed image in bytes.
     * @return A Task containing the download URL of the uploaded image.
     */
    public Task<Uri> uploadEntrantProfile(@NonNull String entrantId,
                                          @NonNull Bitmap bitmap,
                                          int jpegQuality,
                                          int maxBytes) {
        byte[] data = compressBitmapToJpegBytes(bitmap, jpegQuality, maxBytes);
        return putBytesAndGetUrl(entrantProfileRef(entrantId), data, "image/jpeg");
    }

    /**
     * Uploads an entrant's profile picture from a file URI.
     *
     * @param entrantId The ID of the entrant.
     * @param fileUri   The URI of the image file.
     * @return A Task containing the download URL of the uploaded image.
     */
    public Task<Uri> uploadEntrantProfile(@NonNull String entrantId, @NonNull Uri fileUri) {
        return putFileAndGetUrl(entrantProfileRef(entrantId), fileUri, "image/jpeg");
    }

    /**
     * Gets the download URL for an entrant's profile picture.
     *
     * @param entrantId The ID of the entrant.
     * @return A Task containing the download URL.
     */
    public Task<Uri> getEntrantProfileDownloadUrl(@NonNull String entrantId) {
        return entrantProfileRef(entrantId).getDownloadUrl();
    }

    /**
     * Downloads an entrant's profile picture as a byte array.
     *
     * @param entrantId        The ID of the entrant.
     * @param maxDownloadBytes The maximum number of bytes to download.
     * @return A Task containing the image data as a byte array.
     */
    public Task<byte[]> getEntrantProfileBytes(@NonNull String entrantId, long maxDownloadBytes) {
        return entrantProfileRef(entrantId).getBytes(maxDownloadBytes);
    }

    /**
     * Deletes an entrant's profile picture.
     *
     * @param entrantId The ID of the entrant.
     * @return A Task that completes when the deletion is finished.
     */
    public Task<Void> deleteEntrantProfile(@NonNull String entrantId) {
        return entrantProfileRef(entrantId).delete();
    }

    /**
     * Checks if an entrant's profile picture exists.
     *
     * @param entrantId The ID of the entrant.
     * @return A Task containing true if the profile picture exists, false otherwise.
     */
    public Task<Boolean> entrantProfileExists(@NonNull String entrantId) {
        return exists(entrantProfileRef(entrantId));
    }

    // ---------- Public API: Event Poster ----------

    /**
     * Uploads an event poster from a Bitmap.
     * The bitmap is compressed to a JPEG with the specified quality and byte limit.
     *
     * @param eventId     The ID of the event.
     * @param bitmap      The poster image as a Bitmap.
     * @param jpegQuality The initial JPEG compression quality (1-100).
     * @param maxBytes    The maximum allowed size of the compressed image in bytes.
     * @return A Task containing the download URL of the uploaded image.
     */
    public Task<Uri> uploadEventPoster(@NonNull String eventId,
                                       @NonNull Bitmap bitmap,
                                       int jpegQuality,
                                       int maxBytes) {
        byte[] data = compressBitmapToJpegBytes(bitmap, jpegQuality, maxBytes);
        return putBytesAndGetUrl(eventPosterRef(eventId), data, "image/jpeg");
    }

    /**
     * Uploads an event poster from a file URI.
     *
     * @param eventId The ID of the event.
     * @param fileUri The URI of the image file.
     * @return A Task containing the download URL of the uploaded image.
     */
    public Task<Uri> uploadEventPoster(@NonNull String eventId, @NonNull Uri fileUri) {
        return putFileAndGetUrl(eventPosterRef(eventId), fileUri, "image/jpeg");
    }

    /**
     * Gets the download URL for an event poster.
     *
     * @param eventId The ID of the event.
     * @return A Task containing the download URL.
     */
    public Task<Uri> getEventPosterDownloadUrl(@NonNull String eventId) {
        return eventPosterRef(eventId).getDownloadUrl();
    }

    /**
     * Downloads an event poster as a byte array.
     *
     * @param eventId          The ID of the event.
     * @param maxDownloadBytes The maximum number of bytes to download.
     * @return A Task containing the image data as a byte array.
     */
    public Task<byte[]> getEventPosterBytes(@NonNull String eventId, long maxDownloadBytes) {
        return eventPosterRef(eventId).getBytes(maxDownloadBytes);
    }

    /**
     * Deletes an event poster.
     *
     * @param eventId The ID of the event.
     * @return A Task that completes when the deletion is finished.
     */
    public Task<Void> deleteEventPoster(@NonNull String eventId) {
        return eventPosterRef(eventId).delete();
    }

    /**
     * Checks if an event poster exists.
     *
     * @param eventId The ID of the event.
     * @return A Task containing true if the poster exists, false otherwise.
     */
    public Task<Boolean> eventPosterExists(@NonNull String eventId) {
        return exists(eventPosterRef(eventId));
    }

    // ---------- Internals ----------
    private Task<Uri> putBytesAndGetUrl(StorageReference ref, byte[] data, String contentType) {
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .build();

        UploadTask upload = ref.putBytes(data, metadata);
        return upload.continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return ref.getDownloadUrl();
        });
    }

    private Task<Uri> putFileAndGetUrl(StorageReference ref, Uri fileUri, String contentType) {
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .build();

        UploadTask upload = ref.putFile(fileUri, metadata);
        return upload.continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return ref.getDownloadUrl();
        });
    }

    private Task<Boolean> exists(StorageReference ref) {
        // getMetadata() fails if the object doesn't exist
        return ref.getMetadata()
                .continueWith(task -> task.isSuccessful());
    }

    private byte[] compressBitmapToJpegBytes(@NonNull Bitmap bitmap, int initialQuality, int maxBytes) {
        int quality = Math.min(Math.max(initialQuality, 1), 100);
        byte[] out;
        do {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            out = baos.toByteArray();
            // reduce quality in steps if too large; clamp to avoid infinite loop
            if (out.length > maxBytes && quality > 10) {
                quality -= 10;
            } else {
                break;
            }
        } while (true);
        return out;
    }
}
