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
import android.widget.TextView;

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
    private TextView timer = null;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;


    public sMediaRecorder(Context context,SurfaceView sv) {
        super(context);
        Log.i(TAG, "create sMediaRecorder:");
        mContext = context;
        mMediaHolder= sv.getHolder();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        if(mMediaHolder != null){

            mMediaHolder.addCallback(this); //添加回调
            // deprecated setting, but required on Android versions prior to 3.0
            mMediaHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            timer = (TextView) findViewById(R.id.timer);

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
            mMediaHolder=null;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder=null;
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
                //timer.setText(format(hour)+":"+format(minute)+":"+format(second));
                Log.d(TAG, "timer-"+format(hour)+":"+format(minute)+":"+format(second));
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

    public void startRecording(Camera vCamera) {
        Log.i(TAG, "startRecording:");

        vCamera.unlock(); //让media程序存取到相機

        second = 0;
        minute = 0;
        hour = 0;

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        else
            mMediaRecorder.reset();

        mMediaRecorder.setCamera(vCamera);
        //init recorder parameter
        mMediaRecorder.setVideoSource(android.media.MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
        //mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoSize(320, 240);
        mMediaRecorder.setVideoFrameRate(15);
        mMediaRecorder.setPreviewDisplay(mMediaHolder.getSurface());
        //mMediaRecorder.setOrientationHint(90);

        //设置缓存路径
        mRecVedioPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/temp/");
        Log.d(TAG, "set recorder path:"
                + Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/temp/");
        if (!mRecVedioPath.exists()) {
            mRecVedioPath.mkdirs();
        }

        try {
            mRecAudioFile = File.createTempFile("Vedio", ".3gp", mRecVedioPath);
        } catch (IOException e) {
            Log.e(TAG, "createTempFile error!");
            e.printStackTrace();
        }

        mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());

        try {
            //timer.setVisibility(VISIBLE);
            handler.postDelayed(task, 1000);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            Log.e(TAG, "start recorder error！");
            e.printStackTrace();
        }
    }

    public void stopRecording(Camera vCamera) {
        Log.i(TAG, "startRecording:");

        try {
                if(mMediaRecorder != null){
                    handler.removeCallbacks(task);
                    //timer.setVisibility(INVISIBLE);
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "stop recorder error！");
                e.printStackTrace();
            }

            //showMsg("录制完成，已保存");
        }
}
