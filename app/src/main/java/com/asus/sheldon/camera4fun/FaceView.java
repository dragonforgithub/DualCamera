package com.asus.sheldon.camera4fun;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
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
    private Drawable mLeftEye = null;
    private Drawable mRightEye = null;
    private Drawable mMouth = null;
    private int mCamID=0;
    private int mSpecificNo=0;
    private Point leftEye;
    private Point rightEye;
    private Point mouth;

    public FaceView(Context context, AttributeSet attrs) {
        // TODO Auto-generated constructor stub
        super(context, attrs);
        mFaceIndicator = getResources().getDrawable(R.drawable.ic_face_find_1);
        mLeftEye =  getResources().getDrawable(R.drawable.fire);
        mRightEye =  getResources().getDrawable(R.drawable.fire);
        mMouth =  getResources().getDrawable(R.drawable.video1);
        initPaint();
    }

    public void setFaces(Camera.Face[] faces , int CI ,int specificNo){
        mCamID = CI;
        mFaces = faces;
        mSpecificNo = specificNo;
        invalidate();
    }

    public void clearFaces(){
        mFaces = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if(mFaces == null || mFaces.length < 1){
            return;
        }

        boolean isMirror = false;
        if(mCamID == Camera.CameraInfo.CAMERA_FACING_BACK){
            isMirror = false; //后置Camera无需mirror
            Log.i("FACE","isMirror: false");
        }else if(mCamID == Camera.CameraInfo.CAMERA_FACING_FRONT){
            isMirror = true;  //前置Camera需要mirror
            Log.i("FACE","isMirror: true");
        }

        int preview_Width=getWidth();
        int preview_Height=getHeight();
        int centerX=0;
        int centerY=0;
        int ShapeSize=0;


        Util.prepareMatrix(mMatrix, isMirror, 90, preview_Width, preview_Height);
        canvas.save();
        //mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
        //canvas.rotate(-0);     //Canvas.rotate()默认是逆时针
        for(int i = 0; i< mFaces.length; i++){
            Log.i("FACE","getWidth()= "+preview_Width+",getHeight()= "+preview_Height);
            /*
            Log.e("FACE","num:"+mFaces.length+
                    " = "+Math.round(mRect.left)+
                    " - "+Math.round(mRect.top)+
                    " - "+Math.round(mRect.right)+
                    " - "+Math.round(mRect.bottom));
            */
            mRect.set(mFaces[i].rect);
            mMatrix.mapRect(mRect);
            mFaceIndicator.setBounds(Math.round(mRect.left),
                                     Math.round(mRect.top),
                                     Math.round(mRect.right),
                                     Math.round(mRect.bottom));

            mFaceIndicator.draw(canvas);
            //canvas.drawRect(mRect, mLinePaint);

            //if specific on,then display
            if(mSpecificNo > 0){
                leftEye = mFaces[i].leftEye;
                rightEye = mFaces[i].rightEye;
                mouth = mFaces[i].mouth;
                /*
                Log.e("FACE","num:"+mFaces.length+
                        " leftEye:"+leftEye.x+"-"+leftEye.y+
                        " rightEye:"+rightEye.x+"-"+rightEye.y+
                        " mouth:"+mouth.x+"-"+mouth.y);
                 */
                if(mCamID == 1){ //front camera mirror
                    Log.e("FACE","front camera mirror:");
                    leftEye.x = 0-leftEye.x;
                    leftEye.y = 0-leftEye.y;
                    rightEye.x = 0-rightEye.x;
                    rightEye.y = 0-rightEye.y;
                    mouth.x = 0-mouth.x;
                    mouth.y = 0-mouth.y;
                }

                //decide the shap size
                ShapeSize = Math.abs(rightEye.x - leftEye.x);
                if(ShapeSize > 0){
                    ShapeSize = ShapeSize*10;
                }
                Log.e("ShapeSize:","val="+ShapeSize);

                centerX = (leftEye.y + 1000)*preview_Width/2000;
                centerY = (leftEye.x + 1000)*preview_Height/2000;
                Log.e("leftEye","centerX:"+centerX+",centerY:"+centerY);
                //canvas.drawCircle(centerX, centerY, 20, mLinePaint);
                mLeftEye.setBounds(Math.round(centerX-ShapeSize),
                        Math.round(centerY-ShapeSize),
                        Math.round(centerX+ShapeSize),
                        Math.round(centerY+ShapeSize));
                mLeftEye.draw(canvas);

                centerX = (rightEye.y + 1000)*preview_Width/2000;
                centerY = (rightEye.x + 1000)*preview_Height/2000;
                Log.e("rightEye","centerX:"+centerX+",centerY:"+centerY);
                //canvas.drawCircle(centerX, centerY, 20, mLinePaint);
                mRightEye.setBounds(Math.round(centerX-ShapeSize),
                        Math.round(centerY-ShapeSize),
                        Math.round(centerX+ShapeSize),
                        Math.round(centerY+ShapeSize));
                mRightEye.draw(canvas);

                centerX = (mouth.y + 1000)*preview_Width/2000;
                centerY = (mouth.x + 1000)*preview_Height/2000;
                Log.e("Mouth","centerX:"+centerX+",centerY:"+centerY);
                mMouth.setBounds(Math.round(centerX-ShapeSize),
                        Math.round(centerY-ShapeSize),
                        Math.round(centerX+ShapeSize),
                        Math.round(centerY+ShapeSize));
                //canvas.drawCircle(centerX, centerY, 20, mLinePaint);
                mMouth.draw(canvas);
            }
        }
        canvas.restore();
    }

    private void initPaint(){
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.RED);
        //int color = Color.rgb(98, 212, 68);
        //mLinePaint.setColor(color);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5f);
        mLinePaint.setAlpha(180);
    }
}
