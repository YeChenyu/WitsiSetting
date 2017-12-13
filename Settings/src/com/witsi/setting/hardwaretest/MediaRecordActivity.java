package com.witsi.setting.hardwaretest;


import java.io.File;
import java.io.IOException;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.AudioRecorder;
import com.witsi.tools.ConfigSharePaference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MediaRecordActivity extends Activity implements OnClickListener{
	private String TAG = "RecordActivity";
	private Context context = MediaRecordActivity.this;
	
	private Button record, player, play;
	private AudioRecorder mr;
	private MediaPlayer mediaPlayer;
	private Thread recordThread;
	
	private Button media_return;
	private Button media_ok;
	private Button media_false;
	private Button media_test;
	
	private static int MAX_TIME = 15;    
	private static int MIX_TIME = 1;     
	
	private static int RECORD_NO = 0;  
	private static int RECORD_ING = 1; 
	private static int RECODE_ED = 2;  	
	private static int RECODE_STATE = 0;
	
	private static float recodeTime=0.0f;
	private static double voiceValue=0.0;
	
	public int falg_buzzer = -1;
	
	private ImageView dialog_img;
	private ProgressBar pb;
	private static boolean playState = false; 
	private SharedPreferences config;
	private Editor editor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏
		setContentView(R.layout.hardware_media_activity);
		FyLog.i(TAG, "音频测试界面");
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		player = (Button) findViewById(R.id.button1);
		record = (Button) findViewById(R.id.button2);
		play = (Button) findViewById(R.id.button3);
		View v = findViewById(R.id.ll_tool);
		media_return = (Button) v.findViewById(R.id.back);
		media_ok = (Button) v.findViewById(R.id.pass);
		media_false = (Button) v.findViewById(R.id.fail);
		media_test = (Button) v.findViewById(R.id.test);
		media_return.setOnClickListener(MediaRecordActivity.this);
		media_ok.setOnClickListener(MediaRecordActivity.this);
		media_false.setOnClickListener(MediaRecordActivity.this);
		media_test.setOnClickListener(MediaRecordActivity.this);
		dialog_img = (ImageView)findViewById(R.id.dialog_img);
		pb = (ProgressBar) findViewById(R.id.pb);
		
		player.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//播放音频
				playVoice(player);
				pb.setProgress(0);
			}
		});

		record.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(!record.getText().toString().equals("正在录音...")){
					if (RECODE_STATE != RECORD_ING) {
						scanOldFile();	//如果文件存在，则删除	
						mr = new AudioRecorder("voice");
						RECODE_STATE = RECORD_ING;
						try {
							mr.start();
							record.setText("正在录音...");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mythread();
					}
				}else{
					if (RECODE_STATE == RECORD_ING) {
						RECODE_STATE=RECODE_ED;
						record.setText("点击开始录音");
						try {
								mr.stop();
								voiceValue = 0.0;
						} catch (IOException e) {
								e.printStackTrace();
						}
						if (recodeTime < MIX_TIME) {
							showWarnToast();
							record.setText("录音时间太短！");
							RECODE_STATE=RECORD_NO;
						}else{
							 record.setText("录音成功");
							 play.setEnabled(true);
							 pb.setEnabled(true);
						}
					}
				}
				
			}
		});
		play.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				playVoice(play);
			}
		});
	}
	
	@Override
	public void onClick(View BuzzerClick) {
		editor.putBoolean("flag_burn", false);
		editor.commit();
		isSleepExit = false;
		switch (BuzzerClick.getId()) {
		case R.id.back: {
			if (config.getBoolean("singletest", false) == true) {
				ActivityManagers.trunToSingleTestActivity(MediaRecordActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				ActivityManagers.trunToEntryActivity(MediaRecordActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(MediaRecordActivity.this);
			} 
			break;
		}
		case R.id.pass: {
			falg_buzzer = 1;
			if (config.getBoolean("singletest", false) == true){
				editor.putString("media", "ok");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(MediaRecordActivity.this);
			} else if (config.getBoolean("alltest", false) == true){
				editor.putString("media", "ok");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(MediaRecordActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(MediaRecordActivity.this);
			} 
			break;
		}
		case R.id.fail: {
			falg_buzzer = -1;
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("media", "ng");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(MediaRecordActivity.this);
			} else if (config.getBoolean("alltest", false) == true){
				editor.putString("media", "ng");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(MediaRecordActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(MediaRecordActivity.this);
			} 
			break;
		}
		case R.id.test: {
			// /* 重新测试一次 */
			play.setEnabled(false);
			break;
		}
		default:
			break;
		}
	}
	private boolean isProgressStop = true;
	private Thread thread = null;
	/**
	 * 播放音乐和录音
	 * @param btn
	 */
	private void playVoice(final Button btn){
		if (!playState) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.reset();
			
			String url = "myheart.mp3";
			try
			{
				//设置播放音乐来源
				if(btn.getId() == R.id.button1){
					AssetFileDescriptor afd = getAssets().openFd(url);
					mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
					//mediaPlayer.setDataSource(getAmrPath());
				}else{
					//设置播放音乐来源
					mediaPlayer.setDataSource(mr.getTheRecordPath());
					//mediaPlayer.setDataSource(getAmrPath());
				}
				
				mediaPlayer.prepare();
				mediaPlayer.start();
				if(btn.getId() == R.id.button1){
					btn.setText("正在播放...");
				}else{
					btn.setText("正在播放录音...");
				}
				pb.setMax(mediaPlayer.getDuration());
				pb.setProgress(0);
				isProgressStop = true;
				thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						while(isProgressStop){
							int progress = mediaPlayer.getCurrentPosition();
							if(pb.getMax() > progress)
								pb.setProgress(progress);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
				thread.start();
				playState=true;
				//播放结束事件监听
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						isProgressStop = false;
						if(thread != null){
							thread.interrupt();
							thread = null;
						}
						if (playState) {
							if(btn.getId() == R.id.button1){
								btn.setText("播放");
							}else{
								btn.setText("播放录音");
							}
							playState=false;
						}
					}
				});
//				mediaPlayer.setOnInfoListener(listener);
			}
			catch (IllegalArgumentException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IllegalStateException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
				playState=false;
			}else {
				playState=false;
			}
			if(btn.getId() == R.id.button1){
				btn.setText("播放");
			}else{
				btn.setText("播放录音");
			}
		}
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isProgressStop = false;
		if(recordThread != null){
			recordThread.interrupt();
			recordThread = null;
		}
		if(thread != null){
			thread.interrupt();
			thread = null;
		}
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		FyLog.i(TAG, "onStop");
		if(!isSleepExit){
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
	//点击返回键填出提示窗口
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					editor.putBoolean("burn", false);
					editor.commit();
					ActivityManagers.clearActivity();
					finish();
				}
			});  
            isExit.setButton2("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // 显示对话框  
            isExit.show();  
        }  
        return false;  
    }  
	/**
	 * 判断录音文件是否存在，若存在，则删除
	 */
	void scanOldFile(){
		File file = new File(Environment  
                .getExternalStorageDirectory(), "my/voice.amr");
		if(file.exists()){
			file.delete();
		}
	}
	
	void showWarnToast(){
		Toast toast = new Toast(MediaRecordActivity.this);
		 LinearLayout linearLayout = new LinearLayout(MediaRecordActivity.this);
		 linearLayout.setOrientation(LinearLayout.VERTICAL); 
		 linearLayout.setPadding(20, 20, 20, 20);
		
		// 
		 ImageView imageView = new ImageView(MediaRecordActivity.this);
		 imageView.setImageResource(R.drawable.voice_to_short); //
		 
		 TextView mTv = new TextView(MediaRecordActivity.this);
		 mTv.setText("录音时间太短，请重新录音！");
		 mTv.setTextSize(14);
		 mTv.setTextColor(Color.WHITE);//
		 //mTv.setPadding(0, 10, 0, 0);
		 
		// 
		 linearLayout.addView(imageView);
		 linearLayout.addView(mTv);
		 linearLayout.setGravity(Gravity.CENTER);//
		 linearLayout.setBackgroundResource(R.drawable.record_bg);//
		 
		 toast.setView(linearLayout); 
		 toast.setGravity(Gravity.CENTER, 0,0);//
		 toast.show();				
	}
	
	//
	private String getAmrPath(){
		File file = new File(Environment  
                .getExternalStorageDirectory(), "my/voice.amr");
		return file.getAbsolutePath();
	}
	
	//
	void mythread(){
		recordThread = new Thread(ImgThread);
		recordThread.start();
	}
	
	//
	void setDialogImage(){
		if (voiceValue < 200.0) {
			dialog_img.setImageResource(R.drawable.record_animate_01);
		}else if (voiceValue > 200.0 && voiceValue < 400) {
			dialog_img.setImageResource(R.drawable.record_animate_02);
		}else if (voiceValue > 400.0 && voiceValue < 800) {
			dialog_img.setImageResource(R.drawable.record_animate_03);
		}else if (voiceValue > 800.0 && voiceValue < 1600) {
			dialog_img.setImageResource(R.drawable.record_animate_04);
		}else if (voiceValue > 1600.0 && voiceValue < 3200) {
			dialog_img.setImageResource(R.drawable.record_animate_05);
		}else if (voiceValue > 3200.0 && voiceValue < 5000) {
			dialog_img.setImageResource(R.drawable.record_animate_06);
		}else if (voiceValue > 5000.0 && voiceValue < 7000) {
			dialog_img.setImageResource(R.drawable.record_animate_07);
		}else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_08);
		}else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_09);
		}else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_10);
		}else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_11);
		}else if (voiceValue > 20000.0 && voiceValue < 24000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_12);
		}else if (voiceValue > 24000.0 && voiceValue < 28000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_13);
		}else if (voiceValue > 28000.0) {
			dialog_img.setImageResource(R.drawable.record_animate_14);
		}
	}
	
	//监听音量大小
	private Runnable ImgThread = new Runnable() {

		@Override
		public void run() {
			recodeTime = 0.0f;
			while (RECODE_STATE==RECORD_ING) {
				if (recodeTime >= MAX_TIME && MAX_TIME != 0) {
					imgHandle.sendEmptyMessage(0);
				}else{
				try {
					Thread.sleep(200);
					recodeTime += 0.2;
					Log.i("song","recodeTime"+recodeTime);
					if (RECODE_STATE == RECORD_ING) {
						voiceValue = mr.getAmplitude();
						imgHandle.sendEmptyMessage(1);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			}
		}

		Handler imgHandle = new Handler() {
			@Override
			public void handleMessage(Message msg) {
            
				switch (msg.what) {
				case 0:
					//
					if (RECODE_STATE == RECORD_ING) {
						RECODE_STATE = RECODE_ED;
						try {
								mr.stop();
								voiceValue = 0.0;
						} catch (IOException e) {
								e.printStackTrace();
						}
						if (recodeTime < 1.0) {
							showWarnToast();
							record.setText("重新录音");
							RECODE_STATE=RECORD_NO;
						}else{
						 record.setText(" ”");
						}
					}
					break;
				case 1:
					setDialogImage();
					break;
				default:
					break;
				}
				
			}
		};
	};
}
