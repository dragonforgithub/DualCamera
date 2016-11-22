package com.asus.sheldon.camera4fun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
    public SurfaceView previewCamera=null;
    public SurfaceView previewCamera_sub=null;
    public SurfaceView previewCamera_full=null;
    public SurfaceView previewCamera_full_sub=null;

    public FaceView faceView=null;
    public TouchView touchView=null;

    //public TouchView touchView=null;
    public TextView timerTV=null;
    private Camera mCamera;
    private Camera mCamera_sub;

    private CameraPreview mCameraSurPreview = null;
    private CameraPreview mCameraSurPreview_sub = null;

    //private sMediaRecorder mSurRecorder = null;
    private ImageButton mCaptureButton = null;
    private ImageButton mSwitchButton = null;
    private ImageButton mVideoButton = null;
    private MyOrientationDetector mOrientationListener=null;

    private String TAG = "Sheldon";
    private int mCamera_mode=0;
    private int mCameraID = 0;
    private int mCameraindex = 0;
    private int mCameraindex_front = 1;
    private int mCameraindex_sub = 2;
    private boolean isRecording=false;
    private static boolean issupportFocuse=false;

    public int mOrientation=0;

    private List<String> supportedFlashMode;
    private List<String> supportedFocuseMode;
    private List<String> list_resolution=new ArrayList<String>();

    //private Spinner spinner_res;
    private Spinner spinner_flash;
    private Spinner spinner_mode;

    private int specificNo=0;
    private static String currentFocusMode="auto";
    private static String currentFlashMode="off";
    private float oldDist = 1f;

    //view detection
    //public FaceDetection faceDetect;
    private boolean focuseDone=false;
    private boolean needMirror=false;
    private boolean doZoom=true;
    private boolean doZoom_sub=false;

    private Bitmap mThumbImage;
    //private ImageView showCameraIv;

    private File mPictureFile;
    private String mSavePhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);//no title
         this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
         this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
        //设置手机屏幕朝向，一共有7种
        //SCREEN_ORIENTATION_BEHIND： 继承Activity堆栈中当前Activity下面的那个Activity的方向
        //SCREEN_ORIENTATION_LANDSCAPE： 横屏(风景照) ，显示时宽度大于高度
        //SCREEN_ORIENTATION_PORTRAIT： 竖屏 (肖像照) ， 显示时高度大于宽度
        //SCREEN_ORIENTATION_SENSOR  由重力感应器来决定屏幕的朝向,它取决于用户如何持有设备,当设备被旋转时方向会随之在横屏与竖屏之间变化
        //SCREEN_ORIENTATION_NOSENSOR： 忽略物理感应器——即显示方向与物理感应器无关，不管用户如何旋转设备显示方向都不会随着改变("unspecified"设置除外)
        //SCREEN_ORIENTATION_UNSPECIFIED： 未指定，此为默认值，由Android系统自己选择适当的方向，选择策略视具体设备的配置情况而定，因此不同的设备会有不同的方向选择
        //SCREEN_ORIENTATION_USER： 用户当前的首选方向
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        //mOrientationListener = new MyOrientationDetector(this);

        // Add a listener to the Capture button
        mCaptureButton = (ImageButton) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new ButtonPart());

        // Add a listener to the Switch button
        mSwitchButton = (ImageButton) findViewById(R.id.button_switch);
        mSwitchButton.setOnClickListener(new ButtonPart());

        // Add a listener to the video button
        mVideoButton = (ImageButton) findViewById(R.id.button_video);
        mVideoButton.setOnClickListener(new ButtonPart());

        // Add ImageView
        /*
        showCameraIv = (ImageView)this.findViewById(R.id.id_show_camera_iv);
        showCameraIv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, EventUtil.REQUEST_SELECT_PHOTO);
            }
        });
        */
        //get the mobile Pictures directory
        //mPictureFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //Log.i(TAG,"mPictureFile path="+mPictureFile.getPath());
        //processShowPicture(mPictureFile.getPath());

        //flash
        spinner_flash=(Spinner)findViewById(R.id.flashMode);
        //spinner_flash.setSelection(0, true);
        spinner_flash.setOnItemSelectedListener(new SpinnerSelectedListener());
        spinner_flash.setSelection(0);

        //mode
        spinner_mode=(Spinner)findViewById(R.id.cameraMode);
        spinner_mode.setSelection(0, true);
        spinner_mode.setOnItemSelectedListener(new SpinnerSelectedListener());

        //preview surface
        previewCamera = (SurfaceView) this.findViewById(R.id.preView);
        previewCamera_sub = (SurfaceView) this.findViewById(R.id.preView_sub);
        previewCamera_full = (SurfaceView) this.findViewById(R.id.preView_full);
        previewCamera_full_sub = (SurfaceView) this.findViewById(R.id.preView_full_sub);

        mCameraindex = FindBackCamera();
        if (mCameraindex == -1){
            Log.e(TAG, "No rear0 camera!");
        }

        mCameraindex_sub = FindBackCamera_2();
        if (mCameraindex_sub == -1){
            Log.e(TAG, "No rear1 camera!");
        }

        mCameraindex_front = FindFrontCamera();
        if (mCameraindex_front == -1){
            Log.e(TAG, "No front camera!");
        }
    }

    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        try {
            switch (mCamera_mode) {
                case 0: //rear0 & rear1
                    if(mCamera == null){
                        mCamera = Camera.open(mCameraindex);
                        Log.i(TAG, "open camera :" + mCameraindex);
                    }

                    if(mCamera_sub == null){
                        mCamera_sub = Camera.open(mCameraindex_sub);
                        Log.i(TAG, "open camera :" + mCameraindex_sub);
                    }
                    break;
                case 1: //rear0
                    if(mCamera == null){
                        mCamera = Camera.open(mCameraindex);
                        Log.i(TAG, "open camera:0");
                    }
                    break;
                default:
                    Toast.makeText(MainActivity.this, "unknow camera mode", Toast.LENGTH_LONG).show();
                    break;
            }
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(this, "camera open fail", Toast.LENGTH_LONG).show();
            Log.e(TAG,e.getMessage());
            return;
        }

        //mOrientationListener.enable();
        faceView = (FaceView)findViewById(R.id.face_view);
        //faceDetect = new FaceDetection(this, faceView);
        touchView = (TouchView)findViewById(R.id.touch_view);
        touchView.setVisibility(View.INVISIBLE);

        if(mCamera_mode == 0){
            if(mCamera_sub != null){
                mCameraSurPreview = new CameraPreview(this, mCamera, previewCamera, mCameraindex);
                InitPinnerOther(mCamera); //设置下拉列表
                mCamera.setDisplayOrientation(90);

                mCameraSurPreview_sub = new CameraPreview(this, mCamera_sub, previewCamera_sub, mCameraindex_sub);
                InitPinnerOther(mCamera_sub); //设置下拉列表
                mCamera_sub.setDisplayOrientation(90);
            }
        }else {
            mCameraSurPreview = new CameraPreview(this, mCamera, previewCamera_full, mCameraindex);
            InitPinnerOther(mCamera); //设置下拉列表
            mCamera.setDisplayOrientation(90);

            //mCameraSurPreview_sub = new CameraPreview(this, mCamera_sub, previewCamera_full_sub, mCameraindex_sub);
            //InitPinnerOther(mCamera_sub); //设置下拉列表
            //mCamera_sub.setDisplayOrientation(90);
        }

        //touchView = (TouchView)findViewById(R.id.touch_view);
        //touchView.setVisibility(View.INVISIBLE);
        timerTV = (TextView) this.findViewById(R.id.videoTimer);
        timerTV.setVisibility(View.INVISIBLE);
        //mSurRecorder = new sMediaRecorder(this, previewCamera, timerTV);

        //preview 800ms後,模擬點擊對焦調光,开启脸部识别
        new Handler().postDelayed(new Runnable(){
            public void run() {
                //execute the task
                Camera.Parameters params = mCamera.getParameters();
                Camera.Size previewSize = params.getPreviewSize();
                setMouseClick(previewSize.height/2, previewSize.width/2);
                //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID); //add face detection after preview
            }
        }, 800);
        Log.v(TAG, "onResume finish");
    }

    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if(mOrientationListener != null){
            mOrientationListener.disable();
        }

        //if(mSurRecorder != null && isRecording == true){
        //    mSurRecorder.stopRecording();
        //}

        if (mCamera != null) {
            //faceDetect.stopFaceDetection(mCamera);
            mCamera.stopPreview();
            //mCamera.release();
            //mCamera=null;
        }

        if (mCamera_sub != null) {
            //faceDetect.stopFaceDetection(mCamera);
            mCamera_sub.stopPreview();
            //mCamera.release();
            //mCamera=null;
        }
    }

    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        if(mOrientationListener != null){
            mOrientationListener.disable();
        }
        //if(mSurRecorder != null && isRecording == true){
         //   mSurRecorder.stopRecording();
        //}
        if(mCamera != null){
            //faceDetect.stopFaceDetection(mCamera);
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }

        if(mCamera_sub != null){

            mCamera_sub.stopPreview();
            mCamera_sub.release();
            mCamera_sub=null;
        }

        System.exit(0);
    }


    private int FindFrontCamera() {
        int cameraCount;
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
                Log.e(TAG,"FindBackCamera index =  "+camIdx);
                return camIdx;
            }
        }
        return -1;
    }

    private int FindBackCamera_2() {
        boolean findFirst;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        findFirst=false; //init
        for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                if(findFirst == true){
                    Log.e(TAG,"FindBackCamera_2 index =  "+camIdx);
                    return camIdx;
                }else {
                    findFirst=true;
                }
            }
        }
        return -1;
    }

    //初始化分辨率下拉列表
    public void InitPinnerOther(Camera pCamera){

        Camera.Parameters parameters = pCamera.getParameters();

        //get camera capbility ------
        supportedFlashMode = parameters.getSupportedFlashModes();
        supportedFocuseMode = parameters.getSupportedFocusModes();

        if(supportedFlashMode == null || supportedFlashMode.isEmpty()){
            Log.e(TAG, "supportedFlashMode : Null");
            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
        }else {
            for(int i=0;i<supportedFlashMode.size();i++){
                Log.i(TAG, "supportedFlashMode : "+supportedFlashMode.get(i).toString());
            }
            parameters.setFlashMode(currentFlashMode);
        }

        issupportFocuse=false;
        if(supportedFocuseMode == null || supportedFocuseMode.isEmpty()){
            Log.e(TAG, "supportedFlashMode : Null");
        }else {
            for(int i=0;i<supportedFocuseMode.size();i++){
                Log.i(TAG, "supportedFocuseMode : "+supportedFocuseMode.get(i).toString());
                if(supportedFocuseMode.get(i).equals("auto")){
                    issupportFocuse=true;
                }
            }
        }

        parameters.setPictureFormat(256); //JPEG
        pCamera.setParameters(parameters);
        Log.v(TAG, "InitPinnerOther end.");
    }

    //下拉列表事件监听---------------------------------------------
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            Camera.Parameters parameters = mCamera.getParameters();
            switch (arg0.getId()) {
                case R.id.flashMode:
                    switch (arg2){
                        case 0:
                            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
                            mCamera.setParameters(parameters);
                            currentFlashMode = parameters.FLASH_MODE_OFF;
                            break;
                        case 1:
                            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
                            mCamera.setParameters(parameters);
                            parameters.setFlashMode(parameters.FLASH_MODE_ON);
                            mCamera.setParameters(parameters);
                            currentFlashMode = parameters.FLASH_MODE_ON;
                            break;
                        case 2:
                            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
                            mCamera.setParameters(parameters);
                            parameters.setFlashMode(parameters.FLASH_MODE_AUTO);
                            mCamera.setParameters(parameters);
                            currentFlashMode = parameters.FLASH_MODE_AUTO;
                            break;
                        case 3:
                            parameters.setFlashMode(parameters.FLASH_MODE_TORCH);
                            mCamera.setParameters(parameters);
                            currentFlashMode = parameters.FLASH_MODE_TORCH;
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "didn`t support!", Toast.LENGTH_LONG).show();
                            break;
                    }
                    break;
                case R.id.cameraMode:
                    mCamera_mode = arg2;
                    Log.i(TAG, "mCamera_mode:"+mCamera_mode);
                    if(mCamera_mode == 0 && mCamera_sub == null){
                        //faceDetect.stopFaceDetection(mCamera);
                        mCamera.stopPreview();//释放资源
                        previewCamera_full.setVisibility(View.INVISIBLE);
                        //mCamera.release();//释放资源
                        //mCamera = null;//取消原来摄像头

                        //mCaptureButton.setEnabled(true);
                        //mCaptureButton.setBackgroundColor(Color.TRANSPARENT);

                        new Handler().postDelayed(new Runnable(){
                            public void run() {
                                //execute the task
                                try {
                                    if(mCamera == null){
                                        mCamera = Camera.open(mCameraindex);
                                    }
                                    mCamera_sub = Camera.open(mCameraindex_sub);
                                    mCamera.setDisplayOrientation(90);
                                    mCamera_sub.setDisplayOrientation(90);
                                    //Camera.Parameters parameters_0 = mCamera.getParameters();
                                    //Camera.Parameters parameters_1 = mCamera_sub.getParameters();
                                    //parameters_0.setRotation(180);
                                    //parameters_1.setRotation(180);
                                    //mCamera.setParameters(parameters_0);
                                    //mCamera_sub.setParameters(parameters_1);
                                    previewCamera.setVisibility(View.VISIBLE);
                                    previewCamera_sub.setVisibility(View.VISIBLE);
                                    mCameraSurPreview.setMaxPreviewAndPictureSize(mCamera);
                                    mCameraSurPreview.setMaxPreviewAndPictureSize(mCamera_sub);
                                    mCamera.setPreviewDisplay(previewCamera.getHolder());
                                    mCamera_sub.setPreviewDisplay(previewCamera_sub.getHolder());

                                    mCamera.startPreview();
                                    //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);
                                    mCamera_sub.startPreview();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "setPreviewDisplay error!");
                                }
                            }
                        }, 1500);

                    }else if(mCamera_mode == 1 && mCamera_sub != null){

                        mCamera_sub.stopPreview();

                        //faceDetect.stopFaceDetection(mCamera);
                        mCamera.stopPreview();//释放资源
                        previewCamera_sub.setVisibility(View.INVISIBLE);
                        previewCamera.setVisibility(View.INVISIBLE);
                        mCamera_sub.release();
                        mCamera.release();//释放资源
                        mCamera_sub = null;
                        mCamera = null;//取消原来摄像头

                        //mCaptureButton.setEnabled(true);
                        //mCaptureButton.setBackgroundColor(Color.TRANSPARENT);

                        new Handler().postDelayed(new Runnable(){
                            public void run() {
                                //execute the task
                                previewCamera_full_sub.setVisibility(View.INVISIBLE);
                                previewCamera_full.setVisibility(View.VISIBLE);

                                mCamera = Camera.open(mCameraindex);
                                mCamera.setDisplayOrientation(90);
                                //Camera.Parameters parameters = mCamera.getParameters();
                                //parameters.setRotation(180);
                                //mCamera.setParameters(parameters);
                                mCameraSurPreview.setMaxPreviewAndPictureSize(mCamera);

                                mCamera_sub = Camera.open(mCameraindex_sub);
                                mCamera_sub.setDisplayOrientation(90);
                                mCameraSurPreview_sub.setMaxPreviewAndPictureSize(mCamera_sub);

                                try {
                                    //通过surfaceview显示取景画面
                                    mCamera.setPreviewDisplay(previewCamera_full.getHolder());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    //通过surfaceview显示取景画面
                                    mCamera_sub.setPreviewDisplay(previewCamera_full_sub.getHolder());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                mCamera.startPreview();
                                //mCamera_sub.startPreview();
                                //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);
                            }
                        }, 500);

                    }else {
                        Log.e(TAG, "Do not need change camera mode!");
                    }
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            Log.e(TAG, "onNothingSelected!");
        }
    }

    private String getOutputMediaFile() {

        String picPatch;
        //get the mobile Pictures directory
        mPictureFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //get the current time
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        picPatch = mPictureFile.getPath() + File.separator + "IMG_" + timeStamp;
        Log.e(TAG, "picPatch:" + picPatch);
        return picPatch;
    }
    /* 触发系统的media scanner来把图片加入Media Provider's database */
    private void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);  //设置URI
        this.sendBroadcast(mediaScanIntent);  //发送广播
    }


    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //set the picture save path
            String savePath;
            savePath = getOutputMediaFile();
            if (savePath == null) {
                Log.e(TAG, "Error creating media file, check storage permissions: ");
                Toast.makeText(MainActivity.this,"creat media file fail",Toast.LENGTH_LONG).show();
            }

            /* //so slowly
            try {
                BufferedOutputStream bocameras = null;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.setRotate(90,(float) bitmap.getWidth(), (float) bitmap.getHeight());
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mPictureFile));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                bitmap.recycle();//回收bitmap空间
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
             */

            if(mCameraID == 0)
                savePath = savePath+"_imx362.jpg";
            else if(mCameraID == 1)
                savePath = savePath+"_imx214.jpg";
            else if(mCameraID == 2)
                savePath = savePath+"_3m3.jpg";

            try {
                if (data != null && savePath != null){
                    File rawOutput = new File(savePath);
                    FileOutputStream outStream = new FileOutputStream(rawOutput);
                    outStream.write(data);
                    outStream.close();
                }
            }catch(IOException e){
                Log.e(TAG,e.getMessage());
            }

            mCamera.startPreview(); //开始预览
            //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID); //add face detection after preview
            mThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(savePath), 320, 240);
            //showCameraIv.setImageBitmap(mThumbImage);
            //galleryAddPic(savePath);
            //Toast.makeText(MainActivity.this,picWidth+"x"+picHeight+savePath,Toast.LENGTH_LONG).show();
            //See if need to enable or not
            mCaptureButton.setEnabled(true);
            mCaptureButton.setBackgroundColor(Color.TRANSPARENT);
            //mCaptureButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_shutter_default));
        }
    };

    Camera.PictureCallback mPictureCallback_Sub = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //set the picture save path
            String savePath;
            savePath = getOutputMediaFile();
            if (savePath == null) {
                Log.e(TAG, "Error creating media file, check storage permissions: ");
                Toast.makeText(MainActivity.this,"creat media file fail",Toast.LENGTH_LONG).show();
            }

            savePath = savePath+"_3m3.jpg";

            try {
                if (data != null && savePath != null){
                    File rawOutput = new File(savePath);
                    FileOutputStream outStream = new FileOutputStream(rawOutput);
                    outStream.write(data);
                    outStream.close();
                }
            }catch(IOException e){
                Log.e(TAG,e.getMessage());
            }

            mCamera_sub.startPreview(); //开始预览
            //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID); //add face detection after preview

            mThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(savePath), 320, 240);
            //showCameraIv.setImageBitmap(mThumbImage);
            //galleryAddPic(savePath);
            //Toast.makeText(MainActivity.this,picWidth+"x"+picHeight+savePath,Toast.LENGTH_LONG).show();
            //See if need to enable or not
            mCaptureButton.setEnabled(true);
            mCaptureButton.setBackgroundColor(Color.TRANSPARENT);
            //mCaptureButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_shutter_default));
        }
    };

    class ButtonPart implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Camera.Parameters parameters =null;

            switch (v.getId()) {
                case R.id.button_capture:
                    Log.d(TAG,"Take pic-flash mode:"+mCamera.getParameters().getFlashMode());
                    //mCaptureButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_shutter_pressed));
                    //mCaptureButton.setBackgroundColor(Color.rgb(0x00,0xBF,0xFF)); //深天藍
                    mCaptureButton.setBackgroundColor(Color.WHITE);
                    mCaptureButton.setEnabled(false);
                    mCamera.takePicture(null, null, mPictureCallback);
                    if(mCamera_mode == 0 && mCamera_sub != null){
                        mCamera_sub.takePicture(null, null, mPictureCallback_Sub);
                    }
                    break;
                case R.id.button_switch:
                    mSwitchButton.setEnabled(false); //disable switch button
                    if(mCameraID == 0){
                           mCameraID=1;
                            //mCamera.stopPreview();
                            //mCamera_sub.stopPreview();
                           if(mCamera_mode == 0){
                               mCamera_sub.startPreview();

                               //faceDetect.stopFaceDetection(mCamera);
                               mCamera.stopPreview();
                           }else {
                                //faceDetect.stopFaceDetection(mCamera);
                                mCamera.stopPreview();
                                mCamera.release();
                                mCamera=null;

                                mCamera = Camera.open(mCameraindex_front);
                                mCamera.setDisplayOrientation(90);
                                //parameters = mCamera.getParameters();
                                //parameters.setRotation(270);
                                //mCamera.setParameters(parameters);

                                try {
                                    mCamera.setPreviewDisplay(previewCamera_full.getHolder());//通过surfaceview显示取景画面
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.e(TAG, "setPreviewDisplay_full error!");
                                }
                               mCamera.startPreview();
                               //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);
                           }
                    }else if(mCameraID == 1){
                        if(mCamera_mode == 0){
                            mCamera.startPreview();
                            //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);
                            mCamera_sub.stopPreview();
                            mCameraID=0;
                        }else {
                            mCameraID=2;
                            //faceDetect.stopFaceDetection(mCamera);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera=null;

                            mCamera = Camera.open(mCameraindex_sub);
                            mCamera.setDisplayOrientation(90);
                            //parameters = mCamera.getParameters();
                            //parameters.setRotation(90);
                            //mCamera.setParameters(parameters);
                            try {
                                mCamera.setPreviewDisplay(previewCamera_full.getHolder());//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "setPreviewDisplay_sub error!");
                            }
                            mCamera.startPreview();
                            //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);
                        }
                    }else if(mCameraID == 2){
                        mCameraID=0;
                        //faceDetect.stopFaceDetection(mCamera);
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera=null;

                        mCamera = Camera.open(mCameraindex);
                        mCamera.setDisplayOrientation(90);
                        //parameters = mCamera.getParameters();
                        //parameters.setRotation(90);
                        //mCamera.setParameters(parameters);
                        try {
                            mCamera.setPreviewDisplay(previewCamera_full.getHolder());//通过surfaceview显示取景画面
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "setPreviewDisplay error!");
                        }
                        mCamera.startPreview();
                        //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);
                    }
                    mSwitchButton.setEnabled(true); //enable switch button
                    break;
                case R.id.button_video:
                    if(isRecording == false){
                        mSwitchButton.setVisibility(View.INVISIBLE);
                        mVideoButton.setBackgroundColor(Color.RED);
                        //faceDetect.stopFaceDetection(mCamera);//stop face detection
                        //spinner_res.setVisibility(View.INVISIBLE);
                        spinner_flash.setVisibility(View.INVISIBLE);
                        //mSurRecorder.startRecording(mCamera, mCameraID);
                        isRecording = true;
                    }
                    else{
                        mVideoButton.setBackgroundColor(Color.TRANSPARENT);
                        //spinner_res.setVisibility(View.VISIBLE);
                        spinner_flash.setVisibility(View.VISIBLE);
                        //mSurRecorder.stopRecording();
                        //faceDetect.stopFaceDetection(mCamera);
                        mCamera.stopPreview();
                        mCamera.startPreview();
                        //faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);//restart face detection
                        mSwitchButton.setVisibility(View.VISIBLE);

                        isRecording = false;
                    }
                    break;
                default:
                    Toast.makeText(MainActivity.this, "camera open fail", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public class MyOrientationDetector extends OrientationEventListener {
        public MyOrientationDetector(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            Log.i("MyOrientationDetector ", "onOrientationChanged:" + orientation);
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;  //手机平放时，检测不到有效的角度
            }
            //只检测是否有四个角度的改变
            if (orientation > 315 || orientation <= 45) { //0度
                mOrientation = 0;
                mSwitchButton.setRotation(0);
            } else if (orientation > 45 && orientation <= 135) { //90度
                mOrientation = 90;
                mSwitchButton.setRotation(270);
            } else if (orientation > 135 && orientation <= 225) { //180度
                mOrientation = 180;
                mSwitchButton.setRotation(180);
            } else if (orientation > 225 && orientation <= 315) { //270度
                mOrientation = 270;
                mSwitchButton.setRotation(90);
            } else {
                return;
            }
        }
    }

    //simulateClick:模擬屏幕點擊開camera時自動對焦和調光,作用於Activity
    public void setMouseClick(int x, int y){
        Log.e(TAG, "setMouseClick : "+x+","+y);
        MotionEvent evenDownt = MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis() + 100,
                MotionEvent.ACTION_DOWN, x, y, 0);
        dispatchTouchEvent(evenDownt);
        MotionEvent eventUp = MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis() + 100,
                MotionEvent.ACTION_UP, x, y, 0);
        dispatchTouchEvent(eventUp);
        evenDownt.recycle();
        eventUp.recycle();
    }

    //focuse and metering handle
    private void handleFocusMetering(MotionEvent event, Camera camera) {
        String TAG_TC = "SheldonTC";
        Camera.Parameters params = camera.getParameters(); //获得参数设置
        Camera.Size previewSize = params.getPreviewSize();

        //触摸测光----------------------------------
        Rect meteringRect = calculateTapArea(event.getRawX(), event.getRawY(), 1.5f, previewSize);
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 400));
            params.setMeteringAreas(meteringAreas);
            camera.setParameters(params);
        } else {
            Log.e(TAG_TC, "metering areas not supported");
        }

        //触摸对焦----------------------------------
        if(issupportFocuse){
            camera.cancelAutoFocus();   //去掉对焦完成后的回调函数
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            final Rect focusRect = calculateTapArea(event.getRawX(), event.getRawY(), 1f, previewSize);

            //touchView.setVisibility(View.VISIBLE);
            //touchView.setFocus(focusRect, focuseDone, needMirror);

            /*
            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 800));
                params.setFocusAreas(focusAreas);
            } else {
                Log.e(TAG_TC,"focus areas not supported");
            }

            camera.setParameters(params);
            Log.d(TAG_TC,"Default focus mode:"+currentFocusMode);
            */
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG,"onAutoFocus : "+success);
                        focuseDone = success;
                        //touchView.setFocus(focusRect, focuseDone, needMirror);
                        //mHandler.sendEmptyMessageDelayed(HandleMsg.MSG_START_PREVIEW, 1000);
                        //touchView.setVisibility(View.INVISIBLE);
                        focuseDone = false;
                    Camera.Parameters params = camera.getParameters();
                    params.setFocusMode(currentFocusMode);
                    camera.setParameters(params);
                }
            });
        }
    }

    //触摸区域范围限定及计算----------------------------------
    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {

        float AreaSize = 300;
        int areaSize = Float.valueOf(AreaSize * coefficient).intValue();
        //ps:x-previewSize.height, y-previewSize.width
        int centerX = (int)((x / previewSize.height) * 2000 - 1000);
        int centerY = (int)((y / previewSize.width) * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(centerX + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(centerY + areaSize, -1000, 1000);

        RectF mTapRectF = new RectF(left, top, right, bottom);
        return new Rect(Math.round(mTapRectF.left), Math.round(mTapRectF.top),
                        Math.round(mTapRectF.right), Math.round(mTapRectF.bottom));
    }

    //缩放功能----------------------------------
    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);

            Log.i(TAG, "maxZoom:"+maxZoom+", zoom="+zoom);

            if(mCamera_mode == 1){
                if(zoom > 45 && zoom < 55 && doZoom){
                    Log.i(TAG, "preview:2");
                    //mCamera.stopPreview();
                    //previewCamera_full.setVisibility(View.INVISIBLE);
                    //mCamera_sub.startPreview();
                    //previewCamera_full_sub.setVisibility(View.VISIBLE);

                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera=null;
                    mCamera_sub = Camera.open(mCameraindex_sub);
                    mCamera_sub.setDisplayOrientation(90);

                    try {
                        mCamera_sub.setPreviewDisplay(previewCamera_full.getHolder());//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "setPreviewDisplay_sub error!");
                    }
                    mCamera_sub.startPreview();


                    doZoom=false;
                    doZoom_sub=true;
                    //mCamera.startPreview();
                }else if(zoom > 35 && zoom < 45 && doZoom_sub){
                    Log.i(TAG, "preview:0");
                    //mCamera.startPreview();
                    //previewCamera_full.setVisibility(View.VISIBLE);
                    //previewCamera_full_sub.setVisibility(View.INVISIBLE);

                    mCamera_sub.stopPreview();
                    mCamera_sub.release();
                    mCamera_sub=null;
                    mCamera = Camera.open(mCameraindex);
                    mCamera.setDisplayOrientation(90);

                    try {
                        mCamera.setPreviewDisplay(previewCamera_full.getHolder());//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "setPreviewDisplay_sub error!");
                    }
                    mCamera.startPreview();


                    //mCamera_sub.stopPreview();
                    doZoom_sub=false;
                    doZoom=true;
                }
            }
        } else {
            Log.e(TAG, "zoom not supported");
        }
    }

    //获取触摸事件----------------------------------
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "event.getPointerCount() = "+event.getPointerCount());
        if (event.getPointerCount() == 1) {
            if(mCamera != null){
                handleFocusMetering(event, mCamera);
            }
            if(mCamera_sub != null){
                handleFocusMetering(event, mCamera_sub);
            }
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    Log.i(TAG, "oldDist:"+oldDist);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if(doZoom){
                        handleZoom(newDist > oldDist, mCamera);
                    }else {
                        handleZoom(newDist > oldDist, mCamera_sub);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }
}

