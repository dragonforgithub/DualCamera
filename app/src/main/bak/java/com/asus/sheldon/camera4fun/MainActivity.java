package com.asus.sheldon.camera4fun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends Activity {

    public Camera mCamera;
    private CameraPreview mCameraSurPreview = null;
    private Button mCaptureButton = null;
    private String TAG = "Sheldon";
    private int mCameraID = 0;
    private int mCameraindex = 0;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our Preview view and set it as the content of our activity.
        try {
            Log.d(TAG, "mCamera open id=" + 0);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

            if (mCameraID == 0) {
                mCameraindex = FindBackCamera();
                if (mCameraindex == -1)
                    return;
            } else if (mCameraID == 1) {
                mCameraindex = FindFrontCamera();
                if (mCameraindex == -1)
                    return;
            }

            mCamera = Camera.open(mCameraindex);
            mCamera.setDisplayOrientation(90);

            mCameraSurPreview = new CameraPreview(this, mCamera);
            preview.addView(mCameraSurPreview);
            //setContentView(mCameraSurPreview);
            mCamera.startPreview();
            Log.d(TAG, "camera open finish");
            // Add a listener to the Capture button
            mCaptureButton = (Button) findViewById(R.id.button_capture);
            mCaptureButton.setOnClickListener(new TakePicOnClick());
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "camera open fail");
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "camera open fail", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(TAG, "onDestroy");
        super.onDestroy();
    }

    private int FindFrontCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return camIdx;
            }
        }
        return -1;
    }

    private int FindBackCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return camIdx;
            }
        }
        return -1;
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //save the picture to sdcard
            String pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            Toast.makeText(MainActivity.this,"save to "+pictureFile+"...",Toast.LENGTH_LONG).show();

            try {
                if (data != null && pictureFile != null){
                    File rawOutput = new File(pictureFile);
                    FileOutputStream outStream = new FileOutputStream(rawOutput);
                    outStream.write(data);
                    outStream.close();
                }
            }catch(IOException e){

                Log.e(TAG,"Error save picture");
                Log.e(TAG,e.getMessage());
            }

            //See if need to enable or not
            mCaptureButton.setEnabled(true);
        }
    };

    class TakePicOnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCaptureButton.setEnabled(false);
            // get an image from the camera
            mCamera.takePicture(null, null, mPictureCallback);
        }
    }

    private String getOutputMediaFile() {

        String picPatch;
        //get the mobile Pictures directory
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        picPatch = picDir.getPath() + File.separator + "IMAGE_" + timeStamp + ".jpg";
        Log.e(TAG, "picPatch:" + picPatch);
        return picPatch;
    }
}
