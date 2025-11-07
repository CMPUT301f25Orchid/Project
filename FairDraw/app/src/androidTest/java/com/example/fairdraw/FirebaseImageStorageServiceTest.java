package com.example.fairdraw;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fairdraw.ServiceUtility.FirebaseImageStorageService;
import com.google.android.gms.tasks.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FirebaseImageStorageServiceTest {

    private static final long TIMEOUT_SEC = 30;     // generous timeout to avoid flakes
    private static final int MAX_DOWNLOAD_BYTES = 256 * 1024; // for getBytes()
    private static final int JPEG_QUALITY = 85;     // used by upload(Bitmap,...)
    private static final int MAX_UPLOAD_BYTES = 64 * 1024;

    private FirebaseImageStorageService service;
    private String testEntrantId;
    private String testEventId;

    @Before
    public void setUp() {
        service = new FirebaseImageStorageService();
        // unique IDs per run to avoid clashes on shared buckets
        testEntrantId = "test_entrant_" + UUID.randomUUID();
        testEventId   = "test_event_" + UUID.randomUUID();
    }

    @After
    public void tearDown() throws InterruptedException {
        // Best-effort cleanup
        CountDownLatch done = new CountDownLatch(2);
        service.deleteEntrantProfile(testEntrantId)
                .addOnCompleteListener(t -> done.countDown());
        service.deleteEventPoster(testEventId)
                .addOnCompleteListener(t -> done.countDown());
        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    // --- Helpers ---

    private Bitmap makeTestBitmap(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.BLACK);
        p.setTextSize(24f);
        c.drawRect(2, 2, w - 2, h - 2, p);
        p.setColor(Color.YELLOW);
        c.drawCircle(w / 2f, h / 2f, Math.min(w, h) / 4f, p);
        return bmp;
    }

    private <T> T awaitTask(Task<T> task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Object[] out = new Object[2]; // [0] = result, [1] = error
        task.addOnSuccessListener(result -> {
            out[0] = result;
            latch.countDown();
        }).addOnFailureListener(e -> {
            out[1] = e;
            latch.countDown();
        });
        if (!latch.await(TIMEOUT_SEC, TimeUnit.SECONDS)) {
            fail("Task timed out");
        }
        if (out[1] != null) {
            fail("Task failed: " + ((Throwable) out[1]).getMessage());
        }
        @SuppressWarnings("unchecked")
        T res = (T) out[0];
        return res;
    }

    // --- Tests: Entrant Profile ---

    @Test
    public void testEntrantProfile_UploadBitmap_GetUrl_GetBytes_Delete() throws Exception {
        Bitmap sample = makeTestBitmap(96, 96);

        // Upload (bitmap) -> URL
        Uri downloadUri = awaitTask(
                service.uploadEntrantProfile(testEntrantId, sample, JPEG_QUALITY, MAX_UPLOAD_BYTES)
        );
        assertNotNull("Download URL should not be null", downloadUri);

        // Exists?
        Boolean exists = awaitTask(service.entrantProfileExists(testEntrantId));
        assertTrue("Profile should exist after upload", exists);

        // Get bytes
        byte[] bytes = awaitTask(service.getEntrantProfileBytes(testEntrantId, MAX_DOWNLOAD_BYTES));
        assertNotNull("Downloaded bytes should not be null", bytes);
        assertTrue("Downloaded size should be > 0", bytes.length > 0);

        // Spot-check: re-upload the same bitmap and ensure bytes change is minimal or equal is tricky.
        // Instead, just confirm we can fetch URL directly as well.
        Uri directUrl = awaitTask(service.getEntrantProfileDownloadUrl(testEntrantId));
        assertNotNull("getDownloadUrl should succeed", directUrl);

        // Delete
        awaitTask(service.deleteEntrantProfile(testEntrantId));

        // Exists? (We expect getMetadata to fail -> exists() returns false)
        Boolean existsAfterDelete = awaitTask(service.entrantProfileExists(testEntrantId));
        assertFalse("Profile should NOT exist after delete", existsAfterDelete);
    }

    // --- Tests: Event Poster ---

    @Test
    public void testEventPoster_UploadBitmap_GetUrl_GetBytes_Delete() throws Exception {
        Bitmap poster = makeTestBitmap(256, 144); // small landscape

        // Upload (bitmap) -> URL
        Uri posterUrl = awaitTask(
                service.uploadEventPoster(testEventId, poster, JPEG_QUALITY, MAX_UPLOAD_BYTES)
        );
        assertNotNull("Poster download URL should not be null", posterUrl);

        // Exists?
        Boolean exists = awaitTask(service.eventPosterExists(testEventId));
        assertTrue("Poster should exist after upload", exists);

        // Get bytes
        byte[] bytes = awaitTask(service.getEventPosterBytes(testEventId, MAX_DOWNLOAD_BYTES));
        assertNotNull("Downloaded poster bytes should not be null", bytes);
        assertTrue("Downloaded poster bytes length should be > 0", bytes.length > 0);

        // Direct URL
        Uri directUrl = awaitTask(service.getEventPosterDownloadUrl(testEventId));
        assertNotNull("getDownloadUrl for poster should succeed", directUrl);

        // Delete
        awaitTask(service.deleteEventPoster(testEventId));

        // Exists? (should be false)
        Boolean existsAfterDelete = awaitTask(service.eventPosterExists(testEventId));
        assertFalse("Poster should NOT exist after delete", existsAfterDelete);
    }

    // --- Optional: sanity test round-trip of small exact bytes ---
    // NOTE: JPEG is lossy, so round-trip equality won't hold. Instead we just ensure we can download.
    @Test
    public void testEntrantProfile_Reupload_StillRetrievable() throws Exception {
        Bitmap a = makeTestBitmap(80, 80);
        awaitTask(service.uploadEntrantProfile(testEntrantId, a, 90, MAX_UPLOAD_BYTES));

        // second upload should overwrite same path
        Bitmap b = makeTestBitmap(80, 80);
        awaitTask(service.uploadEntrantProfile(testEntrantId, b, 75, MAX_UPLOAD_BYTES));

        byte[] bytes = awaitTask(service.getEntrantProfileBytes(testEntrantId, MAX_DOWNLOAD_BYTES));
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // cleanup for this test (also done in tearDown)
        awaitTask(service.deleteEntrantProfile(testEntrantId));
    }
}
