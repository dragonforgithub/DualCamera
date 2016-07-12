package com.asus.sheldon.camera4fun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    SurfaceView previewCamera=null;
    FaceView faceView=null;
    TouchView touchView=null;
    TextView timerTV=null;
    private Camera mCamera;
    private CameraPreview mCameraSurPreview = null;
    private sMediaRecorder mSurRecorder = null;
    private Button mCaptureButton = null;
    private Button mSwitchButton = null;
    private Button mVideoButton = null;
    private String TAG = "Sheldon";
    private int mCameraID = 0;
    private int mCameraindex = 0;
    private boolean isRecording=false;
    private static boolean issupportFocuse=false;

    private int picWidth;
    private int picHeight;

    private List<Camera.Size> supportedPreviewSizes;
    private List<Camera.Size> supportedPictureSizes;
    private List<String> supportedFlashMode;
    private List<String> supportedFocuseMode;
    private List<String> list_resolution=new ArrayList<String>();

    private Spinner spinner_res;
    private Spinner spinner_flash;

    private static String currentFocusMode="auto";
    private static String currentFlashMode="auto";
    private float oldDist = 1f;

    //view detection
    public FaceDetection faceDetect;
    private boolean focuseDone=false;
    private boolean needMirror=false;

    public Handler mHandler;

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
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        try {
            Log.d(TAG, "mCamera open id=" + 0);

            if (mCameraID == 0) {
                mCameraindex = FindBackCamera();
                if (mCameraindex == -1)
                    return;
            } else if (mCameraID == 1) {
                mCameraindex = FindFrontCamera();
                if (mCameraindex == -1)
                    return;
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG,e.getMessage());
            Toast.makeText(this, "camera open fail", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.

        if(mSurRecorder != null){
            mSurRecorder.stopRecording();
        }

        if (mCamera != null) {
            faceDetect.stopFaceDetection(mCamera);
            faceDetect=null;
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.lock();
            mCamera.release();
            mCamera=null;
        }
    }

    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        if(mCamera == null){
            mCamera = Camera.open(mCameraindex);
            if(mCamera == null) {
                Log.e(TAG, "error : open camera " + mCameraindex);
                return;
            }
        }

        // Create Preview and video recorder
        previewCamera = (SurfaceView) this.findViewById(R.id.preView);
        faceView = (FaceView)findViewById(R.id.face_view);
        faceDetect = new FaceDetection(this, faceView);
        touchView = (TouchView)findViewById(R.id.touch_view);
        touchView.setVisibility(View.INVISIBLE);
        mCameraSurPreview = new CameraPreview(this, mCamera, previewCamera, mCameraindex);
        timerTV = (TextView) this.findViewById(R.id.videoTimer);
        timerTV.setVisibility(View.INVISIBLE);
        mSurRecorder = new sMediaRecorder(this, previewCamera, timerTV);

        Camera.Parameters parameters = mCamera.getParameters();
        currentFocusMode = parameters.getFocusMode();//保存默认对焦模式

        InitPinnerOther(mCamera); //设置下拉列表

        picWidth = Integer.parseInt((String.valueOf(supportedPictureSizes.get(0).width)));
        picHeight = Integer.parseInt((String.valueOf(supportedPictureSizes.get(0).height)));
        parameters.setPictureSize(picWidth, picHeight);
        parameters.setPreviewSize(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);
        parameters.setRotation(90); //default picture rotation
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);

        mCamera.startPreview();
        faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID); //add face detection after preview

        Log.d(TAG, "camera open finish");

        // Add a listener to the Capture button
        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new ButtonPart());

        // Add a listener to the Switch button
        mSwitchButton = (Button) findViewById(R.id.button_switch);
        mSwitchButton.setOnClickListener(new ButtonPart());

        // Add a listener to the video button
        mVideoButton = (Button) findViewById(R.id.button_video);
        mVideoButton.setOnClickListener(new ButtonPart());

        Log.v(TAG, "onResume finish");
    }

    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.v(TAG, "onDestroy");
        super.onDestroy();
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
                return camIdx;
            }
        }
        return -1;
    }

    //初始化分辨率下拉列表
    public void InitPinnerOther(Camera pCamera){

        ArrayAdapter<String> adapter_res;
        Camera.Parameters parameters = pCamera.getParameters();

        //get current preview size list
        supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        //get current picture size list
        supportedPictureSizes = parameters.getSupportedPictureSizes();

        //get camera capbility ------
        supportedFlashMode = parameters.getSupportedFlashModes();
        supportedFocuseMode = parameters.getSupportedFocusModes();

        if(supportedFlashMode == null || supportedFlashMode.isEmpty()){
            Log.e(TAG, "supportedFlashMode : Null");
            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
        }else {
            for(int i=0;i<supportedFlashMode.size();i++){
                Log.e(TAG, "supportedFlashMode : "+supportedFlashMode.get(i).toString());
            }
            parameters.setFlashMode(currentFlashMode);
        }

        issupportFocuse=false;
        if(supportedFocuseMode == null || supportedFocuseMode.isEmpty()){
            Log.e(TAG, "supportedFlashMode : Null");
        }else {
            for(int i=0;i<supportedFocuseMode.size();i++){
                Log.e(TAG, "supportedFocuseMode : "+supportedFocuseMode.get(i).toString());
                if(supportedFocuseMode.get(i).equals("auto")){
                    issupportFocuse=true;
                    parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
                }
            }
        }

        parameters.setPictureFormat(256); //JPEG
        pCamera.setParameters(parameters);

        list_resolution.clear();
        for(int i=0;i<supportedPictureSizes.size();i++){
            list_resolution.add(String.valueOf(supportedPictureSizes.get(i).width)+"*"+
                    String.valueOf(supportedPictureSizes.get(i).height));
        }

        //设置下拉列表的风格
        //resolution
        adapter_res=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,list_resolution);
        adapter_res.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter 添加到spinner中
        spinner_res=(Spinner)findViewById(R.id.resolution);
        spinner_res.setAdapter(adapter_res);
        //設置背景顏色
        //spinner_res.setBackgroundColor(Color.parseColor("#111111"));
        //添加事件Spinner事件监听
        spinner_res.setOnItemSelectedListener(new SpinnerSelectedListener());

        //flash
        spinner_flash=(Spinner)findViewById(R.id.flashMode);
        spinner_flash.setOnItemSelectedListener(new SpinnerSelectedListener());

        Log.v(TAG, "InitPinnerOther end.");
    }

    //下拉列表事件监听---------------------------------------------
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            Camera.Parameters parameters = mCamera.getParameters();
            switch (arg0.getId()) {
                case R.id.resolution:
                    if(isRecording == false){
                        picWidth=Integer.parseInt((String.valueOf(supportedPictureSizes.get(arg2).width)));
                        picHeight=Integer.parseInt((String.valueOf(supportedPictureSizes.get(arg2).height)));
                        parameters.setPictureSize(picWidth, picHeight);
                        Log.i(TAG, "pic:"+arg2+
                                "set Pic_width:"+picWidth+
                                "set Pic_height:"+picHeight);
                    }
                    break;
                case R.id.flashMode:
                    switch (arg2){
                        case 0:
                            parameters.setFlashMode(parameters.FLASH_MODE_OFF);
                            mCamera.setParameters(parameters);
                            parameters.setFlashMode(parameters.FLASH_MODE_AUTO);
                            mCamera.setParameters(parameters);
                            currentFlashMode = parameters.FLASH_MODE_AUTO;
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
                            currentFlashMode = parameters.FLASH_MODE_OFF;
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
                default:
                    Toast.makeText(MainActivity.this, "select error!", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            Log.e(TAG, "onNothingSelected!");
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

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //save the picture to sdcard
            String pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            /*
            try {
                BufferedOutputStream bocameras = null;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.setRotate(90,(float) bitmap.getWidth(), (float) bitmap.getHeight());
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
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
            try {
                if (data != null && pictureFile != null){
                    File rawOutput = new File(pictureFile);
                    FileOutputStream outStream = new FileOutputStream(rawOutput);
                    outStream.write(data);
                    outStream.close();
                }
            }catch(IOException e){
                Log.e(TAG,e.getMessage());
            }

            Toast.makeText(MainActivity.this,picWidth+"x"+picHeight+pictureFile,Toast.LENGTH_LONG).show();
            //See if need to enable or not
            mCaptureButton.setEnabled(true);
            mCamera.startPreview(); //开始预览
            faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID); //add face detection after preview
        }
    };

    class ButtonPart implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Camera.Parameters parameters =null;

            switch (v.getId()) {
                case R.id.button_capture:
                    Log.d(TAG,"Take pic-flash mode:"+mCamera.getParameters().getFlashMode());
                    mCaptureButton.setEnabled(false);
                    mCamera.takePicture(null, null, mPictureCallback);
                    break;
                case R.id.button_switch:
                    if(mCamera != null){
                        faceDetect.stopFaceDetection(mCamera);
                        mCamera.stopPreview();//停掉原来摄像头的预览
                        mCamera.release();//释放资源
                        mCamera = null;//取消原来摄像头
                    }

                    if(mCameraID == 0){
                            mCameraindex = FindFrontCamera();
                            if (mCameraindex == -1){
                                Toast.makeText(MainActivity.this, "No front camera!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            mCamera = Camera.open(mCameraindex);
                            mCamera.setDisplayOrientation(90);
                            parameters = mCamera.getParameters();
                            parameters.setRotation(270);
                            mCamera.setParameters(parameters);

                            try {
                                mCamera.setPreviewDisplay(previewCamera.getHolder());//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "setPreviewDisplay error!");
                            }
                            mCameraID = 1;

                    }else if(mCameraID == 1){
                            mCameraindex = FindBackCamera();
                            if (mCameraindex == -1){
                                Toast.makeText(MainActivity.this, "No rear camera!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            mCamera = Camera.open(mCameraindex);
                            mCamera.setDisplayOrientation(90);
                            parameters = mCamera.getParameters();
                            parameters.setRotation(90);
                            mCamera.setParameters(parameters);
                            try {
                                mCamera.setPreviewDisplay(previewCamera.getHolder());//通过surfaceview显示取景画面
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "setPreviewDisplay error!");
                            }
                            mCameraID = 0;
                    }
                    if(mCamera != null){
                        Log.d(TAG, "Switch to "+mCameraindex);
                        InitPinnerOther(mCamera);
                        mCamera.startPreview(); //开始预览
                        faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID); //add face detection after preview
                    }
                    break;
                case R.id.button_video:
                    if(isRecording == false){
                        faceDetect.stopFaceDetection(mCamera);//stop face detection
                        spinner_res.setVisibility(View.INVISIBLE);
                        spinner_flash.setVisibility(View.INVISIBLE);
                        //mCaptureButton.setVisibility(View.INVISIBLE);
                        mSwitchButton.setVisibility(View.INVISIBLE);
                        mVideoButton.setText("停止");
                        mSurRecorder.startRecording(mCamera, mCameraID);
                        isRecording = true;
                    }
                    else{
                        spinner_res.setVisibility(View.VISIBLE);
                        spinner_flash.setVisibility(View.VISIBLE);
                        //mCaptureButton.setVisibility(View.VISIBLE);
                        mSwitchButton.setVisibility(View.VISIBLE);
                        mVideoButton.setText("录影");
                        mSurRecorder.stopRecording();

                        mCamera.stopPreview();
                        mCamera.startPreview();
                        faceDetect.startFaceDetection(mCamera, faceDetect, mCameraID);//restart face detection

                        isRecording = false;
                    }
                    break;
                default:
                    Toast.makeText(MainActivity.this, "camera open fail", Toast.LENGTH_LONG).show();
                    break;
            }
        }
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

            touchView.setVisibility(View.VISIBLE);
            touchView.setFocus(focusRect, focuseDone, needMirror);

            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusRect, 800));
                params.setFocusAreas(focusAreas);
            } else {
                Log.e(TAG_TC,"focus areas not supported");
            }

            camera.setParameters(params);
            Log.d(TAG_TC,"Default focus mode:"+currentFocusMode);

            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG,"onAutoFocus : "+success);
                        focuseDone = success;
                        touchView.setFocus(focusRect, focuseDone, needMirror);
                        //mHandler.sendEmptyMessageDelayed(HandleMsg.MSG_START_PREVIEW, 1000);
                        touchView.setVisibility(View.INVISIBLE);
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
        } else {
            Log.e(TAG, "zoom not supported");
        }
    }

    //获取触摸事件----------------------------------
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocusMetering(event, mCamera);
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    Log.i(TAG, "oldDist:"+oldDist);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }
}

