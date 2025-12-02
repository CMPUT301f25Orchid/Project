package com.example.fairdraw.ServiceUtility;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * A service for interacting with Firebase Cloud Storage to manage entrant profile images and event posters.
 * This class provides methods to upload, download, delete, and check for the existence of images.
 */
public class FirebaseImageStorageService {

    private static final String ENTRANTS_DIR = "entrants";
    private static final String EVENTS_DIR   = "events";
    private static final String PROFILE_NAME = "profile.jpg";
    private static final String POSTER_NAME  = "poster.jpg";
    private static final String QR_NAME      = "qr.png";

    private final FirebaseStorage storage;

    private static final String TAG = "FirebaseImageStorage";

    /**
     * Simple DTO for mapping an event ID to its poster download Uri.
     */
    public static class EventPosterInfo {
        public final String eventId;
        public final Uri downloadUri;

        public EventPosterInfo(@NonNull String eventId, @NonNull Uri downloadUri) {
            this.eventId = eventId;
            this.downloadUri = downloadUri;
        }
    }

    /**
     * Callback interface for receiving event poster list updates.
     */
    public interface EventPostersCallback {
        /**
         * Called when the list of event posters is retrieved.
         * @param posters List of EventPosterInfo objects, or null on error.
         */
        void onCallback(@Nullable List<EventPosterInfo> posters);
    }

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

    // QR storage reference for events
    private StorageReference eventQrRef(@NonNull String eventId) {
        return storage.getReference()
                .child(EVENTS_DIR)
                .child(eventId)
                .child(QR_NAME);
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
        return putBytesAndGetUrl(entrantProfileRef(entrantId), data, "image/*");
    }

    /**
     * Uploads an entrant's profile picture from a file URI.
     *
     * @param entrantId The ID of the entrant.
     * @param fileUri   The URI of the image file.
     * @return A Task containing the download URL of the uploaded image.
     */
    public Task<Uri> uploadEntrantProfile(@NonNull String entrantId, @NonNull Uri fileUri) {
        return putFileAndGetUrl(entrantProfileRef(entrantId), fileUri, "image/*");
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
        return putBytesAndGetUrl(eventPosterRef(eventId), data, "image/*");
    }

    /**
     * Uploads an event poster from a file URI.
     *
     * @param eventId The ID of the event.
     * @param fileUri The URI of the image file.
     * @return A Task containing the download URL of the uploaded image.
     */
    public Task<Uri> uploadEventPoster(@NonNull String eventId, @NonNull Uri fileUri) {
        return putFileAndGetUrl(eventPosterRef(eventId), fileUri, "image/*");
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

    /**
     * Lists all event posters currently stored under the EVENTS_DIR.
     * <p>
     * This is primarily intended for admin use: it scans each child folder
     * under {@code /events} (where each folder name is an eventId),
     * attempts to resolve {@code poster.jpg}, and for every event where a
     * poster exists, returns an {@link EventPosterInfo} containing the
     * eventId and its download Uri.
     *
     * @return A Task that resolves to a list of EventPosterInfo objects.
     *         Only events that actually have a poster are included.
     */
    public Task<List<EventPosterInfo>> listAllEventPosters() {
        StorageReference eventsRoot = storage.getReference().child(EVENTS_DIR);

        return eventsRoot.listAll()
                .continueWithTask(listTask -> {
                    if (!listTask.isSuccessful()) {
                        Exception e = listTask.getException();
                        if (e != null) {
                            Log.e(TAG, "Failed to list events directory in Storage", e);
                            return Tasks.forException(e);
                        }
                        return Tasks.forException(
                                new IllegalStateException("Failed to list events directory in Storage"));
                    }

                    ListResult listResult = listTask.getResult();
                    if (listResult == null || listResult.getPrefixes().isEmpty()) {
                        // No event folders; return an empty list.
                        return Tasks.forResult(new ArrayList<EventPosterInfo>());
                    }

                    List<StorageReference> eventFolders = listResult.getPrefixes();
                    List<Task<EventPosterInfo>> perEventTasks = new ArrayList<>();

                    for (StorageReference eventFolder : eventFolders) {
                        final String eventId = eventFolder.getName();
                        StorageReference posterRef = eventFolder.child(POSTER_NAME);

                        // Try to fetch the download URL. If it fails (no file / permission), we skip it.
                        Task<EventPosterInfo> posterTask = posterRef
                                .getDownloadUrl()
                                .continueWith(uriTask -> {
                                    if (!uriTask.isSuccessful()) {
                                        // Most likely means there is no poster.jpg for this event.
                                        if (uriTask.getException() != null) {
                                            Log.w(TAG, "No poster for event " + eventId
                                                    + " or failed to fetch URL.", uriTask.getException());
                                        }
                                        return null; // Signal "no poster" for this event.
                                    }

                                    Uri uri = uriTask.getResult();
                                    if (uri == null) {
                                        return null;
                                    }
                                    return new EventPosterInfo(eventId, uri);
                                });

                        perEventTasks.add(posterTask);
                    }

                    // When all per-event tasks complete, collect the successful ones.
                    return Tasks.whenAllSuccess(perEventTasks)
                            .continueWith(resultTask -> {
                                List<?> rawResults = resultTask.getResult();
                                List<EventPosterInfo> posters = new ArrayList<>();
                                if (rawResults == null) {
                                    return posters;
                                }

                                for (Object obj : rawResults) {
                                    if (obj instanceof EventPosterInfo) {
                                        posters.add((EventPosterInfo) obj);
                                    }
                                }
                                return posters;
                            });
                });
    }

    /**
     * Fetches all event posters and delivers them via a callback.
     * <p>
     * This method provides a callback-based interface for fetching event posters,
     * making it easier to use in lifecycle-aware components. Unlike Firestore,
     * Firebase Storage does not support real-time listeners, so this performs
     * a one-time fetch when called.
     *
     * @param callback The callback to receive the list of event posters.
     *                 Called with the list on success, or null on failure.
     */
    public void listenToEventPosters(@NonNull EventPostersCallback callback) {
        listAllEventPosters()
                .addOnSuccessListener(posters -> callback.onCallback(posters))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch event posters", e);
                    callback.onCallback(null);
                });
    }


    // ---------- Public API: Event QR (new) ----------

    /**
     * Uploads an event QR image from a Bitmap. Prefers PNG encoding; falls back to JPEG if PNG exceeds maxBytes.
     *
     * @param eventId     The ID of the event.
     * @param bitmap      The QR image as a Bitmap.
     * @param jpegQuality The initial JPEG compression quality (used only if PNG is too large).
     * @param maxBytes    The maximum allowed size of the compressed image in bytes.
     * @return A Task containing the download URL of the uploaded QR image.
     */
    public Task<Uri> uploadEventQr(@NonNull String eventId,
                                   @NonNull Bitmap bitmap,
                                   int jpegQuality,
                                   int maxBytes) {
        byte[] data = compressBitmapToPngBytes(bitmap, maxBytes);
        // If PNG exceeded maxBytes, compressBitmapToPngBytes will fall back to JPEG compression already.
        if (data.length > maxBytes) {
            // As a final attempt, re-encode with provided jpegQuality limits
            data = compressBitmapToJpegBytes(bitmap, jpegQuality, maxBytes);
        }
        return putBytesAndGetUrl(eventQrRef(eventId), data, "image/png");
    }

    /**
     * Uploads an event QR image from a file Uri.
     *
     * @param eventId The ID of the event.
     * @param fileUri The URI of the QR image file.
     * @return A Task containing the download URL of the uploaded QR image.
     */
    public Task<Uri> uploadEventQr(@NonNull String eventId, @NonNull Uri fileUri) {
        return putFileAndGetUrl(eventQrRef(eventId), fileUri, "image/png");
    }

    /**
     * Gets the download URL for an event QR image.
     *
     * @param eventId The ID of the event.
     * @return A Task containing the download URL.
     */
    public Task<Uri> getEventQrDownloadUrl(@NonNull String eventId) {
        return eventQrRef(eventId).getDownloadUrl();
    }

    /**
     * Downloads an event QR image as a byte array.
     *
     * @param eventId          The ID of the event.
     * @param maxDownloadBytes The maximum number of bytes to download.
     * @return A Task containing the image data as a byte array.
     */
    public Task<byte[]> getEventQrBytes(@NonNull String eventId, long maxDownloadBytes) {
        return eventQrRef(eventId).getBytes(maxDownloadBytes);
    }

    /**
     * Deletes an event QR image.
     *
     * @param eventId The ID of the event.
     * @return A Task that completes when the deletion is finished.
     */
    public Task<Void> deleteEventQr(@NonNull String eventId) {
        return eventQrRef(eventId).delete();
    }

    /**
     * Checks if an event QR image exists.
     *
     * @param eventId The ID of the event.
     * @return A Task containing true if the QR image exists, false otherwise.
     */
    public Task<Boolean> eventQrExists(@NonNull String eventId) {
        return exists(eventQrRef(eventId));
    }

    // ---------- Internals ----------
    private Task<Uri> putBytesAndGetUrl(StorageReference ref, byte[] data, String contentType) {
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .build();

        UploadTask upload = ref.putBytes(data, metadata);
        return upload.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Exception e = (Exception) task.getException();
                if (e != null) throw e;
                throw new Exception("Upload task failed");
            }
            return ref.getDownloadUrl();
        });
    }

    private Task<Uri> putFileAndGetUrl(StorageReference ref, Uri fileUri, String contentType) {
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .build();

        UploadTask upload = ref.putFile(fileUri, metadata);
        return upload.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Exception e = (Exception) task.getException();
                if (e != null) throw e;
                throw new Exception("Upload task failed");
            }
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

    /**
     * Try PNG encoding first (lossless). If PNG bytes exceed maxBytes, fall back to JPEG compression.
     */
    private byte[] compressBitmapToPngBytes(@NonNull Bitmap bitmap, int maxBytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] png = baos.toByteArray();
        if (png.length <= maxBytes) return png;
        // Fallback: convert to JPEG with progressive quality reductions
        return compressBitmapToJpegBytes(bitmap, 90, maxBytes);
    }
}
