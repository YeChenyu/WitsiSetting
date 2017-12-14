package com.witsi.setting.hardwaretest;

import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.IntentFilter;

public class WifiActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = WifiActivity.class.getSimpleName();
	private final static boolean D = false;
	private Context context = WifiActivity.this;
	/************************* ������ *********************************/
	AlertDialog.Builder mAlertDialogBuilder;
	AlertDialog mAlertDialog;
	View main = null;
	TextView current_wifi;
	Button wifi_choose;
	Button wifi_set;
	ImageView wifi_image;
	private Button button_wifi_false;
	private Button button_wifi_return;
	private Button button_wifi_test_state;
	private Button button_wifi_net_text;
	public TextView text_wifi_state;
	private TextView text_wifi_list;
	private TextView wifi_network_info;
	private TextView wifi_ok;
	private TextView wifi_level;
	
	WifiManager mWifiManager;	
	private WifiAdmin wifiAdmin;
	private ScanResult mScanResult;
	private List<ScanResult> listResult;
	Handler handler;
// Handler timerHandler;
	ClientThread clientThread;
	PingThread mPingThread;
	ArrayList<String> lstWifi;
	
	boolean isburning = false;
	boolean screen_sleep = false;
	boolean just_wifi_flag = false;
	boolean timer_out = false;
	boolean wifi_test_over = false;
	boolean pingok = false;
	
	public int timerdata = 0;
	int k = 1;
	String receivedata;
	String defaultaddr = "192.168.1.1";
	String addr1 = "10.83.50.111";
	String addr2 = "www.baidu.com";
	String addr3 = "192.168.2.1";
	int defaultNum = 4;
	
	public BufferedReader br = null;
	private StringBuffer stringBuffer = new StringBuffer();
	
	
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
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����״̬��
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		if (config.getBoolean("alltest", false) == true) {
			if(WifiTest.isWifiTestSucc)
			{
				Intent intent = new Intent(context, BuzzerActivity.class);
				editor.putString("wifi", "ok");
				editor.commit();
				startActivity(intent);
				finish();
				return;
			}
		}

		isburning = config.getBoolean("flag_burn", false);
		if (isburning == true) {
			getLayoutInflater();
			// ��װ���ء����ð�~
			main = LayoutInflater.from(this).inflate(
					R.layout.hardware_wifi_activity, null);
			main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			setContentView(main);
		} else {
			setContentView(R.layout.hardware_wifi_activity);
		}
	
		/************************ ��Ա��ʼ���� *********************************/
		setMobileData(this, false);
		wifiAdmin = new WifiAdmin(context);
		wifi_set = (Button) this.findViewById(R.id.wifi_set);
		View v = findViewById(R.id.ll_tool);
		button_wifi_false = (Button) this.findViewById(R.id.fail);
		button_wifi_return = (Button) this.findViewById(R.id.back);
		button_wifi_test_state = (Button) this.findViewById(R.id.pass);
		button_wifi_net_text = (Button) this.findViewById(R.id.test);

		text_wifi_state = (TextView) this.findViewById(R.id.wifi_state);
		text_wifi_list = (TextView) this.findViewById(R.id.wifi_list);
		wifi_ok = (TextView) this.findViewById(R.id.wifi_ok);
		wifi_network_info = (TextView) this.findViewById(R.id.wifi_network_info);
		wifi_choose = (Button) this.findViewById(R.id.wifi_choose);
		current_wifi = (TextView)this.findViewById(R.id.current_wifi);
		wifi_level = (TextView)this.findViewById(R.id.wifi_level);
		
		/************************ �����¼���ʼ���� ******************************/
		wifi_choose.setOnClickListener(this);
		current_wifi.setText("��ǰ����wifiΪ\n" + "����" + wifiAdmin.GetSSID());
		wifi_level.setText("�ź�ǿ�ȣ�" + wifiAdmin.GetWifiLevel());		
		wifi_set.setOnClickListener(this);
		button_wifi_net_text.setOnClickListener(this);
		button_wifi_return.setOnClickListener(this);
		button_wifi_false.setOnClickListener(this);
		button_wifi_test_state.setOnClickListener(this);
		wifi_network_info.addTextChangedListener(netTestWatcher);// �ǿ����������ݶԽ�
		/************************* ��ʼ�� **********************************/

		wifi_image = (ImageView) this.findViewById(R.id.wifi_image);
		if (config.getBoolean("light", true) == false) {
			wifi_image.setBackgroundResource(R.drawable.bg_black);
			screen_sleep = true;
		} else
			screen_sleep = false;
		//ע�����wifi״̬
		registerReceiver(wifiChangeReceiver, getIntentFilter());
		if (wifiAdmin.Wifistate() == 1)
			text_wifi_state.setText("wifi�ѿ���");
		else{
			text_wifi_state.setText("wifi�ѹر�");
			wifiAdmin.OpenWifi();
		}

		FyLog.i("Wifi����", "����20�붨ʱ��");
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (wifi_test_over == false) {
					FyLog.i("Wifi����", "���ʱ�䵽");
					wifi_ok.setText("ʧ��");
					timer_out = true;
					isSleepExit = false;
					if (isburning == true) {
						editor.putString("wifi", "ng");
						editor.putInt("error", 1);
						editor.commit();
					}
				}
			}
		}, 20000);
		//��ʼ����
		handler = new Handler() // ��
		{
			@Override
			public void handleMessage(Message msg) {
				// �����Ϣ���������߳�
				// ����ȡ������׷����ʾ���ı�����
				if (msg.what == 0x123) {
					if (msg.obj.toString() != "") {
						receivedata = msg.obj.toString();
						wifi_network_info.append("\n");
						wifi_network_info.append((msg.obj.toString()));
					}
				}
			}
		};
		if (wifiAdmin.Wifistate() == 0) {
			wifiAdmin.OpenWifi();
		}else{
			//��ȡwifi�б� ��ʾ����߻�ɫ����
			getAllNetworkLisr();
		}
		text_wifi_state.setText("wifi�ѿ���");
		if (wifiAdmin.isWifiConnected(context) == true) {
			// if (isburning == false) {
			text_wifi_state.setText("wifi������");
			wifi_ok.setText("������");
			FyLog.i("wifi", "wifi����socket�߳�");
			// �ͻ�������ClientThread�̴߳����������ӡ���ȡ���Է�����������
			mPingThread = new PingThread(defaultaddr, 4, handler);
			new Thread(mPingThread).start(); // ��
			FyLog.i("wifi", "wifi��socket�߳̿���");
			// }
		} else {
			wifi_ok.setText("�ѶϿ�");
			text_wifi_state.setText("wifi�ѶϿ�");
			Toast.makeText(context, "��������wifi", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/* Called when the application resumes */
	@Override
	protected void onResume() {
		FyLog.i(TAG, "onResume");
		super.onResume();
		getAllNetworkLisr();
	}
	
	public IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction("android.net.wifi.STATE_CHANGE");
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		//����wifi�źŸı�
		filter.addAction("android.net.wifi.RSSI_CHANGED");
		return filter;
	}
	
	boolean ifok = false;
	private TextWatcher netTestWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
			boolean ifover = false;
			String pingdata = wifi_network_info.getText().toString();
			char[] pingdatachar = new char[pingdata.length()];
			
			pingdata.getChars(0, pingdata.length(), pingdatachar, 0);
			for(int a = 0;a<pingdatachar.length;a++){
				
				if((pingdatachar[a] == '��')){
					FyLog.i("pingdata", "pingdata:"+"\n\n\n"+pingdata);
					ifover = true;
					FyLog.i("pingdata",(a+3)+","+(pingdata.length()-2));
					String lostdata = pingdata.subSequence(a+2, pingdata.length()-2).toString();
					FyLog.i("pingdata", "lostdata="+lostdata);
					FyLog.i("pingdata", "lostdata="+" +4 errors, 100");
					
					for(int k =0;k<pingdatachar.length;k++){
						FyLog.i("pingdata", "k="+k+"   "+pingdatachar[k]);
					}
					if(lostdata.equals(" +4 errors, 100")){
						FyLog.i("pingdata","��ȫ����");
						wifi_ok.setText("ʧ��");	
						ifok = false;
					}else{
						FyLog.i("pingdata","û��ȫ����");
						wifi_ok.setText("ͨ��");
						ifok = true;
					}
					break;
				}
			}
			if(ifover == true){
				wifi_test_over = true;
				if(ifok == true){
					editor.putString("wifi", "ok");
				}else{
					editor.putString("wifi", "ng");
					editor.putInt("error", 1);
				}
				editor.commit();
				isSleepExit = false;
				//����ѡ��
				if (config.getBoolean("alltest", false) == true) {
					if(D)FyLog.i("wifi����", "�¸�����ѡȡBuzzer");
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(context);
				}
			}
		}
		@Override
		public void afterTextChanged(Editable arg0) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
	};


	/************************** ��Ա���������� ***************************/
	private void getAllNetworkLisr() {// ��ȡwifi�б�
		if (wifiAdmin.Wifistate() == 1) {
			// ÿ�ε��ɨ��֮ǰ�����һ�ε�ɨ����
			if (stringBuffer != null) {
				stringBuffer = new StringBuffer();
			}

			// ��ʼɨ������
			wifiAdmin.StartScan();
			listResult = wifiAdmin.GetWifiList();
			stringBuffer = stringBuffer.append("");
			if (listResult != null) {
				for (int i = 0; i < listResult.size(); i++) {
					mScanResult = (ScanResult) listResult.get(i);
					// �õ������SSID��the network name
					stringBuffer = stringBuffer
							.append("SSID: " + mScanResult.SSID).append("\n")
							.append("BSSID: " + mScanResult.BSSID).append("\n")
							.append("���ͣ� " + mScanResult.capabilities)
							.append("\n")
							.append("Ƶ�ʣ� " + mScanResult.frequency)
							.append("\n")
							.append("�ź�ǿ�ȣ�" + mScanResult.level + "dbm")
							.append("\n").append("\n\n");
				}
				text_wifi_list.setText("ɨ�赽������Wifi���磺\n"
						+ stringBuffer.toString());
				stringBuffer = stringBuffer.delete(0, stringBuffer.length());
			}
		} else {
			Toast.makeText(context, "���ȴ�wifi", Toast.LENGTH_SHORT)
					.show();
			stringBuffer = stringBuffer.delete(0, stringBuffer.length());
			text_wifi_list.setText("ɨ�赽������Wifi���磺\n" + stringBuffer.toString()
					+ "\n wifi�Ѿ��ر�");
		}
	}

	private void testNet() {// ���ز���
		if (wifiAdmin.Wifistate() == 1) {
			mPingThread = new PingThread(defaultaddr, defaultNum, handler);
			new Thread(mPingThread).start(); // ��
		} else {
			Toast.makeText(context, "���ȴ�wifi", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onClick(View wifiClick) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			wifi_image.setBackgroundResource(R.drawable.bg_transport);
			screen_sleep = false;
		} else {
			isburning = false;
			wifi_test_over = true;
			editor.putBoolean("flag_burn", false);
			editor.commit();
			isSleepExit = false;
			switch (wifiClick.getId()) {
			case R.id.pass: {
				if(ifok){
					if (config.getBoolean("singletest", false) == true) {
						editor.putString("wifi", "ok");
						editor.commit();
						ActivityManagers.trunToSingleTestActivity(context);
					} else if (config.getBoolean("alltest", false) == true) {
						editor.putString("wifi", "ok");
						editor.commit();
						ActivityManagers.trunToNextActivity();
						ActivityManagers.startNextActivity(context);
					}else {
						ActivityManagers.trunToBurnStartActivity(context);
					} 
				}
				break;
			}
			case R.id.fail: {
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("wifi", "ng");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(context);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("wifi", "ng");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(context);
				}else {
					ActivityManagers.trunToBurnStartActivity(context);
				} 
				break;
			}
			case R.id.back: {
				ActivityManagers.clearActivity();
				ActivityManagers.clearActivity();
				if (config.getBoolean("singletest", false) == true) {
					ActivityManagers.trunToSingleTestActivity(context);
				} else if (config.getBoolean("alltest", false) == true) {
					ActivityManagers.trunToEntryActivity(context);
				} else {
					ActivityManagers.trunToBurnStartActivity(context);
				} 
				break;
			}
			case R.id.test: {
				if(wifiAdmin.isWifiConnected(context)){
					wifi_ok.setText("������");
					timer_out = false;
					wifi_test_over = false;
					text_wifi_list.setText("");
					getAllNetworkLisr();
					//���²���
					testNet();
				}else{
					wifi_ok.setText("�ѶϿ�");
					text_wifi_state.setText("wifi�ѶϿ�");
					text_wifi_state.setBackgroundResource(R.color.red);
				}
				break;
			}
			case R.id.wifi_set: {
				// װ��/res/layout/login.xml���沼��
				final TableLayout loginForm1 = (TableLayout) getLayoutInflater()
						.inflate(R.layout.hardware_login, null);
				new AlertDialog.Builder(this)
				// ���öԻ���ı���
					.setTitle("������")
					// ���öԻ�����ʾ��View����
					.setView(loginForm1)
					// Ϊ�Ի�������һ����ȷ������ť
					.setPositiveButton("ȷ��", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							defaultaddr = ((EditText) loginForm1
									.findViewById(R.id.addr)).getText()
									.toString();
							defaultNum = Integer
									.valueOf(((EditText) loginForm1
											.findViewById(R.id.pagnum))
											.getText().toString());
						}
					})
					.setNegativeButton("ȡ��", null)
					.create()
					.show();
				break;
			}
			case R.id.wifi_choose: {
				final RelativeLayout loginForm2 = (RelativeLayout) getLayoutInflater()
						.inflate(R.layout.hardware_login_wifichoose, null);
				lstWifi = new ArrayList<String>();
				Button scan = (Button) loginForm2.findViewById(R.id.scan);
				Button refresh = (Button) loginForm2.findViewById(R.id.refresh);
				ListView lstWifiView = (ListView) loginForm2
						.findViewById(R.id.wifi_list_view);
				final ListAdapter mListAdapter = new ListAdapter(
						context, lstWifi);
				lstWifiView.setAdapter(mListAdapter);
				lstWifiView.setOnItemClickListener(mOnItemClickListener);
				wifiAdmin.StartScan();

				listResult = wifiAdmin.GetWifiList();
				lstWifi.clear();
				mListAdapter.notifyDataSetChanged();
				if (listResult != null) {
					for (int i = 0; i < listResult.size(); i++) {
						FyLog.i("login", "list:" + "\n"
								+ listResult.get(i).toString());
						mScanResult = (ScanResult) listResult.get(i);
						if (mScanResult.capabilities.equals("[ESS]")) {
							lstWifi.add("<���俪��>"+"SSID="+mScanResult.SSID);
							FyLog.i("login", "lstWifi:" + "SSID="
									+ mScanResult.SSID);
						}
						if (mScanResult.capabilities.equals("[WPS][ESS]")) {
							lstWifi.add("<�������>"+"SSID="+mScanResult.SSID);
							FyLog.i("login", "lstWifi:" + "SSID="
									+ mScanResult.SSID);
						}
					}
					mListAdapter.notifyDataSetChanged();
				}

				scan.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						//ɨ��WIFI
						wifiAdmin.StartScan();
						lstWifi.clear();
						mListAdapter.notifyDataSetChanged();
					}
				});
				refresh.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						//ˢ��WIFI�б�
						lstWifi.clear();
						listResult = wifiAdmin.GetWifiList();
						if (listResult != null) {
							for (int i = 0; i < listResult.size(); i++) {
								FyLog.i(TAG, "list:" + "\n"
										+ listResult.get(i).toString());
								mScanResult = (ScanResult) listResult.get(i);
								if (mScanResult.capabilities.equals("[ESS]")) {
									lstWifi.add(0, "<���俪��>"+"SSID="+mScanResult.SSID);
									FyLog.i(TAG, "<���俪��>:" + "SSID="
											+ mScanResult.SSID);
								}else if (mScanResult.capabilities.equals("[WPS][ESS]")) {
									lstWifi.add("<�������>"+"SSID="+mScanResult.SSID);
									FyLog.i(TAG, "<�������>:" + "SSID="
											+ mScanResult.SSID);
								}
							}
							mListAdapter.notifyDataSetChanged();
						}
					}
				});
				if(lstWifi != null){
					mAlertDialogBuilder = new AlertDialog.Builder(this);
					mAlertDialogBuilder
					// ���öԻ���ı���
						.setTitle("��ѡ�񿪷��ȵ�")
						.setView(loginForm2)
						// ���öԻ�����ʾ��View����
						.setView(loginForm2)
						// Ϊ�Ի�������һ����ȡ������ť
						.setNegativeButton("�رմ���", null)
						.setPositiveButton("�Ͽ�����", new OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								isDeviceDisCon = true;
								wifiAdmin.disconnected();
							}
						})
						.create();
					mAlertDialog = mAlertDialogBuilder.show();
				}else{
					new AlertDialog.Builder(this)
					// ���öԻ���ı���
						.setTitle("û���ҵ������ȵ�").setView(loginForm2)
						// ���öԻ�����ʾ��View����
						.setView(loginForm2)
						.setNeutralButton("�Ͽ�����", new OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								isDeviceDisCon = true;
								wifiAdmin.disconnected();
							}
						})
						// Ϊ�Ի�������һ����ȡ������ť
						.setNegativeButton("�رմ���", null)
						// ����������ʾ�Ի���
						.create().show();
				}
			break;
			}
			default:
				break;
			}
		}
	}

	private String wifiSSID = null;
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
				long arg3) {
			mAlertDialog.dismiss();
			FyLog.i(TAG, "��ʼ���ӣ�����");
			String wifiSSIDdata = lstWifi.get(position).toString();
			wifiSSID = wifiSSIDdata.substring(11, wifiSSIDdata.length());
			FyLog.i(TAG, "wifiSSID: " + wifiSSID);
			
			if(wifiAdmin.isWifiConnected(context) 
					&& wifiAdmin.GetSSID().equals("\"" + wifiSSID + "\"")){
				Toast.makeText(context, "��wifi�Ѿ�����", Toast.LENGTH_SHORT)
						.show();
			}else{
				if(wifiAdmin.isWifiConnected(context)){
					Toast.makeText(context, "wifi���ڶϿ�����", Toast.LENGTH_SHORT).show();
					wifiAdmin.disconnected();
				}else{
					wifiAdmin.ContNoPassWordNet(wifiSSID);
					wifi_ok.setText("������");
				}
			}
		}
	};
	
	private boolean isDeviceDisCon = false;
	private BroadcastReceiver wifiChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			String action = intent.getAction();
			wifiAdmin = new WifiAdmin(context);
			if(action.equals("android.net.wifi.RSSI_CHANGED")){
				wifi_level.setText("�ź�ǿ�ȣ�" + wifiAdmin.GetWifiLevel());
			//WIFI״̬�ı�
			}if(action.equals("android.net.wifi.STATE_CHANGE")){
				
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(info != null){
					if(info.getState() == NetworkInfo.State.CONNECTED){
						Log.e(TAG, "connect the wifi");
						text_wifi_state.setText("wifi������");
						text_wifi_state.setBackgroundResource(R.color.green);
						if(wifi_ok.getText().equals("������")
								||wifi_ok.getText().equals("ͨ��")){
						}else{
							wifi_ok.setText("������");
						}
						current_wifi.setText("��ǰ����wifiΪ\n"+"����"+wifiAdmin.GetSSID());
						wifi_level.setText("�ź�ǿ�ȣ�" + wifiAdmin.GetWifiLevel());
						FyLog.i("wifi", "��ǰ����wifiΪ\n"+"����"+wifiAdmin.GetSSID());
					}else if(info.getState() == NetworkInfo.State.CONNECTING){
						
					}else if(info.getState() == NetworkInfo.State.DISCONNECTED){
						Log.e(TAG, "disconnect");
						//��ʾwifi�ѶϿ�
						text_wifi_state.setText("wifi�ѶϿ�");
						text_wifi_state.setBackgroundResource(R.color.red);
						wifi_ok.setText("�ѶϿ�");
						current_wifi.setText("��ǰ����wifiΪ\n" + "����" + "��");
						wifi_level.setText("�ź�ǿ�ȣ�" + wifiAdmin.GetWifiLevel());
						Toast.makeText(context, "WIFI�ѶϿ�", Toast.LENGTH_SHORT).show();
						//���ֻ�ǶϿ������򲻼�������wifi
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								wifiAdmin = new WifiAdmin(context);
								wifiAdmin.ContNoPassWordNet(wifiSSID);
								wifi_ok.setText("������");
								current_wifi.setText("��ǰ����wifiΪ\n" + "����" + wifiAdmin.GetSSID());
								wifi_level.setText("�ź�ǿ�ȣ�" + wifiAdmin.GetWifiLevel());
								FyLog.i(TAG, "��ǰ����wifiΪ\n" + "����" + wifiSSID);
							}
						}, 1000);
					}
				}
			}
		}
	};
	/**
	 * �����ֻ����ƶ�����
	 */
	public static void setMobileData(Context pContext, boolean pBoolean) {

		try {

			ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			Class ownerClass = mConnectivityManager.getClass();

			Class[] argsClass = new Class[1];
			argsClass[0] = boolean.class;

			Method method = ownerClass.getMethod("setMobileDataEnabled",
					argsClass);

			method.invoke(mConnectivityManager, pBoolean);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�ƶ��������ô���: " + e.toString());
		}
	}

	/**
	 * �����ֻ��ƶ����ݵ�״̬
	 * 
	 * @param pContext
	 * @param arg
	 *            Ĭ����null
	 * @return true ���� false �ѶϿ�
	 */
	public static boolean getMobileDataState(Context pContext, Object[] arg) {

		try {

			ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			Class ownerClass = mConnectivityManager.getClass();

			Class[] argsClass = null;
			if (arg != null) {
				argsClass = new Class[1];
				argsClass[0] = arg.getClass();
			}

			Method method = ownerClass.getMethod("getMobileDataEnabled",
					argsClass);

			Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);

			return isOpen;

		} catch (Exception e) {
			// TODO: handle exception

			System.out.println("�õ��ƶ�����״̬����");
			return false;
		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
		unregisterReceiver(wifiChangeReceiver);
		clientThread = null;
		br = null;
		
		setMobileData(this, true);
		
		stringBuffer = null;
		wifiAdmin = null;
		mScanResult = null;
		
		text_wifi_list.setText("");
		text_wifi_state.setText("wifi�ѹر�");
		
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "����Stop״̬");
		if(config.getBoolean("alltest", false) == true){
			wifiAdmin.CloseWifi();
		}
		System.gc();
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

	// ����Ϊ������ؼ������ʾ����

	public boolean onTouchEvent(MotionEvent event) {
		if (screen_sleep == true) {
			toggleBrightness(this, 200);// ����
			switch (event.getAction()) {
			// ������Ļʱ��
			case MotionEvent.ACTION_DOWN:
				wifi_image.setBackgroundResource(R.drawable.bg_transport);
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
