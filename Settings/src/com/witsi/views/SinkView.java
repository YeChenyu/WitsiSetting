package com.witsi.views;


import com.witsi.setting1.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 水波浪球形进度条
 */
public class SinkView extends FrameLayout {
    private Bitmap mBitmap, mScaleBitmap;
    private Paint mPaint = new Paint();

    private int mRepeatCount;
    private int mSpeed = 15;
    private float mLeft, mPercent;

    private static final int mTextColor = 0xFFFFFFFF;
    private static final int mTextSize = 30;

    public SinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPercent(float percent) {
        mPercent = percent;
        postInvalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        
        //裁剪成圆形区域
        Path path = new Path();
        canvas.save();
        path.reset();
        canvas.clipPath(path);
        path.addCircle(width / 2, height / 2, width / 2, Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.REPLACE);

      //绘制外圆环
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(100);
        mPaint.setColor(Color.rgb(255, 255, 255));
        canvas.drawCircle(width / 2, height / 2, width / 2 - 2, mPaint);
        
//        if (mScaleBitmap == null) {
//            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.zxcv);
//            mScaleBitmap = Bitmap.createScaledBitmap(mBitmap, getWidth(), getHeight(), false);
//            mBitmap.recycle();
//            mBitmap = null;
//            mRepeatCount = (int) Math.ceil(getWidth() / mScaleBitmap.getWidth() + 0.5) + 1;
//        }
      //绘制外圆环
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(100);
        mPaint.setColor(Color.rgb(20, 231, 21));

        for (int i = 0; i < 2; i++) {
            canvas.drawCircle(width / 2, height / 2, (mPercent) * width / 2 - 2,  mPaint);
        }
        String text = (int) (mPercent * 100) + "%";
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, (getWidth() - mPaint.measureText(text)) / 2, getHeight() / 2 + mTextSize / 2, mPaint);

        mLeft += mSpeed;
        if (mLeft >= getWidth()) {
            mLeft = 0;
        }
        postInvalidateDelayed(20);

        canvas.restore();
    }
}