package com.witsi.activitys;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.SimpleFormatter;

import com.witsi.adapter.MainGvAdapter;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.FondType;
import com.witsi.views.FondLayout;
import com.witsi.views.FondLayout.OnFondDislogDismissListener;
import com.witsi.views.SleepLayout;
import com.witsi.views.SlipButton;
import com.witsi.views.SystemDateLayout;
import com.witsi.views.SystemDateLayout.OnComplishListener;
import com.witsi.views.SleepLayout.OnDismissListener;

import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.filterpacks.videoproc.BackDropperFilter.LearningDoneListener;
import android.media.AudioManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DisplayActivity extends Activity implements OnCheckedChangeListener
				, OnSeekBarChangeListener	, OnClickListener{

	private String TAG = DisplayActivity.class.getSimpleName();
	private final boolean D = true;
	private Context context = DisplayActivity.this;
	
	private LinearLayout ll_back;
	
	private CheckBox cb0;
	private CheckBox cb;
	private CheckBox cb1;
	private TextView tip_music;
	private CheckBox cb2;
	private SeekBar light;
	private TextView sleep_time;
	private TextView fond_size;
	private TextView tv_date;
	private CheckBox cb3;
	private LinearLayout inc_sound;
	private SeekBar sound;
	
	private Configuration mCurConfig = new Configuration();
	private AudioManager audio = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_activity);
		
		initViews();
		
		initDatas();
	}

	private void initViews() {
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		ll_back.setOnClickListener(this);
		((TextView)findViewById(R.id.tv)).setText("声音显示");
		findViewById(R.id.action_back).findViewById(R.id.sw).setVisibility(View.GONE);
		findViewById(R.id.sound).setOnClickListener(this);
		cb0 = (CheckBox) findViewById(R.id.cb0);
		inc_sound = (LinearLayout) findViewById(R.id.inc_sound);
		sound = (SeekBar) inc_sound.findViewById(R.id.sb_sound);
		findViewById(R.id.touch_screen_tip).setOnClickListener(this);
		cb = (CheckBox) findViewById(R.id.cb);
		findViewById(R.id.lock_screen_tip).setOnClickListener(this);
		cb1 = (CheckBox) findViewById(R.id.cb1);
		findViewById(R.id.tip_music).setOnClickListener(this);
		tip_music = (TextView) findViewById(R.id.tv1);
		cb2 = (CheckBox) findViewById(R.id.cb2);
		light = (SeekBar) findViewById(R.id.sb_light);
		findViewById(R.id.sleep).setOnClickListener(this);
		sleep_time = (TextView) findViewById(R.id.tv2);
		findViewById(R.id.fond).setOnClickListener(this);
		fond_size = (TextView) findViewById(R.id.tv3);
		findViewById(R.id.date).setOnClickListener(this);
		tv_date = (TextView) findViewById(R.id.tv4);
		findViewById(R.id.rotation).setOnClickListener(this);
		cb3 = (CheckBox) findViewById(R.id.cb3);
		
		sleep_time.setText("无操作" + getTimeOut(context.getContentResolver())
				+ "后休眠");
		fond_size.setText(FondType.getFontSzie());
	}

	private void initDatas(){
		int set, num;
		//音量
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		try {
			set = System.getInt(context.getContentResolver(),
					System.VOLUME_SYSTEM);
			num = audio.getStreamVolume(AudioManager.STREAM_RING);
			FyLog.d(TAG, "the STREAM_VOICE_CALL is: " + num + " and the flag is: " + set);
			sound.setMax(7);
			sound.setProgress(num);
			if(set > 0){
				cb0.setChecked(true);
				inc_sound.setVisibility(View.VISIBLE);
			}else{
				cb0.setChecked(false);
				inc_sound.setVisibility(View.GONE);
			}
		} catch (SettingNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//触摸提示音
		try {
			set = System.getInt(context.getContentResolver(),
					System.SOUND_EFFECTS_ENABLED);
			FyLog.e(TAG, "the touch sound is: " + set);
			if(set == 1)
				cb.setChecked(true);
			else 
				cb.setChecked(false);
		} catch (SettingNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//获得亮度调节状态
		light.setMax(255);
		try {
			set = System.getInt(context.getContentResolver(),
					System.SCREEN_BRIGHTNESS_MODE);
			light.setProgress(System.getInt(context.getContentResolver(),
					System.SCREEN_BRIGHTNESS));
			FyLog.d(TAG, "the light is： " + light.getProgress());
			if(set == System.SCREEN_BRIGHTNESS_MODE_MANUAL){
				cb2.setChecked(true);
				light.setEnabled(true);
			}else if(set == System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC){
				cb2.setChecked(false);
				light.setEnabled(false);
			}
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//系统字体大小
		try {
            mCurConfig.updateFrom(
                ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
        }
        if (mCurConfig.fontScale < 0.76) {
            fond_size.setText("小");
//            fond_size.setText("小" + mCurConfig.fontScale);
        } else if (mCurConfig.fontScale < 1.1) {
        	fond_size.setText("普通");
//        	fond_size.setText("普通" + mCurConfig.fontScale);
        } else if (mCurConfig.fontScale < 1.26){
        	fond_size.setText("大");
//        	fond_size.setText("大" + mCurConfig.fontScale);
        }else {
        	fond_size.setText("超大");
//        	fond_size.setText("超大" + mCurConfig.fontScale);
        }
		//获取系统时间
		Calendar c = Calendar.getInstance();
		SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String str = d.format(c.getTime());
		tv_date.setText(str);
		//获得重力感应状态
		try {
			set = System.getInt(context.getContentResolver()
					, Settings.System.ACCELEROMETER_ROTATION);
			if(set == 1)
				cb3.setChecked(true);
			else 
				cb3.setChecked(false);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cb0.setOnCheckedChangeListener(this);
		cb.setOnCheckedChangeListener(this);
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
		cb3.setOnCheckedChangeListener(this);
		light.setOnSeekBarChangeListener(this);
		sound.setOnSeekBarChangeListener(this);
		
		registerReceiver(register, getIntentFilter());
	}
	
	public IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
		return filter;
	}
	
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_back:
			finish();
			break;
		//设置音量
		case R.id.sound:
			if(!cb0.isChecked()){
				cb0.setChecked(true);
			}else{
				cb0.setChecked(false);
			}
			break;
		//触摸提示音
		case R.id.touch_screen_tip:
			if(!cb.isChecked()){
				cb.setChecked(true);
			}else{
				cb.setChecked(false);
			}
			break;
		//锁屏提示音
		case R.id.lock_screen_tip:
			if(!cb1.isChecked()){
				cb1.setChecked(true);
			}else{
				cb1.setChecked(false);
			}
			break;
		case R.id.tip_music:
			
			break;
		case R.id.sleep:
			OnDismissListener listener = new OnDismissListener() {
				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					sleep_time.setText("无操作" + getTimeOut(context.getContentResolver())
							+ "后休眠");
					dialog.dismiss();
				}
			};
			SleepLayout sleep = new SleepLayout(context, listener);
			showMyDialog(sleep.getView(), 0);
			break;
		case R.id.fond:
			OnFondDislogDismissListener listener1 = new OnFondDislogDismissListener() {
				
				@Override
				public void onDismiss(String fontSzie) {
					// TODO Auto-generated method stub
					fond_size.setText(fontSzie);
					dialog.dismiss();
				}
			};
			FondLayout fond = new FondLayout(context, listener1);
			showMyDialog(fond.getView(), 0);
			break;
		case R.id.date:
			
			SystemDateLayout sysLayout = new SystemDateLayout(context);
			sysLayout.setOnComplishListener(new OnComplishListener() {
				@Override
				public void confirm() {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			showMyDialog(sysLayout.getView(), 0);
			break;
		case R.id.rotation:
			if(!cb3.isChecked()){
				cb3.setChecked(true);
			}else{
				cb3.setChecked(false);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//音量
		case R.id.cb0:
			if(arg1){
				inc_sound.setVisibility(View.VISIBLE);
				System.putInt(context.getContentResolver(),
						System.VOLUME_SYSTEM, sound.getProgress());
				audio.setMode(AudioManager.MODE_NORMAL);
			}else{
				inc_sound.setVisibility(View.GONE);
				System.putInt(context.getContentResolver(),
						System.VOLUME_SYSTEM, 0);
				audio.setMode(AudioManager.MODE_INVALID);
			}
			break;
		//触摸提示音
		case R.id.cb:
			System.putInt(context.getContentResolver(),
					System.SOUND_EFFECTS_ENABLED, arg1 ? 1 : 0);
			break;
		//锁屏提示音
		case R.id.cb1:
			if(arg1){
				
			}else{
				
			}
			break;
		//亮度调节
		case R.id.cb2:
			if(arg1){
				System.putInt(context.getContentResolver(),
						System.SCREEN_BRIGHTNESS_MODE, 
						System.SCREEN_BRIGHTNESS_MODE_MANUAL);
				light.setEnabled(true);
			}else{
				System.putInt(context.getContentResolver(),
						System.SCREEN_BRIGHTNESS_MODE, 
						System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
				light.setEnabled(false);
			}
			break;
		//屏幕选转
		case R.id.cb3:
			FyLog.d(TAG, "屏幕旋转：" + arg1);
			if(arg1){
				Settings.System.putInt(context.getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION, 1);
			}else{
				Settings.System.putInt(context.getContentResolver(),
						Settings.System.ACCELEROMETER_ROTATION, 0);
			}
			break;
		default:
			break;
		}
	}
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.sb_light:
			System.putInt(context.getContentResolver(),
					System.SCREEN_BRIGHTNESS, 
					arg1);
			break;
		//设置音量
		case R.id.sb_sound:
			if(arg1 != system_sound){
				audio.setStreamVolume(AudioManager.STREAM_SYSTEM, arg1, AudioManager.FLAG_PLAY_SOUND);
				system_sound = arg1;
			}
			FyLog.d(TAG, "the sound is: " + arg1);
			break;
		default:
			break;
		}
	}
	
	private int system_sound = 0;
	private BroadcastReceiver register = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			Log.v(TAG, "action: " + arg1.getAction());
			if(action.equals("android.media.VOLUME_CHANGED_ACTION")){
				system_sound = audio.getStreamVolume(AudioManager.STREAM_RING) ;// 当前的媒体音量
				FyLog.d(TAG, "the system_sound is: " + system_sound);
				if(system_sound != sound.getProgress())
					sound.setProgress(system_sound);
			}
		}
	};
	
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {}
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {}
	
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
	
	private Dialog dialog = null;
	private void showMyDialog(View layout, int location) {
		dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
		dialog.setContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = location;
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

}
