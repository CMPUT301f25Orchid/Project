package com.example.fairdraw.ServiceUtility;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility for generating QR code Bitmaps.
 *
 * <p>Creates a square QR code with a configurable size. A minimal margin is applied
 * to produce compact QR images.
 */
public final class QrUtil {
    private QrUtil() {}

    /**
     * Generate a QR code bitmap for the provided content string.
     *
     * <p>Example:
     * <pre>
     * Uri link = DeepLinkUtil.buildLink(TestDeepLinkActivity.class, extras);
     * Bitmap qr = QrUtil.generate(link.toString(), 800);
     * imageView.setImageBitmap(qr);
     * </pre>
     *
     * @param content the text/URL to encode into the QR code
     * @param size the desired width and height of the generated bitmap in pixels
     * @return a Bitmap containing the QR code
     * @throws Exception if the QR encoding fails for any reason
     */
    public static Bitmap generate(String content, int size) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix m = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bmp.setPixel(x, y, m.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bmp;
    }
}
