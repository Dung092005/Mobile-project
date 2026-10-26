package com.chess.stats.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/** Decodes downloaded avatar bytes at roughly the size they are displayed. */
public final class AvatarDecoder {

    private AvatarDecoder() {
    }

    /** Returns null when the bytes are not a decodable image. */
    public static Bitmap decode(byte[] data, int targetPx) {
        if (data == null || data.length == 0) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, targetPx);
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /** Largest power of two that keeps the smaller side at least {@code targetPx}. */
    static int sampleSize(int width, int height, int targetPx) {
        int sample = 1;
        if (targetPx <= 0) {
            return sample;
        }
        int smaller = Math.min(width, height);
        while (smaller / (sample * 2) >= targetPx) {
            sample *= 2;
        }
        return sample;
    }
}
