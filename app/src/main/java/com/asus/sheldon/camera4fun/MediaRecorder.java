package com.asus.sheldon.camera4fun;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by sheldon on 16-5-26.
 */
class sMediaRecorder extends SurfaceView implements SurfaceHolder.Callback{

    String TAG="MediaRecorder:";

    private Context mContext=null;
    private SurfaceHolder mMediaHolder=null;
    private MediaRecorder mMediaRecorder = null;
    private File mRecVedioPath=null;
    private File mRecAudioFile=null;
    private TextView vtimer = null;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;


    public sMediaRecorder(Context context,SurfaceView sv, TextView tv) {
        super(context);
        Log.i(TAG, "create sMediaRecorder:");
        mContext = context;
        mMediaHolder= sv.getHolder();

        vtimer = tv;
        if(vtimer == null){
            Log.e(TAG, "vtimer is null!");
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        if(mMediaHolder != null){

            mMediaHolder.addCallback(this); //添加回调
            // deprecated setting, but required on Android versions prior to 3.0
            mMediaHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            /*
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            wm.addView(sv, params);
            */
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "recorder surfaceCreated:");
        SurfaceHolder surfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "recorder surfaceChanged:");
        SurfaceHolder surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "recorder surfaceDestroyed:");
        if(mMediaRecorder != null){
            //mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder=null;
            mMediaHolder=null;
        }
    }

    /*定时器设置,实现计时*/
    public Handler handler = new Handler();
    public Runnable task = new Runnable() {
        public void run() {
                handler.postDelayed(this, 1000);
                second++;
                if (second >= 60) {
                    minute++;
                    second = second % 60;
                }
                if (minute >= 60) {
                    hour++;
                    minute = minute % 60;
                }
                vtimer.setText(format(hour)+":"+format(minute)+":"+format(second));
                //Log.d(TAG, "timer-"+format(hour)+":"+format(minute)+":"+format(second));
        }
    };

    /*格式化时间*/
    public String format(int i) {
        String s = i + "";
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    public void startRecording(Camera vCamera, int vCameraId) {
        Log.i(TAG, "start Recording:");

        vCamera.unlock(); //让media程序存取到相機

        //second = 0;
        //minute = 0;
        //hour = 0;
        vtimer.setText(format(hour=0)+":"+format(minute=0)+":"+format(second=0));

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        else
            mMediaRecorder.reset();

        mMediaRecorder.setCamera(vCamera);
        //init recorder parameter
        mMediaRecorder.setVideoSource(android.media.MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB);
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        mMediaRecorder.setVideoSize(1920,1080);
        mMediaRecorder.setVideoEncodingBitRate(100*1024*1024); //设置帧率调节清晰度
        mMediaRecorder.setVideoFrameRate(20);
        mMediaRecorder.setPreviewDisplay(mMediaHolder.getSurface());


        //设置缓存路径
        mRecVedioPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/temp/");
        Log.d(TAG, "set recorder path:"
                + Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/temp/");
        if (!mRecVedioPath.exists()) {
            mRecVedioPath.mkdirs();
        }

        try {
            mRecAudioFile = File.createTempFile("Vedio", ".mp4", mRecVedioPath);
        } catch (IOException e) {
            Log.e(TAG, "createTempFile error!");
            e.printStackTrace();
        }

        mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
        if(vCameraId == 1){
            mMediaRecorder.setOrientationHint(180);
        }


        try {
            vtimer.setVisibility(VISIBLE);
            handler.postDelayed(task, 1000);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            Log.e(TAG, "start recorder error！");
            e.printStackTrace();
        }
    }

    public void stopRecording(Camera vCamera) {
        Log.i(TAG, "stop Recording:");

        try {
                vtimer.setVisibility(INVISIBLE);
                if(mMediaRecorder != null){
                    handler.removeCallbacks(task);
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "stop recorder error！");
                e.printStackTrace();
            }

           // Toast.makeText(this, "", Toast.LENGTH_LONG).show();
        }
}
