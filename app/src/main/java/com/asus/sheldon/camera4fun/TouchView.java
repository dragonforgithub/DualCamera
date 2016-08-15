package com.asus.sheldon.camera4fun;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by sheldon on 16-7-11.
 */
public class TouchView extends ImageView{
    private static final String TAG = "sheldon";
    private Paint mLinePaint;
    private RectF mfocusRect;
    private Matrix mMatrix;
    private boolean mfocusDone;
    private boolean needMirror=false;

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMatrix = new Matrix();
        mfocusRect = new RectF();
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //int color = Color.rgb(98, 212, 68);
        //mLinePaint.setColor(color);
        mLinePaint.setColor(Color.GREEN);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(5f);
        mLinePaint.setAlpha(180);
    }

    public void setFocus(Rect focusRect, boolean focusDone, boolean isMirror){
        mfocusRect.set(focusRect);
        needMirror = isMirror;
        mfocusDone = focusDone;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mfocusRect == null){
            Log.e(TAG,"mfocusRect is NULL!");
            return;
        }

        Util.prepareMatrix(mMatrix, needMirror, 0, getWidth(), getHeight());
        mMatrix.mapRect(mfocusRect);

        if(mfocusDone == false){
            Log.i(TAG,"mfocusDone: false");

            Log.e(TAG,"position:"+
                    " = "+Math.round(mfocusRect.left)+
                    " - "+Math.round(mfocusRect.top)+
                    " - "+Math.round(mfocusRect.right)+
                    " - "+Math.round(mfocusRect.bottom));


            //get the circle position and draw
            canvas.drawCircle(mfocusRect.left+(mfocusRect.right-mfocusRect.left)/2,
                              mfocusRect.top+(mfocusRect.bottom-mfocusRect.top)/2,
                              125,mLinePaint);

            //canvas.drawRect(mfocusRect, mLinePaint);

        } else {
            canvas.drawCircle(mfocusRect.left+(mfocusRect.right-mfocusRect.left)/2,
                    mfocusRect.top+(mfocusRect.bottom-mfocusRect.top)/2,
                    100,mLinePaint);
            //canvas.drawRect(mfocusRect, mLinePaint);
        }
    }
}

