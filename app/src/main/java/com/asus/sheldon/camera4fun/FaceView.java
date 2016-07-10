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
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by sheldon on 16-7-1.
 */
class FaceView extends ImageView {
    //ImageView:这个类继承ImageView，用来将Face[] 数据的rect取出来，变换后刷新到UI上
    private static final String TAG = "sheldon";
    private Paint mLinePaint;
    private Camera.Face[] mFaces;
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();
    private Drawable mFaceIndicator = null;
    private int mCamID=0;

    public FaceView(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub
        super(context, attrs);
        mFaceIndicator = getResources().getDrawable(R.drawable.ic_face_find_1);
        initPaint();
    }


    public void setFaces(Camera.Face[] faces , int CI){
        mCamID = CI;
        mFaces = faces;
        invalidate();
    }

    public void clearFaces(){
        mFaces = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        //super.onDraw(canvas);
        if(mFaces == null || mFaces.length < 1){
            return;
        }

        boolean isMirror = false;
        if(mCamID == Camera.CameraInfo.CAMERA_FACING_BACK){
            isMirror = false; //后置Camera无需mirror
            Log.e("FACE","isMirror: false");
        }else if(mCamID == Camera.CameraInfo.CAMERA_FACING_FRONT){
            isMirror = true;  //前置Camera需要mirror
            Log.e("FACE","isMirror: true");
        }

        Util.prepareMatrix(mMatrix, isMirror, 90, getWidth(), getHeight());
        canvas.save();
        mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
        canvas.rotate(-0);     //Canvas.rotate()默认是逆时针
        for(int i = 0; i< mFaces.length; i++){

            mRect.set(mFaces[i].rect);
            mMatrix.mapRect(mRect);
            /*
            Log.e("FACE","num:"+mFaces.length+
                    " = "+Math.round(mRect.left)+
                    " - "+Math.round(mRect.top)+
                    " - "+Math.round(mRect.right)+
                    " - "+Math.round(mRect.bottom));
            */
            mFaceIndicator.setBounds(Math.round(mRect.left),
                                     Math.round(mRect.top),
                                     Math.round(mRect.right),
                                     Math.round(mRect.bottom));

            mFaceIndicator.draw(canvas);

            //canvas.drawRect(mRect, mLinePaint);
        }
        canvas.restore();
        super.onDraw(canvas);
    }

    private void initPaint(){
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mLinePaint.setColor(Color.RED);
		//int color = Color.rgb(0, 150, 255);
        int color = Color.rgb(98, 212, 68);

        mLinePaint.setColor(color);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5f);
        mLinePaint.setAlpha(180);
    }
}


 //Util:最终是通过mMatrix.mapRect(mRect);来将mRect变换成UI坐标系的人脸Rect.
 class Util {
     public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                                      int viewWidth, int viewHeight) {
         // Need mirror for front camera.
         matrix.setScale(mirror ? -1 : 1, 1);
         // This is the value for android.hardware.Camera.setDisplayOrientation.
         matrix.postRotate(displayOrientation);
         // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
         // UI coordinates range from (0, 0) to (width, height).
         matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
         matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
     }
 }