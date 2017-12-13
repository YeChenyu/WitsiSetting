package com.witsi.views;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

public class SleepLayout implements OnClickListener, OnCheckedChangeListener{

	private String TAG = SleepLayout.class.getSimpleName();
	private Context context;
	
	private View v;
	
	private RelativeLayout s15, s30, m1, m5, m10, m30, none;
	private RadioButton cb, cb1, cb2, cb3, cb4, cb5, cb6;
	
	private OnDismissListener listener;
	public SleepLayout(Context context, OnDismissListener listener) {
		super();
		this.context = context;
		this.listener = listener;
		v = LayoutInflater.from(context).inflate(R.layout.display_sleep_layout,
				null);
		
		v.findViewById(R.id.s15).setOnClickListener(this);
		v.findViewById(R.id.s30).setOnClickListener(this);
		v.findViewById(R.id.m1).setOnClickListener(this);
		v.findViewById(R.id.m5).setOnClickListener(this);
		v.findViewById(R.id.m10).setOnClickListener(this);
		v.findViewById(R.id.m30).setOnClickListener(this);
		v.findViewById(R.id.none).setOnClickListener(this);
		cb = (RadioButton) v.findViewById(R.id.cb);
		cb1 = (RadioButton) v.findViewById(R.id.cb1);
		cb2 = (RadioButton) v.findViewById(R.id.cb2);
		cb3 = (RadioButton) v.findViewById(R.id.cb3);
		cb4 = (RadioButton) v.findViewById(R.id.cb4);
		cb5 = (RadioButton) v.findViewById(R.id.cb5);
		cb6 = (RadioButton) v.findViewById(R.id.cb6);
		
		String str = getTimeOut(context.getContentResolver());
		if(str.equals("15秒")){
			cb.setChecked(true);
		}else if(str.equals("30秒")){
			cb1.setChecked(true);
		}else if(str.equals("1分钟")){
			cb2.setChecked(true);
		}else if(str.equals("5分钟")){
			cb3.setChecked(true);
		}else if(str.equals("10分钟")){
			cb4.setChecked(true);
		}else if(str.equals("30分钟")){
			cb5.setChecked(true);
		}
		cb.setOnCheckedChangeListener(this);
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
		cb3.setOnCheckedChangeListener(this);
		cb4.setOnCheckedChangeListener(this);
		cb5.setOnCheckedChangeListener(this);
		cb6.setOnCheckedChangeListener(this);
		
	}

	public View getView(){
		return v;
	}

	public interface OnDismissListener{
		public void onDismiss();
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.s15:
			if(!cb.isChecked())
				cb.setChecked(true);
			else
				cb.setChecked(false);
			break;
		case R.id.s30:
			if(!cb1.isChecked())
				cb1.setChecked(true);
			else
				cb1.setChecked(false);
			break;
		case R.id.m1:
			if(!cb2.isChecked())
				cb2.setChecked(true);
			else
				cb2.setChecked(false);
			break;
		case R.id.m5:
			if(!cb3.isChecked())
				cb3.setChecked(true);
			else
				cb3.setChecked(false);
			break;
		case R.id.m10:
			if(!cb4.isChecked())
				cb4.setChecked(true);
			else
				cb4.setChecked(false);
			break;
		case R.id.m30:
			if(!cb5.isChecked())
				cb5.setChecked(true);
			else
				cb5.setChecked(false);
			break;
		case R.id.none:
			if(!cb6.isChecked())
				cb6.setChecked(true);
			else
				cb6.setChecked(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "check change: " + arg1);
		switch (arg0.getId()) {
		case R.id.cb:
			if(arg1){
				setTimeOut(15);
				listener.onDismiss();
			}
			break;
		case R.id.cb1:
			if(arg1){
				setTimeOut(30);
				listener.onDismiss();
			}
			break;
		case R.id.cb2:
			if(arg1){
				setTimeOut(60);
				listener.onDismiss();
			}
			break;
		case R.id.cb3:
			if(arg1){
				setTimeOut(300);
				listener.onDismiss();
			}
			break;
		case R.id.cb4:
			if(arg1){
				setTimeOut(600);
				listener.onDismiss();
			}
			break;
		case R.id.cb5:
			if(arg1){
				setTimeOut(1800);
				listener.onDismiss();
			}
			break;
		case R.id.cb6:
			if(arg1){
//				setTimeOut(-1);
			}
			break;

		default:
			break;
		}
	}

	/**
	* 设置系统休眠时间
	* @param 超时世间
	*/
	private void setTimeOut(int timeout){
		Settings.System.putInt(context.getContentResolver()
				, Settings.System.SCREEN_OFF_TIMEOUT, timeout*1000);
	}
	
	/**
	* 获取系统休眠时间
	* @return 秒数(从不待机 返回0)
	*/
	public static String getTimeOut(ContentResolver resolver){
		int timeOut = 0;
		try {
			timeOut = Settings.System.getInt(resolver, Settings.System.SCREEN_OFF_TIMEOUT);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		String timeOutStr = "";
		timeOut /= 1000;
		if(timeOut/60 == 0){
			timeOutStr += (timeOut % 60) ;
			timeOutStr += "秒" ;
		}else{
			timeOutStr += timeOut / 60;
			timeOutStr += "分钟";
		}
		return timeOutStr;
	}
}
