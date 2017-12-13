package com.witsi.setting.hardwaretest;

import java.io.File;
import java.io.IOException;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.Formatter;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TF_Activity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "TF_Activity";
	private Context context = TF_Activity.this;
	
	TextView TF_show;
	ImageView tf_image;
	Button button_TF_Return, button_TF_test, button_TF_false, button_TF_ok;
	View main = null;
	
	private FileHelper helper;
	private Handler handler = null;
	
	boolean SDexist = false;
	boolean isburning = false;
	boolean isBtnPass = false;
	boolean isAutoPass = false;
	boolean TF_test_over = false;
	boolean screen_sleep = false;
	int flag_serialnumber = -1;
	private StringBuffer TF_show_SB = new StringBuffer();

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
		FyLog.i(TAG, "onCreate()");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		//����ʱ���ع�����
		if (config.getBoolean("light", true) == false) {
			getLayoutInflater();
			// ��װ���ء����ð�~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_tf_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
			screen_sleep = true;
		} else {
			setContentView(R.layout.hardware_tf_activity);
			screen_sleep = false;
		}
		// ��������ѡ����
		isburning = config.getBoolean("flag_burn", false);
		FyLog.v(TAG, "isburning: " + isburning);

		TF_show = (TextView) this.findViewById(R.id.tf_show);
		Display dis = getWindowManager().getDefaultDisplay();
		TF_show.setWidth(dis.getWidth() / 2);
		TF_show.setHeight(dis.getHeight() / 2);
		handler = new Handler();
		View v = findViewById(R.id.ll_tool);
		button_TF_Return = (Button) v.findViewById(R.id.back);
		button_TF_test = (Button) v.findViewById(R.id.test);
		button_TF_false = (Button) v.findViewById(R.id.fail);
		button_TF_ok = (Button) v.findViewById(R.id.pass);
		button_TF_Return.setOnClickListener(TF_Activity.this);
		button_TF_test.setOnClickListener(TF_Activity.this);
		button_TF_false.setOnClickListener(TF_Activity.this);
		button_TF_ok.setOnClickListener(TF_Activity.this);
		helper = new FileHelper(getApplicationContext());

		tf_image = (ImageView) this.findViewById(R.id.tf_image);
		if (screen_sleep == true) {
			tf_image.setBackgroundResource(R.drawable.bg_black);
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				FyLog.i("TF����", "���ʱ�䵽");
				if (TF_test_over == false) {
					isSleepExit = false;
					editor.putString("tf", "ng");
					editor.putInt("error_tf", config.getInt("error_tf", 0) + 1);
					editor.putInt("error", 1);
					editor.commit();
					if (isburning == true) {
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
					} else if (config.getBoolean("alltest", false) == true) {
						if(!isBtnPass){
							isAutoPass = true;
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(TF_Activity.this);
						}
					}

				}
			}
		}, 3000);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				isSleepExit = false;
				TF_show_SB = TF_show_SB.append("TF��������").append("\n\r");
				try {
					TF_show_SB.append("�����ļ���"
							+ helper.createSDFile("test.txt").getAbsolutePath()
							+ "\n\r");
					helper.witeSDFile("test.txt", "ok");
					SDexist = true;
					TF_show_SB.append("SD���Ƿ���ڣ�  ����" + "\n\r");
				} catch (IOException e) {
					e.printStackTrace();
					SDexist = false;
					TF_show_SB.append("SD���Ƿ���ڣ�  ������" + "\n\r");
				}
				if (SDexist) {
					editor.putString("tf", "ok");
					editor.commit();
					FyLog.i("TF����", "��ȡTF��·��");
					TF_show_SB.append("TF��·����").append(helper.getSDPATH())
							.append("\n\r");
					TF_show_SB.append("��ȡ�ļ�:" + helper.readSDFile("test.txt")
							+ "\n\r");
					TF_show_SB.append("ɾ���ļ��Ƿ�ɹ�:"
							+ helper.deleteSDFile("test.txt") + "\n\r");
				} else {
					editor.putString("tf", "ng");
					editor.putInt("error", 1);
					editor.commit();
				}
				TF_show.setText(TF_show_SB.toString());
				isSleepExit = false;
				if (isburning == true) {
					if (TF_test_over == false) {
						TF_test_over = true;
						ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
					}
				} else if (config.getBoolean("alltest", false) == true) {
					if (TF_test_over == false) {
						TF_test_over = true;
						if(!isBtnPass){
							isAutoPass = true;
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(TF_Activity.this);
						}
					}
				}
				flag_serialnumber = 1;
			}
		}, 800);
	}
	
	/**
	 * ���SD���ܴ�С
	 */
	private String getSDTotalSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* totalBlocks);
	}

	/**
	 * ���sd��ʣ�������������ô�С
	 */
	private String getSDAvailableSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* availableBlocks);
	}

	/**
	 * ��û����ڴ��ܴ�С
	 */
	private String getRomTotalSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* totalBlocks);
	}

	/**
	 * ��û�������ڴ�
	 */
	private String getRomAvailableSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(TF_Activity.this, blockSize
				* availableBlocks);
	}

	/************************** �¼����������� ***************************/
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				tf_image.setBackgroundResource(R.drawable.bg_transport);
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

	@Override
	public void onClick(View ledClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			tf_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (ledClick.getId()) {
			case R.id.back: {
				TF_test_over = true;
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(TF_Activity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(TF_Activity.this);
				}else {
					ActivityManagers.trunToBurnStartActivity(TF_Activity.this);
				}  
				break;
			}
			case R.id.test: {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						TF_show_SB = TF_show_SB.append("TF��������").append("\n\r");
						try {
							TF_show_SB.append("�����ļ���"
									+ helper.createSDFile("test.txt").getAbsolutePath()
									+ "\n\r");
							helper.witeSDFile("test.txt", "ok");
							SDexist = true;
							TF_show_SB.append("SD���Ƿ���ڣ�  ����" + "\n\r");
						} catch (IOException e) {
							e.printStackTrace();
							SDexist = false;
							TF_show_SB.append("SD���Ƿ���ڣ�  ������" + "\n\r");
						}
						if (SDexist) {
							editor.putString("tf", "ok");
							FyLog.i("TF����", "��ȡTF��·��");
							TF_show_SB.append("TF��·����").append(helper.getSDPATH())
									.append("\n\r");
							TF_show_SB.append("��ȡ�ļ�:" + helper.readSDFile("test.txt")
									+ "\n\r");
							TF_show_SB.append("ɾ���ļ��Ƿ�ɹ�:"
									+ helper.deleteSDFile("test.txt") + "\n\r");
						} else {
							editor.putString("tf", "ng");
							editor.putInt("error", 1);
						}
						editor.commit();
						isSleepExit = false;
						TF_show.setText(TF_show_SB.toString());
						if (isburning == true) {
							if (TF_test_over == false) {
								TF_test_over = true;
								ActivityManagers.toNextBurnTest(context, config, ActivityManagers.BURN_SERIALNUMBER);
							}
						} else if (config.getBoolean("alltest", false) == true) {
							if (TF_test_over == false) {
								TF_test_over = true;
								editor.putString("tf", "ok");
								editor.commit();
								if(!isBtnPass){
									ActivityManagers.trunToNextActivity();
									ActivityManagers.startNextActivity(TF_Activity.this);
								}
							}
						}
					}
				}, 0);
				break;
			}
			case R.id.pass: {
				if(flag_serialnumber == 1){
					isBtnPass = true;
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("tf", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(TF_Activity.this);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("tf", "ok");
						editor.commit();
						if(!isAutoPass){
							ActivityManagers.trunToNextActivity();
							ActivityManagers.startNextActivity(TF_Activity.this);
						}
					}else {
						ActivityManagers.trunToBurnStartActivity(TF_Activity.this);
					} 
				}
				break;
			}
			case R.id.fail: {
				isBtnPass = true;
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("tf", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(TF_Activity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("tf", "ng");
					editor.commit();
					if(!isAutoPass){
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(TF_Activity.this);
					}
				}else {
					ActivityManagers.trunToBurnStartActivity(TF_Activity.this);
				} 
				break;
			}
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume");
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		if(handler != null)
			handler = null;
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
					isburning = false;
					editor.putBoolean("burn", false);
					editor.commit();
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