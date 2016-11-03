package com.asus.sheldon.camera4fun;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

/**
 * Created by sheldon on 16-5-17.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder=null;
    private int CameraIndex=0;
    private Camera mCamera=null;
    private static final String TAG = "CameraPreview";

    public CameraPreview(Context context, Camera camera, SurfaceView sv, int CI) {
        super(context);
        mCamera = camera;
        CameraIndex = CI;
        mHolder = sv.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        if(mHolder != null){
            mHolder.addCallback(this); //添加回调
            // deprecated setting, but required on Android versions prior to 3.0
            Log.e("Sheldon", "mHolder.setType:");
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("Sheldon", "surfaceCreated() is called");
        try {
            if(mCamera != null && mHolder != null){
                Log.d("Sheldon", "mCamera.setPreviewDisplay-mHolder");
                mCamera.setPreviewDisplay(mHolder);
            }
        } catch (IOException e) {
            Log.d("Sheldon", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void setMaxPreviewAndPictureSize(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            Camera.Parameters parameters=mCamera.getParameters();
            int lw = 0, lh = 0;
            float picScale= (float) 0.0;
            float preScale= (float) 0.0;
            float diff = (float) 0.0;

            //set picture size
            List<Camera.Size> mSupportedPictureSizes = parameters.getSupportedPictureSizes();
            for (int i = 0; i < mSupportedPictureSizes.size(); ++i) {
                Camera.Size size = mSupportedPictureSizes.get(i);
                //Log.i(TAG, "mSupportedPictureSizes:" + size.width + "x" + size.height );
                if (size.width > lw || size.height > lh) {
                    lw = size.width;
                    lh = size.height;
                }
            }
            parameters.setPictureSize(lw, lh);
            picScale = (float)lw/(float)lh;
            Log.v(TAG, "picScale = " + picScale);
            Log.v(TAG, "set picturesize:" + lw + "x" + lh );

            lw = 0;
            lh = 0;
            //set preview size
            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (int i = 0; i < mSupportedPreviewSizes.size(); ++i) {
                Camera.Size size = mSupportedPreviewSizes.get(i);
                //Log.i(TAG, "mSupportedPreviewSizes:" + size.width + "x" + size.height );
                if (size.width > lw || size.height > lh) {
                    preScale = (float)size.width/(float)size.height;
                    diff = Math.abs(preScale - picScale);
                    //Log.v(TAG, "Scale diff = " + diff); //keep preview FOV same as picture
                    if(Math.abs(diff) < 0.1 ){
                        lw = size.width;
                        lh = size.height;
                    }
                }
            }
            parameters.setPreviewSize(lw, lh);
            Log.v(TAG, "set previewsize:" + lw + "x" + lh );

            //reset display size if not dual camera mode
            /*if(mCameraMode != 0){
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
                lp.height = lw;
                lp.width = lh;
                mSurfaceView.setLayoutParams(lp);
            }*/

            mCamera.setParameters(parameters);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.v("Sheldon", "surfaceChanged() is called");

        List<Camera.Size> supportedPreviewSizes;
        List<Camera.Size> supportedPictureSizes;

        if(mCamera == null){
            Log.e("Sheldon", "mCamera is null!");
            return;
        }

        /*
        int picWidth=640;
        int picHeight=480;

        Camera.Parameters parameters = mCamera.getParameters();
        //get current preview size list
        supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        //get current picture size list
        supportedPictureSizes = parameters.getSupportedPictureSizes();
        picWidth = Integer.parseInt((String.valueOf(supportedPictureSizes.get(0).width)));
        picHeight = Integer.parseInt((String.valueOf(supportedPictureSizes.get(0).height)));
        Log.i("Sheldon", "setPictureSize:"+picWidth+"X"+picHeight);
        parameters.setPictureSize(picWidth, picHeight);
        parameters.setPreviewSize(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);
        parameters.setRotation(90); //default picture rotation
        mCamera.setParameters(parameters);
        */
        setMaxPreviewAndPictureSize(mCamera);
        mCamera.startPreview();
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
