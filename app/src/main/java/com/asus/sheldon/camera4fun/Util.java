package com.asus.sheldon.camera4fun;

import android.app.Instrumentation;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by sheldon on 16-7-11.
 */
public class Util {

        //prepareMatrix:最终是通过mMatrix.mapRect(mRect);来将mRect变换成UI坐标系的Rect.
        public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation, int viewWidth, int viewHeight) {
            // Need mirror for front camera.
            matrix.setScale(mirror ? -1 : 1, 1);
            // This is the value for android.hardware.Camera.setDisplayOrientation.
            matrix.postRotate(displayOrientation);
            // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
            // UI coordinates range from (0, 0) to (width, height).
            matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
            matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
        }
}
