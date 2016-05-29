package com.asus.sheldon.camera4fun;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by sheldon on 16-5-26.
 */

class FaceDetection implements Camera.FaceDetectionListener {

    private String TAG="FaceDetection:";
    private boolean isDetecting=false;
    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        Log.d(TAG,"faces.length :"+faces.length);
        if (faces.length > 0){
            Log.d(  TAG, "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    "Y: " + faces[0].rect.centerY() );
        }
    }

    public void startFaceDetection(Camera mCamera){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            isDetecting=true;
            // camera supports face detection, so can start it:
            params.getMaxNumDetectedFaces();
            Log.d(TAG, "face number:"+params.getMaxNumDetectedFaces());
            mCamera.startFaceDetection();
        }
    }

    public void stopFaceDetection(Camera mCamera) {
        if (isDetecting==true) {
            Log.d(TAG, "stop FaceDetection:");
            mCamera.stopFaceDetection();
            isDetecting=false;
        }
    }
}
