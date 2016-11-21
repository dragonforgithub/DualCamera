package com.asus.sheldon.camera4fun;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

public class FaceDetection implements Camera.FaceDetectionListener {

    private String TAG="FaceDetection:";
    private boolean isDetecting=false;
    private Context mContext;
    private FaceView faceView;
    int mCamID=0;
    int specificNo=0;

    public FaceDetection(Context c, FaceView mFV){
        mContext = c;
        faceView = mFV;
        //faceView.setMinimumHeight(500);
        //faceView.setMinimumWidth(300);
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if(faces != null){
            if (faces.length > 0){
                Log.i(TAG,"faces.length :"+faces.length);
                /*
                Log.e(  TAG, "face detected: "+ faces.length +
                        "X: " + faces[0].rect.centerX() +
                        "Y: " + faces[0].rect.centerY() );
                */
                faceView.setFaces(faces , mCamID, specificNo);
                faceView.setVisibility(View.VISIBLE);
            } else{
                faceView.setVisibility(View.INVISIBLE);
            }

        }

    }

    public void updateFaceStatus(int mSpecificNo){
        specificNo = mSpecificNo;
    }

    public void startFaceDetection(Camera mCamera, FaceDetection fd , int mCI){
        // Try starting Face Detection
        mCamID = mCI;
        Camera.Parameters params = mCamera.getParameters();
        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            isDetecting=true;
            Log.v(TAG, "face number:"+params.getMaxNumDetectedFaces());
            mCamera.setFaceDetectionListener(fd);
            mCamera.startFaceDetection();
        }
    }

    public void stopFaceDetection(Camera mCamera) {
        if (isDetecting==true) {
            Log.d(TAG, "stop FaceDetection:");
            faceView.clearFaces();
            mCamera.setFaceDetectionListener(null);
            mCamera.stopFaceDetection();
            isDetecting=false;
        }
    }
}


