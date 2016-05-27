package com.asus.sheldon.camera4fun;

import java.io.IOException;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by sheldon on 16-5-17.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder=null;
    private Camera mCamera=null;

    public CameraPreview(Context context, Camera camera, SurfaceView sv) {
        super(context);

        mCamera = camera;
        mHolder = sv.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        if(mHolder != null){
            mHolder.addCallback(this); //添加回调
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

        Log.d("Sheldon", "surfaceCreated() is called");

        try {
            // Open the Camera in preview mode
            if(mCamera == null) {
                mCamera = Camera.open(0);
            }
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.d("Sheldon", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d("Sheldon", "surfaceChanged() is called");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        Log.d("Sheldon", "surfaceDestroyed() is called");
    }
}
