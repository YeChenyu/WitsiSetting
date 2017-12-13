package com.witsi.setting.hardwaretest;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

import android.witsi.arq.*;

public class RtcActivity extends Activity implements OnClickListener
							{
	private static final String TAG = "RtcActivity";
	private Context context = RtcActivity.this;
	
	private static TextView tvCur;
	private static EditText etSet;
	private Button button_rtc_return;
	private Button button_rtc_ok;
	private Button button_rtc_false;
	ImageView rtc_image;
	View main = null;
	
	private static ArqMisc miscArq;
	private MyRunnable myrun = new MyRunnable();
	private MyHandler mHandler = new MyHandler(this);
	
	boolean isburning = false;
	public int flag_rtc = -1;
	
	private SharedPreferences config;
	private Editor editor;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		// ��������ѡ����
		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// ��װ���ء����ð�~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_rtc_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_rtc_activity);
		}
		
		// SysApplication.getInstance().addActivity(this);
		miscArq = new ArqMisc(this);
        
		tvCur = (TextView) findViewById(R.id.current_date_value);
		etSet = (EditText) findViewById(R.id.set_time_value);
		
		View v = findViewById(R.id.ll_tool);
		button_rtc_return = (Button) findViewById(R.id.back);
		button_rtc_false = (Button) findViewById(R.id.fail);
		button_rtc_ok = (Button) findViewById(R.id.pass);
		((Button)findViewById(R.id.test)).setText("");
		
		button_rtc_false.setOnClickListener(this);
		button_rtc_ok.setOnClickListener(this);
		button_rtc_return.setOnClickListener(this);
		
		etSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if((etSet.getText().toString().contains("YYYY")
						 || etSet.getText().toString().contains("Set"))){
					etSet.setText("");
				}
			}
		});
		
		rtc_image = (ImageView) this.findViewById(R.id.rtc_image);
		if (config.getBoolean("light", true) == false) {
			rtc_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
	}
	
    protected void onResume() {  
    	super.onResume();
        
        FyLog.i(TAG, "###### onResume called. #######"); 
        if(myrun.isThreadRunnig() == false) {
    		Thread thread = new Thread(myrun);
    		thread.start();
        } 
    }
    
    protected void onPause() {  
        FyLog.i(TAG, "###### onPause called. ######");  
        if(myrun.isThreadRunnig() == true) {
        	myrun.stopThread();
        } 
        super.onPause();  
    }	
	
	class MyRunnable implements Runnable
	{
		private boolean threadRunning = false;
		
		public void stopThread()
		{
			threadRunning = false;
		}
		
		public boolean isThreadRunnig()
		{
			return threadRunning;
		}
		
		public void run()
		{
			FyLog.i(TAG, "get date thread working.");
			String date;
			
			threadRunning = true;

			while(threadRunning) {
		    	/* read magc card track */
		    	date = miscArq.getDate();
		    	if(date == null) {  
		    		FyLog.e(TAG, "Misc get date failed. date == null");
		    		continue;
		    	}
		    	  	
		    	Message msg = new Message();
		    	msg.what = 1;
		    	
		    	Bundle bundle = new Bundle();
		    	bundle.putString("date", date);

		    	/* send message */
		    	msg.setData(bundle);
		    	mHandler.sendMessage(msg);
		    	
		    	/* wait 1s */
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					FyLog.d(TAG, "Waiting didnt work!!");
		            e.printStackTrace();
				}
			}
			
			threadRunning = false;			
		}
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if (screen_sleep == true) {
			rtc_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
			toggleBrightness(this, 200);// ����
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (arg0.getId()) {
			case R.id.back:
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(RtcActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(RtcActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(RtcActivity.this);
				} 
				break;
			case R.id.pass:
				flag_rtc = 1;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("rtc", "ok");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(RtcActivity.this);
				}else if (config.getBoolean("alltest", false) == true) { 
					editor.putString("rtc", "ok");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(RtcActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(RtcActivity.this);
				} 
				break;
			case R.id.fail:
				flag_rtc = 0;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("rtc", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(RtcActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("rtc", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(RtcActivity.this);
				} else {
					ActivityManagers.trunToBurnStartActivity(RtcActivity.this);
				} 
				break;
			case R.id.test:
				if(myrun.isThreadRunnig() == false) {
		    		Thread thread = new Thread(myrun);
		    		thread.start();
		        } 
				break;
			default:
				break;
			}
		}
	}
	
	static class MyHandler extends Handler {
		//WeakReference to the outer class's instance
		private WeakReference<RtcActivity> mOuter;
		private Date date;
		private boolean isSet = false;
		
		public MyHandler(RtcActivity activity) {
			mOuter = new WeakReference<RtcActivity>(activity);
		}
		
		public void handleMessage(Message msg) {
			RtcActivity outer = mOuter.get();
			switch (msg.what) {
			case 1:
				if(outer != null) {
					String dateMsg = msg.getData().getString("date");
					if(dateMsg == null) {
						FyLog.e(TAG, "No data received from Message.");
						return;
					}
					Date tmp = null;
					/* format output date: 2014��04��08�� 16:05:30 */
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
				    try {
						date = format.parse(dateMsg);
						tmp = date;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
				    SimpleDateFormat df = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");   
					/* display date+time every 1 second. */
					tvCur.setText(df.format(date));
					if(!isSet){
						tmp.setSeconds(tmp.getSeconds() + 30);
						etSet.setText(format.format(tmp));
						isSet = true;
					}
				}
				break;
			case 0:
				String str = msg.getData().getString("date");
				int ret = miscArq.setDate(str);
		    	if(ret == 0) { /* success. */
		    		etSet.setTextColor(Color.BLUE);
		    		etSet.setText("Set Date success.");
		    		isSet = false;
		    	} else {
		    		etSet.setTextColor(Color.RED);
		    		etSet.setText("Set Date failed. ret = " + ret);
		    	}
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
	}
	
	public void setDateBtnOnClickHandler(View v) {	
		int ret; 
		
    	String date = etSet.getText().toString();
    	if((date == null) || (date.length() != 14)) {
    		etSet.setTextColor(Color.RED);
    		etSet.setText("YYYYMMDDHHMMSS");
    		return;
    	}	
    	Message msg = new Message();
    	msg.what = 0;
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("date", date);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
    	
	}


	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		if(!isSleepExit){
			finish();
		}
	}
	/************************** �¼����������� ***************************/
	@SuppressWarnings("deprecation")
	//������ؼ������ʾ����
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // �����˳��Ի���  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // ���öԻ������  
            isExit.setTitle("ϵͳ��ʾ");  
            // ���öԻ�����Ϣ  
            isExit.setMessage("ȷ��Ҫ�˳���");  
            // ���ѡ��ť��ע�����  
            isExit.setButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ActivityManagers.clearActivity();
					finish();
				}
			});  
            isExit.setButton2("ȡ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // ��ʾ�Ի���  
            isExit.show();  
        }  
        return false;  
    }  

	//����Ϊ������ؼ������ʾ����	
	boolean screen_sleep = false;
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				screen_sleep = false;
				break;
			// �������ƶ�ʱ��
			case MotionEvent.ACTION_MOVE:
				break;
			// ��ֹ����ʱ��
			case MotionEvent.ACTION_UP:
				break;
			}
		}
		return false;
	}
	/**
	 * ���ı�������
	 * 
	 * @param activity
	 */
	public void toggleBrightness(Activity activity, int light) {
		// ��ȡ����ֵ
		int brightness = getScreenBrightness(activity);
		// �Ƿ������Զ����ڣ��������ر��Զ�����
		boolean isAutoBrightness = isAutoBrightness(getContentResolver());
		if (isAutoBrightness) {
			stopAutoBrightness(activity);
		}
		// brightness += 50;// ���Լ�����������
		// ��������
		setBrightness(activity, light);

		if (brightness > 255) {
			// ���ȳ������ֵ������Ϊ�Զ�����
			startAutoBrightness(activity);
			brightness = 50;// ���Լ�����������
		}
		// ��������״̬
		saveBrightness(getContentResolver(), brightness);
	}

	/**
	 * �ж��Ƿ������Զ����ȵ���
	 * 
	 * @param aContext
	 * @return
	 */
	public boolean isAutoBrightness(ContentResolver aContentResolver) {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(aContentResolver,
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}

	/**
	 * ��ȡ��Ļ������
	 * 
	 * @param activity
	 * @return
	 */
	public int getScreenBrightness(Activity activity) {
		int nowBrightnessValue = 0;
		ContentResolver resolver = activity.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(
					resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	/**
	 * ��������
	 * 
	 * @param activity
	 * @param brightness
	 */
	public void setBrightness(Activity activity, int brightness) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
		activity.getWindow().setAttributes(lp);

	}

	/**
	 * ֹͣ�Զ����ȵ���
	 * 
	 * @param activity
	 */
	public void stopAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	/**
	 * ���������Զ�����
	 * 
	 * @param activity
	 */
	public void startAutoBrightness(Activity activity) {
		Settings.System.putInt(activity.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	/**
	 * ������������״̬
	 * 
	 * @param resolver
	 * @param brightness
	 */
	public void saveBrightness(ContentResolver resolver, int brightness) {
		Uri uri = android.provider.Settings.System
				.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
		android.provider.Settings.System.putInt(resolver,
				Settings.System.SCREEN_BRIGHTNESS, brightness);
		resolver.notifyChange(uri, null);
	}
}
