package com.example.fairdraw;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

public final class QrUtil {
    private QrUtil() {}

    /**
     * How to use:
     * testButton.setOnClickListener(v -> {
     *             Uri link = DeepLinkUtil.buildLink(TestDeepLinkActivity.class, extras);
     *             Bitmap qr = null;
     *             try {
     *                 qr = QrUtil.generate(link.toString(), 800);
     *             } catch (Exception e) {
     *                 throw new RuntimeException(e);
     *             }
     *             imageView.setImageBitmap(qr);
     * */
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
