package com.witsi.setting.hardwaretest;

import com.witsi.setting1.R;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqMessage;
import android.witsi.arq.ArqReceiverListener;
import android.witsi.arq.ArqTextOpt;
import android.witsi.arq.ArqUpdate;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

public class DwnLoadActivity extends Activity implements OnClickListener{
	private static final String TAG = "DwnLoadActivity";
	
	private Button button_down_return;
	private Button button_down_ok;
	private Button button_down_false;
	// ϵͳ�ļ�·�� (OTA����)
	private CheckBox cbSys;
	private EditText edSys;

	// Ӧ���ļ�·��
	private CheckBox cbApp;
	private EditText edApp;
	
	// ��ͨ�ļ�·��
	private CheckBox cbText;
	private EditText edText;	
	
	// ��ʾ����״̬
	private TextView tvSta;
	
	// ��ʾ����
	private ProgressBar bar;	
	
	private Handler mHandler = new MyHandler(this);

	// ָʾ���ؽ����Ƿ�����
	private boolean downLoadThreadRunning = false;

	// ��дָʾ��
	private boolean downLoadSysNeeded = false;
	private boolean downLoadAppNeeded = false;
	private boolean downLoadTextNeeded = false;
	
	// ���س������
	private DownLoadThread downLoadThread;
	OTADownLoadCallable downLoadSysCallable = new OTADownLoadCallable(ArqUpdate.FILE_TYPE_SYSTEM);
	DownLoadCallableClass downLoadAppCallable = new DownLoadCallableClass(ArqUpdate.FILE_TYPE_APPLICATION);
	DownLoadCallableClass downLoadTextCallable = new DownLoadCallableClass(ArqUpdate.FILE_TYPE_NORMAL);

	// �����������
	private int updateResult = -1;
	
	private static final int UPDATE_RESULT_INIT = -1;
	private static final int UPDATE_RESULT_DONE = 0;
	private static final int UPDATE_RESULT_PARTITION_NOT_FOUND = 1;
	private static final int UPDATE_RESULT_WRONG_APPLICATION_TYPE = 2;
	private static final int UPDATE_RESULT_APPLICATION_NOT_EXIST = 3;
	private static final int UPDATE_RESULT_INVALID_PACKAGE = 4;	
	private static final int UPDATE_RESULT_UNKNOWN_ERROR = 5;
	private static final int MAX_FRAME_DATA_LEN = 1000;
	private SparseArray<String> typeNames = new SparseArray<String>();
	private SparseArray<EditText> edts = new SparseArray<EditText>();
	private SparseArray<String> updateMessages = new SparseArray<String>();
	
	// RDP�ӿڶ���
	ArqUpdate arq;
	// OTA��������
	ArqTextOpt arqOTA = null;
	
	public int flag_download = -1;
	Intent intent;
	ImageView led_image;
	View main = null;
	Bundle mybundle;
	boolean screen_sleep = false;
	boolean isburning = false;
	private Handler handler;
	boolean ifCloseActivity = true;
	public IntentFilter filterHome = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	
	/**********************************************************************************************/
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
		// ��������ѡ����
		mybundle = new Bundle();
		intent = getIntent();
		mybundle = (Bundle) intent.getExtras();
		if ((Boolean) intent.getSerializableExtra("flag_burn") == true)
			isburning = true;
		else
			isburning = false;
		if (isburning == true) {
			// ��װ���ء����ð�~
			main = getLayoutInflater().from(this).inflate(
					R.layout.hardware_led_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_download_activity);
		}
		registerReceiver(mReceiver,filterHome);
		
		button_down_return = (Button) this.findViewById(R.id.downGetBackToMain);
		button_down_ok = (Button) this.findViewById(R.id.down_test_state);
		button_down_false = (Button) this.findViewById(R.id.downFalse);
		button_down_return.setOnClickListener(DwnLoadActivity.this);
		button_down_ok.setOnClickListener(DwnLoadActivity.this);
		button_down_false.setOnClickListener(DwnLoadActivity.this);
		// �����ؼ�
		cbSys = (CheckBox) findViewById(R.id.sys_checkbox);
		edSys = (EditText) findViewById(R.id.sys_file_path);	
	
		cbApp = (CheckBox) findViewById(R.id.app_checkbox);
		edApp = (EditText) findViewById(R.id.app_file_path);
			
		cbText = (CheckBox) findViewById(R.id.text_checkbox);
		edText = (EditText) findViewById(R.id.text_file_path);

		tvSta = (TextView) findViewById(R.id.downlaod_status);		
		bar = (ProgressBar) findViewById(R.id.procbar);
		
		edSys.setText(R.string.download_system_file);
		edApp.setText(R.string.download_application_file);
		
		typeNames.put(ArqUpdate.FILE_TYPE_SYSTEM, "OTA�����ļ�");
		typeNames.put(ArqUpdate.FILE_TYPE_APPLICATION, "Ӧ�ó���");
		typeNames.put(ArqUpdate.FILE_TYPE_NORMAL, "��ͨ�ļ�");
		
		edts.put(ArqUpdate.FILE_TYPE_SYSTEM, edSys);
		edts.put(ArqUpdate.FILE_TYPE_APPLICATION, edApp);
		edts.put(ArqUpdate.FILE_TYPE_NORMAL, edText);
		
		updateMessages.put(UPDATE_RESULT_PARTITION_NOT_FOUND, "����δ�ҵ�");
		updateMessages.put(UPDATE_RESULT_WRONG_APPLICATION_TYPE, "�����Ӧ������");
		updateMessages.put(UPDATE_RESULT_APPLICATION_NOT_EXIST, "Ӧ�ò�����");
		updateMessages.put(UPDATE_RESULT_INVALID_PACKAGE, "�յ���Ч�Ľ���֡");
		updateMessages.put(UPDATE_RESULT_UNKNOWN_ERROR, "δ֪������֡����");		
		
		arq = new ArqUpdate(this);
		arqOTA = new ArqTextOpt(this);
		
		led_image = (ImageView) this.findViewById(R.id.led_image);
		if ((Boolean) intent.getSerializableExtra("light") == false) {
			led_image.setBackgroundResource(R.drawable.black);
			screen_sleep = true;
		} else
			screen_sleep = false;

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (isburning == true) {
					if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_version")) {
						intent = new Intent(DwnLoadActivity.this,
								VersionActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_wifi")) {
						intent = new Intent(DwnLoadActivity.this,
								WifiActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_gprs")) {
						intent = new Intent(DwnLoadActivity.this,
								GPRS_Activity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_tf")) {
						intent = new Intent(DwnLoadActivity.this, TF_Activity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_serialnumber")) {
						intent = new Intent(DwnLoadActivity.this,
								SerialNumberActivity.class);
					} else if ((Boolean) intent
							.getSerializableExtra("flag_checkbox_printer")) {
						intent = new Intent(DwnLoadActivity.this,
								PrinterActivity.class);
					} else {
						intent = new Intent(DwnLoadActivity.this,
								ResultTableActivity.class);
					}
					mybundle.putString("download", "ok");
					intent.putExtras(mybundle);
					startActivity(intent);
				}
			}
		}, 3000);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
	}
	protected void onPause() {  
        Log.i(TAG, "###### onPause called. ######");  
        this.finish();
        super.onPause();  
    }	
	@Override
	public void onClick(View ledClick) {
		if (screen_sleep == true) {
			led_image.setBackgroundResource(R.drawable.lucency);
			screen_sleep = false;
			toggleBrightness(this, 200);// ����
		} else {
			Intent intent;
			Bundle mybundle = new Bundle();
			intent = getIntent();
			mybundle = (Bundle) intent.getExtras();
			isburning = false;
			mybundle.putBoolean("flag_burn", false);
			switch (ledClick.getId()) {
			case R.id.downGetBackToMain: {
				ActivityManager.clearActivity();
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					ActivityManager.trunToSingleTestActivity(DwnLoadActivity.this);
				} else if ((Boolean) intent.getSerializableExtra("alltest") == true) {
					ActivityManager.trunToEntryActivity(DwnLoadActivity.this);
				} else {
					ActivityManager.trunToBurnStartActivity(DwnLoadActivity.this);
				}
				break;
			}
			case R.id.down_test_state: {
				flag_download = 1;
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					mybundle.putString("download", "ok");
					ActivityManager.trunToSingleTestActivity(DwnLoadActivity.this);
				} else {
					mybundle.putString("download", "ok");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(DwnLoadActivity.this);
				}
				break;
			}
			case R.id.downFalse: {
				flag_download = 0;
				if ((Boolean) intent.getSerializableExtra("singletest") == true) {
					mybundle.putString("download", "ng");
					ActivityManager.trunToSingleTestActivity(DwnLoadActivity.this);
				} else {
					mybundle.putString("download", "ng");
					ActivityManager.trunToNextActivity();
					ActivityManager.startNextActivity(DwnLoadActivity.this);
				}
				break;
			}
			default:
				break;
			}
		}

	}
	
	// ���ÿ�ʼ���ذ�ť������
	public void downLoadBtnOnClickHandler(View v) {
		/* �Ƿ��Ѿ����ļ�����д��, ���򷵻� */
		if(downLoadThreadRunning == true) { 
			Log.i(TAG, "Download thread already running. just return.");
			return;
		}
		
		downLoadSysNeeded = cbSys.isChecked();
		downLoadAppNeeded = cbApp.isChecked();
		downLoadTextNeeded = cbText.isChecked();
		
		// Clear display
		tvSta.setText("");
		//�Ƿ���Ҫ����OTA�ļ�
		// �Ƿ���Ҫ��д�ļ�
		if(downLoadSysNeeded || downLoadAppNeeded || downLoadTextNeeded) {
			downLoadThread = new DownLoadThread();
			downLoadThread.start();
		}	
	}
	
	// ����ϵͳ�ļ���ť�������� OTA ������
	public void importSysFileOnClickHandler(View v) 
	{
		Intent intent = new Intent(DwnLoadActivity.this, FileExplorerActivity.class);
		//������Ҫ��������ֵ��Activity�������������룺requestCode = 0
		startActivityForResult(intent, 0);
	}
	
	// ����Ӧ���ļ���ť������
	public void importAppFileOnClickHandler(View v) 
	{
		Intent intent = new Intent(DwnLoadActivity.this, FileExplorerActivity.class);
		//������Ҫ��������ֵ��Activity�������������룺requestCode = 1
		startActivityForResult(intent, 1);
	}
	
	// ������ͨ�ļ���ť������
	public void importTextFileOnClickHandler(View v) 
	{
		Intent intent = new Intent(DwnLoadActivity.this, FileExplorerActivity.class);
		//������Ҫ��������ֵ��Activity�������������룺requestCode = 2
		startActivityForResult(intent, 2);
	}

	// ��ȡ�ش���ֵ
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
		Log.i(TAG, "requestCode = " + requestCode + "; resultCode = " + resultCode);
		
		if(resultCode != RESULT_OK) {
			Log.e(TAG, "onActivityResult with resultCode != RESULT_OK");
			return;
		}
		
		String filePath = data.getStringExtra("path");
		Log.i(TAG, "filePath = " + filePath);
		switch(requestCode) { 
			case 0:  // ����ѡȡ��ϵͳ�ļ���OTA�ļ���            
	            edSys.setText(filePath);
	            break;  
			case 1: // ����ѡȡ��Ӧ���ļ��� 
	            edApp.setText(filePath);
	            break;  
			case 2: // ����ѡȡ����ͨ�ļ��� 
	            edText.setText(filePath);
	            break;  
			default:  
	            break; 
		}
	}

	/******************************** ��д���������,���ڿ���ϵͳ,Ӧ��,��ͨ�ļ������� ********************************/
	private class DownLoadThread extends Thread {
		int ret = -1;
		
		public void run() {		
			// Wait download to complete
			sendStageMessageToMain("�ȴ�����...");
			while(threadTestAndSet()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
	                Log.d(TAG, "Waiting didnt work!!");
	                e.printStackTrace();
				}
			}
			
			/* <<<<<<<<< DownLoad System bin file >>>>>>>>> */
			if(downLoadSysNeeded) {
				FutureTask<Integer> future = new FutureTask<Integer>(downLoadSysCallable);
				new Thread(future).start();
				try {
					ret = future.get().intValue();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ret != 0) {
					arq.releaseUpdateLock(); // unLOCK UART
					downLoadThreadRunning = false;
					return;
				}
			}
			
			//Lock UART
//			arq.getUpdateLock(); 
			
			/* <<<<<<<<< DownLoad application bin file >>>>>>>>> */
			if(downLoadAppNeeded) {
				FutureTask<Integer> future = new FutureTask<Integer>(downLoadAppCallable);
				new Thread(future).start();
				try {
					ret = future.get().intValue();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ret != 0) {
					arq.releaseUpdateLock(); // unLOCK UART
					downLoadThreadRunning = false;
					return;	
				}
			}
			
			/* <<<<<<<<< DownLoad Text file >>>>>>>>> */
			if(downLoadTextNeeded) {
				FutureTask<Integer> future = new FutureTask<Integer>(downLoadTextCallable);
				new Thread(future).start();
				try {
					ret = future.get().intValue();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(ret != 0) {
					arq.releaseUpdateLock(); // unLOCK UART
					downLoadThreadRunning = false;
					return;					
				}
			}
			
//			arq.releaseUpdateLock(); // unLOCK UART
			downLoadThreadRunning = false;
		}
	}
	/******************************************** �������� ******************************************/
	/*
	 * ��ժҪ��Ϣװ��Ϊ32�ֽڵ�sha256ֵ
	 * param: 
	 * 		data --- ժҪ��Ϣ����
	 * return:
	 * 		!= null: ������32�ֽ�sha256����
	 * 		= null: ʧ��
	 */
	private static byte[] Sha256Encode(byte[] data) {
		MessageDigest md;
		byte[] retBytes = null;
		
		if(data == null) {
			Log.e(TAG, "Bad Argument.");
			return null;
		}
		
		try {
			md = MessageDigest.getInstance("SHA-256");
			retBytes = md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return retBytes;
	}

	// ����2������ͬ��,����֮ǰ״ֵ̬:
	// true --- �Ѿ����߳���������,�ȴ�; 
	// false --- û���߳�����,��ǰ��������
	private synchronized boolean threadTestAndSet() 
	{
		if(downLoadThreadRunning) {
			return true;
		} else {
			downLoadThreadRunning = true;
			return false;
		}
	} 
	/******************************** OTA �����߳� ********************************/
	class OTADownLoadCallable implements Callable<Integer> {
		
		private int downLoadType ;
		public OTADownLoadCallable(int type) {
			this.downLoadType = type;
		}
		
		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			int ret, fileLen, remail, sendLen;
			int frame_cnt = 0;
			int rec_length = 0;
			byte[] frame_id = new byte[2];
			
			try
			{
				//����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					//���ڴ洢�ļ�����
					byte[] buf = new byte[MAX_FRAME_DATA_LEN];
					
					// �鿴�Ƿ��Ѿ�ѡ����Ҫ��д��ϵͳ�ļ�
					String filePath = edts.get(downLoadType).getText().toString();
					if("".equals(filePath)){
						sendDialogMessageToMain("��ָ��Ҫ���ص�" + typeNames.get(downLoadType));
						return Integer.valueOf(-1);
					}
					
					// �����ļ�����
					File dwnFile = new File(filePath);				
					// ����ļ��Ƿ����
					if(!dwnFile.exists()) {
						sendDialogMessageToMain(filePath + " ������");
						return Integer.valueOf(-2);
					}
					/* STEP1: <<<<<<<<<<<<< OTA��������, ���ȴ�Ӧ��  >>>>>>>>>>>>*/
					ret = arqOTA.OTADownload();
					Log.v(TAG, "the OTADownload ret is: " + ret);
					sendStageMessageToMain("ota��������" + typeNames.get(downLoadType) + "...");
					if(ret != 0){
						Log.v(TAG, "��������ʧ��  ����ֵ: " + ret);
						sendStageMessageToMain("ota��������" + typeNames.get(downLoadType) + "ʧ�ܣ�");
						return Integer.valueOf(-3);
					}
					/* STEP2: <<<<<<<<<<<<<<<< �����ļ�����, ���ҽ��շ�������ȷ��  >>>>>>>>>>>>>>>>>> */
					// ������������ļ�����
					RandomAccessFile raf = new RandomAccessFile(dwnFile, "r");					
					sendStageMessageToMain("��ʼ����" + typeNames.get(downLoadType) + "...");
					// ��ȡ�ļ�����
					fileLen = (int)raf.length();
					sendLen = 0;      //�ѷ��ͳ���
					frame_cnt = 0;	  //֡���
					remail = fileLen; //�ļ�����
					raf.seek(0); // set to file start
					
					while(true){
						try {
							if((rec_length = raf.read(buf)) > 0){
								Log.v(TAG, "length is: " + rec_length + " the tmp is: " + " : " +
											ArqConverts.bytesToHexString(buf));
								ret = arqOTA.OTAWrite(++frame_cnt, buf, frame_id);
								Log.v(TAG, "the OTAWrite ret is: " + ret);
								if(ret != 0) {
									Log.e(TAG, "Send data failed. ret = " + ret + "; frameId = " + ArqConverts.bytesToHexString(frame_id));
									sendDialogMessageToMain("����" + typeNames.get(downLoadType) +"����ʧ��, framdId = " 
											+ ArqConverts.bytesToHexString(frame_id));
									return Integer.valueOf(-4);
								}
								sendLen += rec_length;
								/**************** �������ؽ��� ***************/
								sendProgressMessageToMain((float)sendLen / (fileLen));
								if(sendLen == remail)
									break;
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					/* Step3: <<<<<<<<<<<<<<<< OTA �ļ����ؽ���  >>>>>>>>>>>>>>>>>> */
					ret = arqOTA.OTADownloadEnd(frame_cnt);
					if(ret != 0) {
						Log.e(TAG, "Send OTADownloadFinish command failed. ret = " + ret);
						sendDialogMessageToMain("����" + typeNames.get(downLoadType) + "������������ʧ��");
						return Integer.valueOf(-5);
					}
					Log.v(TAG, "the OTADownloadFinish ret is: " + ret);
					sendStageMessageToMain("����" + typeNames.get(downLoadType) + "�ɹ�");
					raf.close();
					return Integer.valueOf(ret);
				} else { /* SD��δ���� */
					sendDialogMessageToMain("SD��δ����,���飡");
					return Integer.valueOf(-6);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Integer.valueOf(-7);				
			}						
		}
	}
	
	class DownLoadCallableClass implements Callable<Integer> {
		private int downLoadType = 0;

		public DownLoadCallableClass(int type) {
			this.downLoadType = type;
		}

		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			int ret, fileLen, len, remail, sendLen;
			int frameId = 0, i;
			
			final int checkPeriod = 100; // 100ms
			final int checkTimes = 200; // (200 * 100)ms = 20s	
				
			try
			{
				//����ֻ�������SD��������Ӧ�ó�����з���SD��Ȩ��
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					//���ڴ洢�ļ�����
					byte[] buf = new byte[ArqUpdate.MAX_FRAME_DATA_LEN];
					
					// �鿴�Ƿ��Ѿ�ѡ����Ҫ��д��ϵͳ�ļ�
					String filePath = edts.get(downLoadType).getText().toString();
					if("".equals(filePath)){
						sendDialogMessageToMain("��ָ��Ҫ���ص�" + typeNames.get(downLoadType));
						return Integer.valueOf(-1);
					}
					
					// �����ļ�����
					File dwnFile = new File(filePath);				
					// ����ļ��Ƿ����
					if(!dwnFile.exists()) {
						sendDialogMessageToMain(filePath + " ������");
						return Integer.valueOf(-2);
					}
					
					// ������������ļ�����
					RandomAccessFile raf = new RandomAccessFile(dwnFile, "r");					
					/* STEP1: <<<<<<<<<<<<< �����ļ�ժҪ��Ϣ, ���ȴ�Ӧ��  >>>>>>>>>>>>*/
					sendStageMessageToMain("��ʼ����" + typeNames.get(downLoadType) + "...");
					// ��ȡ�ļ�����
					fileLen = (int)raf.length();
					// ���ļ����ݶ�ȡ����ʱ��������
					byte[] tmpBuf = new byte[fileLen];
					// �ض�λ���ļ�ͷ	
					raf.seek(0); 			
					len = raf.read(tmpBuf, 0, fileLen);
					if(len != fileLen) {
						Log.e(TAG, "Read file: " + dwnFile.getName() + " failed. len = " + len);
						sendDialogMessageToMain("���ļ�:��" + dwnFile.getName() + " ʧ��");
						return Integer.valueOf(-3);
					}
					// �����ļ�sha256ֵ
					byte[] sha256 = Sha256Encode(tmpBuf);					
					ret = arq.sendTag(downLoadType, dwnFile.getName(), sha256);
					if(ret != 0) {
						Log.e(TAG, "Send down load file tag failed. ret = " + ret);
						sendDialogMessageToMain("����" + typeNames.get(downLoadType) + "TAGʧ��");
						return Integer.valueOf(-4);
					}
					
					/* STEP2: <<<<<<<<<<<<<<<< �����ļ�����, ���ҽ��շ�������ȷ��  >>>>>>>>>>>>>>>>>> */
					sendLen = 0;
					frameId = 0;
					remail = fileLen;
					raf.seek(0); // set to file start
			
					while(remail > 0) {
						len = (remail > ArqUpdate.MAX_FRAME_DATA_LEN) ? ArqUpdate.MAX_FRAME_DATA_LEN : remail;
						
						ret = raf.read(buf, 0, len);
						if(ret != len) {
							Log.e(TAG, "Read file: " + dwnFile.getName() + " failed. ret = " + ret + "; len = " + len);
							sendDialogMessageToMain("��" + typeNames.get(downLoadType) + "����");
							return Integer.valueOf(-5);
						}
						
						// is it the last frame ?
						if(remail - len == 0) {
							frameId = 0xffff;
						}
						
						ret = arq.sendData(frameId, buf, len);
						if(ret != frameId) {
							Log.e(TAG, "Send data failed. ret = " + ret + "; frameId = " + frameId);
							sendDialogMessageToMain("����" + typeNames.get(downLoadType) +"����ʧ��, framdId = " + frameId);
							return Integer.valueOf(-6);
						}
						
						remail -= len;
						sendLen += len;
						frameId++;
						/**************** �������ؽ��� ***************/
						sendProgressMessageToMain((float)sendLen / (fileLen));
					}
					
					sendStageMessageToMain("����" + typeNames.get(downLoadType) + "�ɹ�");
					
					if(downLoadType == ArqUpdate.FILE_TYPE_NORMAL) {
						return Integer.valueOf(0); // download normal file success
					}
					
					/* ע��T1���������� */
					arq.registerProgressListener((android.witsi.arq.ArqReceiverListener) mListener);
					
					/* Step3: <<<<<<<<<<<<<<<< ��ʼϵͳ�ļ�����  >>>>>>>>>>>>>>>>>> */
					sendStageMessageToMain("��ʼ����" + typeNames.get(downLoadType) + "...");
					ret = arq.startUpdate((downLoadType == ArqUpdate.FILE_TYPE_SYSTEM) ? 
							ArqUpdate.START_UPDATE_SYSTEM : ArqUpdate.START_UPDATE_APPLICATION);
					if(ret != 0) {
						Log.e(TAG, "Send start update command failed. ret = " + ret);
						sendDialogMessageToMain("����" + typeNames.get(downLoadType) + "��������ʧ��");
						arq.unregisterProgressListener();
						return Integer.valueOf(-7);
					}
					
					updateResult = UPDATE_RESULT_INIT; // ��ʼ��״̬
					// �ȴ�20S��ʱ,һ��T1���²��ᳬ��20S
					for(i = 0; i < checkTimes; i++) {
						// delay 100ms first					
						try { 
							Thread.sleep(checkPeriod);
						} catch (InterruptedException e) {
			                Log.d(TAG, "Waiting didnt work!!");
			                e.printStackTrace();
						}
											
						if(updateResult != UPDATE_RESULT_INIT){ 
							break;
						}
					}
					
					if((i == checkTimes) && (updateResult == UPDATE_RESULT_INIT)) {
						Log.e(TAG, "received T1 update progress result. Timeout > 20s");
						sendDialogMessageToMain("����" + typeNames.get(downLoadType) + "�������ȳ�ʱ");
						sendStageMessageToMain("����" + typeNames.get(downLoadType) + "ʧ��");
						ret = -8; // ��ʱ
					} else if(updateResult == UPDATE_RESULT_DONE){
						sendStageMessageToMain("����" + typeNames.get(downLoadType) + "�ɹ�");
						ret = 0;
					} else { // ����������ʾ��Ϣ
						sendDialogMessageToMain(typeNames.get(downLoadType) + updateMessages.get(updateResult));
						ret = -9;
					}
					
					// unregister Listener
					arq.unregisterProgressListener();
					raf.close();
					
					return Integer.valueOf(ret);
				} else { /* SD��δ���� */
					sendDialogMessageToMain("SD��δ����,���飡");
					return Integer.valueOf(-10);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Integer.valueOf(-11);				
			}						
		}
		
		// progress frame Listener
		ArqReceiverListener.Stub mListener = new ArqReceiverListener.Stub() {
			@Override
			public void actionPerformed(ArqMessage msg) throws RemoteException {
				// TODO Auto-generated method stub				
				int ret;						
				byte[] result = new byte[3];
				
				ret = msg.getCmdFrameData(result, 0, 3); // <2-Bytes respond> <1-Bytes Progress>
				if(ret != 3) {
					updateResult = UPDATE_RESULT_INVALID_PACKAGE;
					return;
				}
				
				// �������ؽ��
				ret = arq.getUpdateResult(result);
				if(ret >= 0) {
					if(ret == 100) {
						updateResult = UPDATE_RESULT_DONE;
					}
					sendProgressMessageToMain((float) ret / 100); // ����T1��д����
				} else {
					if(ret == ArqUpdate.RESULT_PARTITION_NOT_FOUND) {
						// Partition not found
						updateResult = UPDATE_RESULT_PARTITION_NOT_FOUND;	
					} else if(ret == ArqUpdate.RESULT_WRONG_APPLICATION_TYPE) {
						// wrong application type
						updateResult = UPDATE_RESULT_WRONG_APPLICATION_TYPE;
					} else if(ret == ArqUpdate.RESULT_APPLICATION_NOT_EXIST) {
						// application not exist
						updateResult = UPDATE_RESULT_APPLICATION_NOT_EXIST;
					} else {
						// error not defined
						updateResult = UPDATE_RESULT_UNKNOWN_ERROR;
					}
				}
			}
		};		
	}
	
	private void sendDialogMessageToMain(String text) {
    	Message msg = new Message();
    	msg.what = 0x000;
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("dialog", text);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
	}
	
	private void sendStageMessageToMain(String text) {
    	Message msg = new Message();
    	msg.what = 0x100;
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("stage", text);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
	}	
	
	private void sendProgressMessageToMain(float progress) {
    	Message msg = new Message();
    	msg.what = 0x200;
    	
    	Bundle bundle = new Bundle();
    	bundle.putFloat("progress", progress);

    	/* send message */
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
	}
	
	//ui���߳������и÷���,����: ��ʾ���ؽ���, ��ʾ������Ϣ, ���ؽ׶���Ϣ 
	private static class MyHandler extends Handler {
		private final WeakReference<DwnLoadActivity> mActivity;
		
		public MyHandler(DwnLoadActivity activity) {
			mActivity = new WeakReference<DwnLoadActivity> (activity);
		}

		public void handleMessage(Message msg) {
			DwnLoadActivity outer = mActivity.get();
			
			if(outer != null) {					
				if(msg.what == 0x000) { // ��ʾ������Ϣ
					String dlgMsg = msg.getData().getString("dialog");
					
		            new AlertDialog.Builder(outer).setTitle("������Ϣ")  
		            .setMessage(dlgMsg)  
		            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {  
		                @Override  
		                public void onClick(DialogInterface dialog, int which) {  
		                      
		                }  
		            }).show();  	            
					return;
				}
			
				if(msg.what == 0x100) { // ��ʾ�ļ���д�׶�
					String staMsg = msg.getData().getString("stage");
					String oldText = outer.tvSta.getText().toString();
					outer.tvSta.setText(oldText + staMsg + "\n");
					return;
				}
				
				if(msg.what == 0x200) { // display proccess bar
					float progressFloat = msg.getData().getFloat("progress");
					int progressInt = (int)(progressFloat * 100);
					outer.bar.setProgress(progressInt);
				}
			}
		}
	}
	
	
	/************************** �¼����������� ***************************/
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
            isExit.setButton("ȷ��", listener);  
            isExit.setButton2("ȡ��", listener);  
            // ��ʾ�Ի���  
            isExit.show();  
  
        }  
          
        return false;  
          
    }  
    /**�����Ի��������button����¼�*/  
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
    {  
        public void onClick(DialogInterface dialog, int which)  
        {  
            switch (which)  
            {  
            case AlertDialog.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����  
            	ActivityManager.clearActivity();
            	DwnLoadActivity.this.finish();  
                break;  
            case AlertDialog.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���  
                break;  
            default:  
                break;  
            }  
        }  
    };    
	//����Ϊ������ؼ������ʾ����	
	
	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				led_image.setBackgroundResource(R.drawable.lucency);
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

	//���ڽ���
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				ifCloseActivity =false;
				Log.i(TAG, "home������");
			}

		}

	};
}