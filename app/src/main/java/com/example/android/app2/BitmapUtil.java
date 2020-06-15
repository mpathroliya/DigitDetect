package com.example.android.app2;

import android.graphics.Bitmap;
import android.graphics.Matrix;


/**
 * Provides static functions to decode bitmaps at the optimal size
 */
public class BitmapUtil {

//    private static final String TAG = LogTag.getLogTag();
//    private static final boolean DEBUG = false;

    private BitmapUtil() {
    }


    /**
     * Returns a new Bitmap copy with a center-crop effect a la
     * {@link android.widget.ImageView.ScaleType#CENTER_CROP}. May return the input bitmap if no
     * scaling is necessary.
     *
     * @param src original bitmap of any size
     * @param w desired width in px
     * @param h desired height in px
     * @return a copy of src conforming to the given width and height, or src itself if it already
     *         matches the given width and height
     */
    public static Bitmap centerCrop(final Bitmap src, final int w, final int h) {
        return crop(src, w, h, 0.5f, 0.5f);
    }

    /**
     * Returns a new Bitmap copy with a crop effect depending on the crop anchor given. 0.5f is like
     * {@link android.widget.ImageView.ScaleType#CENTER_CROP}. The crop anchor will be be nudged
     * so the entire cropped bitmap will fit inside the src. May return the input bitmap if no
     * scaling is necessary.
     *
     *
     * Example of changing verticalCenterPercent:
     *   _________            _________
     *  |         |          |         |
     *  |         |          |_________|
     *  |         |          |         |/___0.3f
     *  |---------|          |_________|\
     *  |         |<---0.5f  |         |
     *  |---------|          |         |
     *  |         |          |         |
     *  |         |          |         |
     *  |_________|          |_________|
     *
     * @param src original bitmap of any size
     * @param w desired width in px
     * @param h desired height in px
     * @param horizontalCenterPercent determines which part of the src to crop from. Range from 0
     *                                .0f to 1.0f. The value determines which part of the src
     *                                maps to the horizontal center of the resulting bitmap.
     * @param verticalCenterPercent determines which part of the src to crop from. Range from 0
     *                              .0f to 1.0f. The value determines which part of the src maps
     *                              to the vertical center of the resulting bitmap.
     * @return a copy of src conforming to the given width and height, or src itself if it already
     *         matches the given width and height
     */
    public static Bitmap crop(final Bitmap src, final int w, final int h,
                              final float horizontalCenterPercent, final float verticalCenterPercent) {
        if (horizontalCenterPercent < 0 || horizontalCenterPercent > 1 || verticalCenterPercent < 0
                || verticalCenterPercent > 1) {
            throw new IllegalArgumentException(
                    "horizontalCenterPercent and verticalCenterPercent must be between 0.0f and "
                            + "1.0f, inclusive.");
        }
        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();

        // exit early if no resize/crop needed
        if (w == srcWidth && h == srcHeight) {
            return src;
        }

        final Matrix m = new Matrix();
        final float scale = Math.max(
                (float) w / srcWidth,
                (float) h / srcHeight);
        m.setScale(scale, scale);

        final int srcCroppedW, srcCroppedH;
        int srcX, srcY;

        srcCroppedW = Math.round(w / scale);
        srcCroppedH = Math.round(h / scale);
        srcX = (int) (srcWidth * horizontalCenterPercent - srcCroppedW / 2);
        srcY = (int) (srcHeight * verticalCenterPercent - srcCroppedH / 2);

        // Nudge srcX and srcY to be within the bounds of src
        srcX = Math.max(Math.min(srcX, srcWidth - srcCroppedW), 0);
        srcY = Math.max(Math.min(srcY, srcHeight - srcCroppedH), 0);

        final Bitmap cropped = Bitmap.createBitmap(src, srcX, srcY, srcCroppedW, srcCroppedH, m,
                true /* filter */);

//        if (DEBUG) LogUtils.i(TAG,
//                "BitmapUtils IN centerCrop, srcW/H=%s/%s desiredW/H=%s/%s srcX/Y=%s/%s" +
//                        " innerW/H=%s/%s scale=%s resultW/H=%s/%s",
//                srcWidth, srcHeight, w, h, srcX, srcY, srcCroppedW, srcCroppedH, scale,
//                cropped.getWidth(), cropped.getHeight());
//        if (DEBUG && (w != cropped.getWidth() || h != cropped.getHeight())) {
//            LogUtils.e(TAG, new Error(), "BitmapUtils last center crop violated assumptions.");
//        }

        return cropped;
    }
}