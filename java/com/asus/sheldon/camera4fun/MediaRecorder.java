package com.asus.sheldon.camera4fun;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sheldon on 16-5-26.
 */
class sMediaRecorder implements SurfaceHolder.Callback{
    public void startRecoder(Camera camera){
        String TAG="MediaRecorder:";
        SurfaceHolder mMediaHolder=null;
        android.media.MediaRecorder mMediaRecorder = null;
        File mRecVedioPath;
        File mRecAudioFile = null;
        TextView timer = null;
        boolean isRecording=false;
        int hour = 0;
        int minute = 0;
        int second = 0;

        Camera.Parameters params = camera.getParameters();

        if (isRecording) {

            //if (isPreview) {
                camera.stopPreview();
                camera.release();
                camera = null;
            //}
            second = 0;
            minute = 0;
            hour = 0;
            //bool = true;
            if (mMediaRecorder == null)
                mMediaRecorder = new android.media.MediaRecorder();
            else
                mMediaRecorder.reset();

            // 设置缓存路径
            mRecVedioPath = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/video/temp/");
            if (!mRecVedioPath.exists()) {
                mRecVedioPath.mkdirs();
            }

            android.media.MediaRecorder mHolder;
            mMediaRecorder.setPreviewDisplay(mMediaHolder.getSurface());
            mMediaRecorder.setVideoSource(android.media.MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoSize(320, 240);
            mMediaRecorder.setVideoFrameRate(15);
            try {
                mRecAudioFile = File.createTempFile("Vedio", ".3gp", mRecVedioPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
            try {
                mMediaRecorder.prepare();
                timer.setVisibility(View.VISIBLE);
                //handler.postDelayed(task, 1000);
                mMediaRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "开始录制");
            //mVideoStartBtn.setBackgroundDrawable(iconStop);
            isRecording = !isRecording;
        } else {
                    /*
                     * 点击停止
                     */
            try {
                isRecording = false;
                mMediaRecorder.stop();
                timer.setText(format(hour) + ":" + format(minute) + ":"
                        + format(second));
                mMediaRecorder.release();
                mMediaRecorder = null;
                //videoRename();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isRecording = !isRecording;
            //mVideoStartBtn.setBackgroundDrawable(iconStart);
            //showMsg("录制完成，已保存");

            try {
                camera = Camera.open();
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewFrameRate(5); // 每秒5帧
                parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
                parameters.set("jpeg-quality", 85);// 照片质量
                camera.setParameters(parameters);
                camera.setPreviewDisplay(mMediaHolder);
                camera.startPreview();
                //isPreview = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*生成video文件名字*/
    /*
    protected void videoRename() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/";
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()) + ".3gp";
        File out = new File(path);
        if (!out.exists()) {
            out.mkdirs();
        }
        out = new File(path, fileName);
        if (mRecAudioFile.exists())
            mRecAudioFile.renameTo(out);
    }*/

    /*定时器设置,实现计时*/
    /*
    public Handler handler = new Handler();
    public Runnable task = new Runnable() {
        public void run() {
            if (isRecording) {
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
                timer.setText(format(hour) + ":" + format(minute) + ":"
                        + format(second));
            }
        }
    };*/

    /*格式化时间*/
    public String format(int i) {
        String s = i + "";
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
