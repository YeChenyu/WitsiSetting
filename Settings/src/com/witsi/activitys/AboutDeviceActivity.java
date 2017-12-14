package com.witsi.activitys;


import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.AndroidDev;

import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutDeviceActivity extends Activity implements OnClickListener{

	private String TAG = AboutDeviceActivity.class.getSimpleName();
	private Context context = AboutDeviceActivity.this;
	
	private LinearLayout ll_back;
	private LinearLayout ll_update;
	private LinearLayout ll_status;
	private TextView device_type;
	private TextView version_sys;
	private TextView version_baseband;
	private TextView version_kenel;
	private TextView version_num;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_device_activity);
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		((TextView)findViewById(R.id.tv)).setText("关于设备");
		findViewById(R.id.action_back).findViewById(R.id.sw).setVisibility(View.GONE);
		ll_back.setOnClickListener(this);
		ll_update = (LinearLayout) findViewById(R.id.ll_update);
		ll_status = (LinearLayout) findViewById(R.id.ll_status);
		ll_update.setOnClickListener(this);
		ll_status.setOnClickListener(this);
		
		device_type = (TextView) findViewById(R.id.tv1);
		version_sys = (TextView) findViewById(R.id.tv2);
		version_baseband = (TextView) findViewById(R.id.tv3);
		version_kenel = (TextView) findViewById(R.id.tv4);
		version_num = (TextView) findViewById(R.id.tv5);
		
		
		FyLog.d(TAG, "the model is: " + Build.MODEL + "hte release is: " + Build.VERSION.RELEASE
				+ "the kernel_ver is: " + AndroidDev.getKernel_Ver() + "the display is: " + Build.DISPLAY
				+ "the baseband is: " + AndroidDev.getBaseband_Ver());
		device_type.setText("至能"+Build.MODEL);
		version_sys.setText(Build.VERSION.RELEASE);
		version_kenel.setText(AndroidDev.getKernel_Ver());
		version_num.setText(Build.DISPLAY);
		version_baseband.setText(AndroidDev.getBaseband_Ver());
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_back:
			finish();
			break;
		case R.id.ll_update:
			Toast.makeText(context, "未提供该功能！", Toast.LENGTH_SHORT).show();
//			finish();
			break;
		case R.id.ll_status:
			Intent intent = new Intent(AboutDeviceActivity.this,
					WorkStatusActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		default:
			break;
		}
	}
}
