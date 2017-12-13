package com.witsi.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

	public static final String TAG = "MarqueeTextView";

	/** ��Ļ�������ٶ� �죬��ͨ���� */
	public static final int SCROLL_SLOW = 0;
	public static final int SCROLL_NORM = 1;
	public static final int SCROLL_FAST = 2;

	/** ��Ļ���� */
	private String mText;

	/** ��Ļ������ɫ */
	private int mTextColor;

	/** ��Ļ�����С */
	private float mTextSize;

	private float offX = 10f;

	private float mStep = 0.5f;

	private Rect mRect = new Rect();

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);;

	public MarqueeTextView(Context context) {
		super(context);
		setSingleLine(true);
	}

	public MarqueeTextView(Context context, AttributeSet attr) {
		super(context, attr);
		setSingleLine(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mText = getText().toString();
		mTextColor = getCurrentTextColor();
		mTextSize = getTextSize();
		mPaint.setColor(mTextColor);
		mPaint.setTextSize(mTextSize);
		mPaint.getTextBounds(mText, 0, mText.length(), mRect);
	};

	@Override
	protected void onDraw(Canvas canvas) {
		float x, y;
		x = getMeasuredWidth() - offX*2;
		y = getMeasuredHeight() / 2 + (mPaint.descent() - mPaint.ascent()) / 2;
		canvas.drawText(mText, x, y, mPaint);
		offX += mStep;
		if (offX >= getMeasuredWidth() ) {
			offX = 0f;
		}
//		if (offX >= getMeasuredWidth() + mRect.width()) {
//			offX = 0f;
//		}
		invalidate();
	}

	/**
	 * ������Ļ�������ٶ�
	 */
	public void setScrollMode(int scrollMod) {
		if (scrollMod == SCROLL_SLOW) {
			mStep = 4f;
		} else if (scrollMod == SCROLL_NORM) {
			mStep = 4f;
		} else {
			mStep = 4f;
		}
	}

}
