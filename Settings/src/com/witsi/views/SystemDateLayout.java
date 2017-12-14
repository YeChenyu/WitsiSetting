package com.witsi.views;

import java.util.Calendar;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.views.WifiDisconLayout.OnClickNoPassListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

public class SystemDateLayout implements OnClickListener, OnCheckedChangeListener{

	private String TAG = SystemDateLayout.class.getSimpleName();
	private Context context;
	private ScrollView v;
	
	private CheckBox cb, cb1, cb2;
	private LinearLayout set_date, set_time, set_date_block;
	private TextView date, time, date_block, time_type, date_type;
	private LinearLayout inc_date_type, inc_date, inc_time;
	private DatePicker mDatePicker;
	private RelativeLayout rl_set_date, rl_cancel_date;
	private TimePicker mTimePicker;
	private RelativeLayout rl_set_time, rl_cancel_time;
	private RadioButton rl_mdy, rl_dmy, rl_ymd, local;
	private Button btn;
	
	
	public SystemDateLayout(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		v = (ScrollView) LayoutInflater.from(context).inflate(R.layout.display_sysdate_layout, null);
		v.findViewById(R.id.net_date).setOnClickListener(this);
		v.findViewById(R.id.net_date_block).setOnClickListener(this);
		set_date = (LinearLayout) v.findViewById(R.id.date);
		set_date.setOnClickListener(this);
		set_time = (LinearLayout) v.findViewById(R.id.time);
		set_time.setOnClickListener(this);
		set_date_block = (LinearLayout) v.findViewById(R.id.select_data_block);
		set_date_block.setOnClickListener(this);
		v.findViewById(R.id.time_type).setOnClickListener(this);
		v.findViewById(R.id.date_type).setOnClickListener(this);
				
		
		cb = (CheckBox) v.findViewById(R.id.cb);
		cb1 = (CheckBox) v.findViewById(R.id.cb1);
		date = (TextView) v.findViewById(R.id.tv);
		time = (TextView) v.findViewById(R.id.tv1);
		date_block = (TextView) v.findViewById(R.id.tv2);
		time_type = (TextView) v.findViewById(R.id.tv3);
		cb2 = (CheckBox) v.findViewById(R.id.cb2);
		date_type = (TextView) v.findViewById(R.id.tv4);
		
		inc_date = (LinearLayout) v.findViewById(R.id.inc_date);
		inc_time = (LinearLayout) v.findViewById(R.id.inc_time);
		inc_date_type = (LinearLayout) v.findViewById(R.id.inc_date_type);
		local = (RadioButton) inc_date_type.findViewById(R.id.cb_dt);
		rl_mdy = (RadioButton) inc_date_type.findViewById(R.id.cb1_dt);
		rl_dmy = (RadioButton) inc_date_type.findViewById(R.id.cb2_dt);
		rl_ymd = (RadioButton) inc_date_type.findViewById(R.id.cb3_dt);
		
		mDatePicker = (DatePicker) inc_date.findViewById(R.id.date);
		mTimePicker = (TimePicker) inc_time.findViewById(R.id.time);
		
		btn = (Button) v.findViewById(R.id.btn);
		//自动确定日期，时间和时区
		try {
			FyLog.d(TAG, "the datetype is: " + System.getInt(context.getContentResolver(),
					System.AUTO_TIME));
			if(System.getInt(context.getContentResolver(),
					System.AUTO_TIME) == 1){
				cb.setChecked(true);
				set_date.setVisibility(View.GONE);
				set_time.setVisibility(View.GONE);
			}else{
				cb.setChecked(false);
				set_date.setVisibility(View.VISIBLE);
				set_time.setVisibility(View.VISIBLE);
			}
			FyLog.d(TAG, "the datetype is: " + System.getInt(context.getContentResolver(),
					System.AUTO_TIME_ZONE));
			if(System.getInt(context.getContentResolver(),
					System.AUTO_TIME_ZONE) == 1){
				cb1.setChecked(true);
				set_date_block.setVisibility(View.GONE);
			}else{
				cb1.setChecked(false);
				set_date_block.setVisibility(View.VISIBLE);
			}
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//选择时间格式
		String str = System.getString(context.getContentResolver(),
				System.TIME_12_24);
		FyLog.d(TAG, "time type is: " + str);
		if(str != null && str.equals("24")){
			time_type.setText("13:00");
			cb2.setChecked(true);
		}else if(str != null){
			time_type.setText("下午 1:00");
			cb2.setChecked(false);
		}
		//选择日期格式
		str = System.getString(context.getContentResolver(),
				System.DATE_FORMAT);
		FyLog.d(TAG, "date type is: " + str);
		if(str != null && str.length() > 0){
			if(str.equals("MM-dd-yyyy")){
				date_type.setText("07-31-2016");
				rl_mdy.setChecked(true);
			}else if(str.equals("dd-MM-yyyy")){
				date_type.setText("31-07-2016");
				rl_dmy.setChecked(true);
			}else if(str.equals("yyyy-MM-dd")){
				date_type.setText("2016-07-31");
				rl_ymd.setChecked(true);
			}
		}else if(str != null ){
			date_type.setText("所在区域（2016-7-31）");
			local.setChecked(true);
		}
		
		cb.setOnCheckedChangeListener(this);
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
		inc_date.findViewById(R.id.rl1_date).setOnClickListener(this);
		inc_date.findViewById(R.id.rl2_date).setOnClickListener(this);
		inc_time.findViewById(R.id.rl1_time).setOnClickListener(this);
		inc_time.findViewById(R.id.rl2_time).setOnClickListener(this);
		inc_date_type.findViewById(R.id.rl).setOnClickListener(this);
		inc_date_type.findViewById(R.id.rl1).setOnClickListener(this);
		inc_date_type.findViewById(R.id.rl2).setOnClickListener(this);
		inc_date_type.findViewById(R.id.rl3).setOnClickListener(this);
		local.setOnCheckedChangeListener(this);
		rl_dmy.setOnCheckedChangeListener(this);
		rl_mdy.setOnCheckedChangeListener(this);
		rl_ymd.setOnCheckedChangeListener(this);
	}
	
	public View getView(){
		return v;
	}

	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.net_date:
			if(!cb.isChecked())
				cb.setChecked(true);
			else
				cb.setChecked(false);
			break;
		case R.id.net_date_block:
			if(!cb1.isChecked())
				cb1.setChecked(true);
			else
				cb1.setChecked(false);
			break;
		//时间
		case R.id.time:
			if(inc_time.getVisibility() == View.GONE)
				inc_time.setVisibility(View.VISIBLE);
			else
				inc_time.setVisibility(View.GONE);
			break;
		case R.id.rl1_time:
			int hour = mTimePicker.getCurrentHour();
			int minute = mTimePicker.getCurrentMinute();
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR, hour);
			c.set(Calendar.MINUTE, minute);
			FyLog.d(TAG, "the time is: " + hour + " " + minute);		
		case R.id.rl2_time:
			inc_time.setVisibility(View.GONE);
			break;
		case R.id.time_type:
			if(!cb2.isChecked()){
				cb2.setChecked(true);
			}else{
				cb2.setChecked(false);
			}
			break;
		//日期
		case R.id.date:
			if(inc_date.getVisibility() == View.GONE){
				inc_date.setVisibility(View.VISIBLE);
			}else
				inc_date.setVisibility(View.GONE);
			break;
		case R.id.rl1_date:
			int year = mDatePicker.getYear();
			int month = mDatePicker.getMonth();
			int day = mDatePicker.getDayOfMonth();
			Calendar d = Calendar.getInstance();
			d.set(Calendar.YEAR, year);
			d.set(Calendar.MONTH, month+1);
			d.set(Calendar.DAY_OF_MONTH, day);
			FyLog.d(TAG, "day is: " + year + " " + month + " " + day);
		case R.id.rl2_date:
			inc_date.setVisibility(View.GONE);
			break;
		case R.id.date_type:
			if(inc_date_type.getVisibility() == View.GONE){
				inc_date_type.setVisibility(View.VISIBLE);
				new Handler().postDelayed(new Runnable() {
				    @Override
				    public void run() {
				        v.fullScroll(ScrollView.FOCUS_DOWN);
				    }
				}, 80);
			}else
				inc_date_type.setVisibility(View.GONE);
			break;
		case R.id.rl:
			if(!local.isChecked())
				local.setChecked(true);
			else
				local.setChecked(false);
			break;
		case R.id.rl1:
			if(!rl_mdy.isChecked())
				rl_mdy.setChecked(true);
			else
				rl_mdy.setChecked(false);
			break;
		case R.id.rl2:
			if(!rl_dmy.isChecked())
				rl_dmy.setChecked(true);
			else
				rl_dmy.setChecked(false);
			break;
		case R.id.rl3:
			if(!rl_ymd.isChecked())
				rl_ymd.setChecked(true);
			else
				rl_ymd.setChecked(false);
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "check change: " + arg1);
//		((ScrollView)v).scrollTo(0, ScrollView.SCROLLBARS_INSIDE_OVERLAY);
		switch (arg0.getId()) {
		//时间同步方式
		case R.id.cb:
			if(arg1){
				set_date.setVisibility(View.GONE);
				set_time.setVisibility(View.GONE);
			}else{
				set_date.setVisibility(View.VISIBLE);
				set_time.setVisibility(View.VISIBLE);
			}
			break;
		//时区选择
		case R.id.cb1:
			if(arg1){
				set_date_block.setVisibility(View.GONE);
			}else{
				set_date_block.setVisibility(View.VISIBLE);
			}
			break;
		//时间显示格式
		case R.id.cb2:
			if(arg1){
				time_type.setText("13:00");
				System.putString(context.getContentResolver(),
						System.TIME_12_24, "24");
			}else{
				time_type.setText("下午 1:00");
				System.putString(context.getContentResolver(),
						System.TIME_12_24, "12");
			}
			break;
		case R.id.cb_dt:
			if(arg1){
				date_type.setText("所在区域（2016-7-31）");
				System.putString(context.getContentResolver(),
						System.DATE_FORMAT, "");
			}
			inc_date_type.setVisibility(View.GONE);
			break;
		case R.id.cb1_dt:
			if(arg1){
				date_type.setText("07-31-2016");
				System.putString(context.getContentResolver(),
						System.DATE_FORMAT, "MM-dd-yyyy");
			}
			inc_date_type.setVisibility(View.GONE);
			break;
		case R.id.cb2_dt:
			if(arg1){
				date_type.setText("31-07-2016");
				System.putString(context.getContentResolver(),
						System.DATE_FORMAT, "dd-MM-yyyy");
			}
			inc_date_type.setVisibility(View.GONE);
			break;
		case R.id.cb3_dt:
			if(arg1){
				date_type.setText("2016-07-31");
				System.putString(context.getContentResolver(),
						System.DATE_FORMAT, "yyyy-MM-dd");
			}
			inc_date_type.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	public interface OnComplishListener{
		public void confirm();
	}
	
	private OnComplishListener mListener = null;
	public void setOnComplishListener(OnComplishListener listener){
		this.mListener = listener;
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(mListener != null)
					mListener.confirm();
			}
		});
	}
}
