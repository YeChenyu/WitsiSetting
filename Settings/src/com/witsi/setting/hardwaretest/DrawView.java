package com.witsi.setting.hardwaretest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {
	float preX0 = 0;
	float preY0 = 0;
	float preX1 = 0;
	float preY1 = 0;
	float preX2 = 0;
	float preY2 = 0;

	private Path path0;
	private Path path1;
	private Path path2;
	public Paint paint;
	final int ViewWidth = 480;
	final int ViewHeight = 800;
	Bitmap cacheBitmap = null;
	Canvas cacheCanvas = null;
	int i = 0;
	int flag1 = 1;
	int flag2 = 1;

	/**
	 * 构造继承
	 * */
	public DrawView(Context context, AttributeSet set) {
		super(context, set);
		cacheBitmap = Bitmap.createBitmap(ViewWidth, ViewHeight,
				Config.ARGB_8888);
		cacheCanvas = new Canvas();
		path0 = new Path();
		path1 = new Path();
		path2 = new Path();
		cacheCanvas.setBitmap(cacheBitmap);// 建立位图

		paint = new Paint(Paint.DITHER_FLAG);// 抖动色
		paint.setStyle(Paint.Style.STROKE);// 设置画笔的风格
		paint.setColor(Color.GREEN);// 绿色画笔
		paint.setStrokeWidth(15);// 画笔宽度
		paint.setAntiAlias(true);// 去锯齿
		paint.setDither(true);// 去抖

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {		

			float x0 = event.getX(0);
			float y0 = event.getY(0);
			// float x1 = event.getX(1);
			// float y1 = event.getY(1);
			// float x2 = event.getX(2);
			// float y2 = event.getY(2);

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				i = event.getPointerCount();

				path0.moveTo(x0, y0);// 把起始点放到手指的位置去
				
				preX0 = x0;
				preY0 = y0;
				break;
			case MotionEvent.ACTION_MOVE:
				i = event.getPointerCount();
				if (i > 0) {
					path0.quadTo(preX0, preY0, x0, y0);
					preX0 = x0;
					preY0 = y0;
				}
				// if (i > 1) {
				// if(flag1==1){
				// flag1 = 0;
				// preX1 = x1;
				// preY1 = y1;
				// path1.moveTo(x1, y1);
				// }
				// path1.quadTo(preX1, preY1, x1, y1);
				// preX1 = x1;
				// preY1 = y1;
				// }
				// if (i > 2) {
				// if(flag2==1){
				// flag2 = 0;
				// preX2 = x2;
				// preY2 = y2;
				// path2.moveTo(x2, y2);
				// }
				// path2.quadTo(preX2, preY2, x2, y2);
				// preX2 = x2;
				// preY2 = y2;
				// }
				break;
			case MotionEvent.ACTION_UP:
				//这里注释掉可以保留画线的痕迹
				//path0.reset();  
				//path1.reset();
				//path2.reset();
				flag1 = 1;
				flag2 = 1;
				break;
			}
			invalidate();
			
		//}
		return true;
	}

	@Override
	public void onDraw(Canvas canvas) {
		Paint bmppaint = new Paint();
		canvas.drawBitmap(cacheBitmap, 0, 0, bmppaint);// 显示位图
		canvas.drawPath(path0, paint);
		canvas.drawPath(path1, paint);
		canvas.drawPath(path2, paint);

	}
}
