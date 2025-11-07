package com.example.fairdraw.ServiceUtility;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for generating QR codes.
 * This class provides static methods to create Bitmap QR codes from string content.
 * This class is final and cannot be instantiated.
 */
public final class QrUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private QrUtil() {}

    /**
     * Generates a QR code as a Bitmap from the given content string.
     * The QR code is encoded with minimal margin and rendered as a black-and-white bitmap.
     * 
     * Example usage:
     * <pre>
     * Uri link = DeepLinkUtil.buildLink(TestDeepLinkActivity.class, extras);
     * Bitmap qr = QrUtil.generate(link.toString(), 800);
     * imageView.setImageBitmap(qr);
     * </pre>
     * 
     * @param content The content to encode in the QR code (typically a URL or string)
     * @param size The width and height of the square QR code bitmap in pixels
     * @return A Bitmap containing the QR code image
     * @throws Exception If QR code generation fails
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
