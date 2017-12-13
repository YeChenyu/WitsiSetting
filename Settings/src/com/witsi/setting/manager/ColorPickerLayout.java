package com.witsi.setting.manager;

import com.witsi.setting1.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorPickerLayout implements OnSeekBarChangeListener{

	private View v;
	private SeekBar sba, sbr, sbg, sbb;
	private TextView tv, tv1, tv2, tv3, tv4;
	
	public ColorPickerLayout(Context context) {
		// TODO Auto-generated constructor stub
		v = LayoutInflater.from(context).inflate(R.layout.manager_color_picker_layout, 
				null);
		sba = (SeekBar) v.findViewById(R.id.sk);
		sbr = (SeekBar) v.findViewById(R.id.sk1);
		sbg = (SeekBar) v.findViewById(R.id.sk2);
		sbb = (SeekBar) v.findViewById(R.id.sk3);
		tv = (TextView) v.findViewById(R.id.tv);
		tv1 = (TextView) v.findViewById(R.id.tv1);
		tv2 = (TextView) v.findViewById(R.id.tv2);
		tv3 = (TextView) v.findViewById(R.id.tv3);
		tv4 = (TextView) v.findViewById(R.id.tv4);
		
		sba.setOnSeekBarChangeListener(this);
		sbr.setOnSeekBarChangeListener(this);
		sbg.setOnSeekBarChangeListener(this);
		sbb.setOnSeekBarChangeListener(this);
	}

	private int a = 255, r = 255, g = 255, b = 255;
	private int color;
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.sk:
			a = arg1;
			tv.setText("" + a);
			color = Color.argb(a, r, g, b);
			break;
		case R.id.sk1:
			r = arg1;
			tv1.setText("" + r);
			color = Color.argb(a, r, g, b);
			break;
		case R.id.sk2:
			g = arg1;
			tv2.setText("" + g);
			color = Color.argb(a, r, g, b);
			break;
		case R.id.sk3:
			b = arg1;
			tv3.setText("" + b);
			color = Color.argb(a, r, g, b);
			break;

		default:
			break;
		}
		tv4.setBackgroundColor(color);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
	}
	
	public int getColor() {
		return color;
	}
	
	public void setA(int alpha) {
		a = alpha;
		sba.setProgress(alpha);
	}
	public void setColor(int color) {
		 r = (color & 0xff0000) >> 16; 
	     g = (color & 0x00ff00) >> 8; 
	     b = (color & 0x0000ff); 
	     sbr.setProgress(r);
	     sbg.setProgress(g);
	     sbb.setProgress(b);
	}
	public View getView() {
		return v;
	}
}
