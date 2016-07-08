package com.asus.sheldon.camera4fun;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.params.Face;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FaceDetection implements Camera.FaceDetectionListener {

    private String TAG="FaceDetection:";
    private boolean isDetecting=false;
    private Context mContext;
    private FaceView faceView;

    public FaceDetection(Context c, Camera fCamera , LinearLayout mSV){
        mContext = c;
        faceView = new FaceView(c,fCamera);
        faceView.setMinimumHeight(500);
        faceView.setMinimumWidth(300);
        mSV.addView(faceView);
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        //Log.e(TAG,"faces.length :"+faces.length);

        if(faces != null){
            //Message m = mHander.obtainMessage();
            //m.what = EventUtil.UPDATE_FACE_RECT;
            //m.obj = faces;
            //m.sendToTarget();
            faceView.invalidate();
            faceView.setFaces(faces);
        }
        /*
        if (faces.length > 0){
            Log.e(  TAG, "face detected: "+ faces.length +
                         " Face 1 Location X: " + faces[0].rect.centerX() +
                         "Y: " + faces[0].rect.centerY() );
        }
        */
    }

    public void startFaceDetection(Camera mCamera, FaceDetection fd){
        // Try starting Face Detection
        Camera.Parameters params = mCamera.getParameters();
        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0){
            // camera supports face detection, so can start it:
            isDetecting=true;
            //if(faceView != null){
                //faceView.clearFaces();
                //faceView.setVisibility(View.VISIBLE);
            //}
            Log.v(TAG, "face number:"+params.getMaxNumDetectedFaces());
            mCamera.setFaceDetectionListener(fd);
            mCamera.startFaceDetection();
        }
    }

    public void stopFaceDetection(Camera mCamera) {
        if (isDetecting==true) {
            Log.d(TAG, "stop FaceDetection:");
            mCamera.setFaceDetectionListener(null);
            mCamera.stopFaceDetection();
            //faceView.clearFaces();
            isDetecting=false;
        }
    }
}


