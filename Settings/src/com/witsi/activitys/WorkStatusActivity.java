package com.witsi.activitys;

import java.util.Timer;
import java.util.TimerTask;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.AndroidDev;

import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WorkStatusActivity extends Activity implements OnClickListener
													, Callback{

	private String TAG = WorkStatusActivity.class.getSimpleName();
	private Context context = WorkStatusActivity.this;
	
	private LinearLayout ll_back;
	private RelativeLayout ll_sim;
	private ImageView iv_sim, iv_imei;
	private LinearLayout sim;
	private LinearLayout imei;
	private RelativeLayout ll_imei;
	
	private TextView battery_status;
	private TextView power;
//	private TextView imei;
	private TextView ip_address;
	private TextView wlan_address;
	private TextView bt_address;
	private TextView serial_number;
	private TextView open_time;
	private TextView network, net_db, network_type, service_status,
					 romaing, network_status, phone_num;
	private TextView imei1, imei_sv1;
	
	private TelephonyManager mTelephonyManager;
	private MyPhoneStateListener mMyPhoneStateListener;
	private WifiManager mWifiManager;
	private BluetoothAdapter mBluetoothAdapter;
	private Timer mTimer;
	private Handler handler;
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_device_workstatus_activity);
		initViews();
		
		
		initDatas();
	}

	private void initViews() {
		
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		((TextView)findViewById(R.id.tv)).setText("状态信息");
		findViewById(R.id.action_back).findViewById(R.id.sw).setVisibility(View.GONE);
		ll_back.setOnClickListener(this);
		
		battery_status = (TextView) findViewById(R.id.tv1);
		power = (TextView) findViewById(R.id.tv2);
		ip_address = (TextView) findViewById(R.id.tv3);
		wlan_address = (TextView) findViewById(R.id.tv4);
		bt_address = (TextView) findViewById(R.id.tv5);
		serial_number = (TextView) findViewById(R.id.tv6);
		open_time = (TextView) findViewById(R.id.tv7);
		ll_sim = (RelativeLayout) findViewById(R.id.tv8);
		ll_imei = (RelativeLayout) findViewById(R.id.tv9);
		iv_sim = (ImageView) findViewById(R.id.iv1);
		iv_imei = (ImageView) findViewById(R.id.iv2);
		sim = (LinearLayout) findViewById(R.id.sim);
			network = (TextView) sim.findViewById(R.id.tv81);
			net_db = (TextView) sim.findViewById(R.id.tv82);
			network_type = (TextView) sim.findViewById(R.id.tv83);
			service_status = (TextView) sim.findViewById(R.id.tv84);
			romaing = (TextView) sim.findViewById(R.id.tv85);
			network_status = (TextView) sim.findViewById(R.id.tv86);
			phone_num = (TextView) sim.findViewById(R.id.tv87);
//		imei = (TextView) findViewById(R.id.tv9);
		imei = (LinearLayout) findViewById(R.id.imei);
			imei1 = (TextView) imei.findViewById(R.id.tv91);
			imei_sv1 = (TextView) imei.findViewById(R.id.tv92);
		
		ll_sim.setOnClickListener(this);
		ll_imei.setOnClickListener(this);
	}

	private void initDatas() {
		
		handler = new Handler(this);
		Intent batteryStatus = registerReceiver(mReceiver, getFilter());
		// 是否在充电
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		// 怎么充
		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		if(!isCharging)
			battery_status.setText("未在充电");
		if(usbCharge && !acCharge)
			battery_status.setText("正在通过USB充电");
		if(!usbCharge && acCharge)
			battery_status.setText("通过适配器充电");
		//获取剩余电量
		//当前剩余电量
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		//电量最大值
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		//电量百分比
		float batteryPct = level / (float)scale;
		power.setText(""+batteryPct);
		
		/* 用电话管理员来监听电话信号强度 */
		mMyPhoneStateListener = new MyPhoneStateListener();
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mMyPhoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		try {
			/** 获得电话管理员 */
			TelephonyManager tm = (TelephonyManager) this
					.getSystemService(TELEPHONY_SERVICE);
			//设置通信商户
			String operator = tm.getNetworkOperator();
			FyLog.v(TAG, "the operator is: " + operator);
			if(operator.length() < 1){
				network.setText("未知");
			}else{
				int mnc = Integer.valueOf(operator.substring(3, 5));
				switch (mnc) {
				case 00:
					network.setText("中国移动");
					break;
				case 01:
					network.setText("中国联通");
					break;
				case 10:
					network.setText("中国电信");
					break;
				default:
					network.setText("未知");
					break;
				}
			}
			//设置网络类型
			/**         
			 * * 获取网络类型         
			 * *          
			 * * NETWORK_TYPE_CDMA 网络类型为CDMA         
			 * * NETWORK_TYPE_EDGE 网络类型为EDGE         
			 * * NETWORK_TYPE_EVDO_0 网络类型为EVDO0         
			 * * NETWORK_TYPE_EVDO_A 网络类型为EVDOA         
			 * * NETWORK_TYPE_GPRS 网络类型为GPRS         
			 * * NETWORK_TYPE_HSDPA 网络类型为HSDPA         
			 * * NETWORK_TYPE_HSPA 网络类型为HSPA         
			 * * NETWORK_TYPE_HSUPA 网络类型为HSUPA         
			 * * NETWORK_TYPE_UMTS 网络类型为UMTS         
			 * *          
			 * * 在中国，联通的3G为UMTS或HSDPA，
			 * 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA，
			 * 电信的3G为EVDO      
			 *    */
			switch (tm.getNetworkType()) {
			case TelephonyManager.NETWORK_TYPE_CDMA:
				network_type.setText("2G");
				break;
			case TelephonyManager.NETWORK_TYPE_EDGE:
				network_type.setText("2G");
				break;
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				network_type.setText("3G");
				break;
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				network_type.setText("3G");
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
				network_type.setText("2G");
				break;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				network_type.setText("3G");
				break;
			case TelephonyManager.NETWORK_TYPE_HSPA:
				network_type.setText("HSPA");
				break;
			case TelephonyManager.NETWORK_TYPE_UMTS:
				network_type.setText("3G");
				break;
			default:
				network_type.setText("未知");
				break;
			}
			/**SIM卡状态
			 * SIM_STATE_ABSENT SIM卡未找到
	         * SIM_STATE_NETWORK_LOCKED SIM卡网络被锁定，需要Network PIN解锁
	         * SIM_STATE_PIN_REQUIRED SIM卡PIN被锁定，需要User PIN解锁
	         * SIM_STATE_PUK_REQUIRED SIM卡PUK被锁定，需要User PUK解锁
	         * SIM_STATE_READY SIM卡可用
	         * SIM_STATE_UNKNOWN SIM卡未知
			 * */
			switch (tm.getSimState()) {
			case TelephonyManager.SIM_STATE_ABSENT:
				service_status.setText("SIM卡未找到");
				break;
			case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
				service_status.setText("SIM卡网络被锁定，需要Network PIN解锁");
				break;
			case TelephonyManager.SIM_STATE_PIN_REQUIRED:
				service_status.setText("SIM卡PIN被锁定，需要User PIN解锁");
				break;
			case TelephonyManager.SIM_STATE_PUK_REQUIRED:
				service_status.setText("SIM卡PUK被锁定，需要User PUK解锁");
				break;
			case TelephonyManager.SIM_STATE_READY:
				service_status.setText("SIM卡可用");
				break;
			case TelephonyManager.SIM_STATE_UNKNOWN:
				service_status.setText("SIM卡未知");
				break;

			default:
				break;
			}
			//漫游状态
			if(tm.isNetworkRoaming())
				romaing.setText("漫游");
			else
				romaing.setText("非漫游");
			//设置移动网络状态
			switch (tm.getDataState()) {
			case TelephonyManager.DATA_CONNECTED:
				network_status.setText("已连接");
				break;
			case TelephonyManager.DATA_CONNECTING:
				network_status.setText("正在连接");
				break;
			case TelephonyManager.DATA_DISCONNECTED:
				network_status.setText("已断开连接");
				break;
			case TelephonyManager.DATA_SUSPENDED:
				network_status.setText("暂停连接");
				break;

			default:
				break;
			}
			//设置本机电话号码
			if(tm.getLine1Number() == null 
					|| tm.getLine1Number().length() < 1)
				phone_num.setText("未知");
			else
				phone_num.setText(tm.getLine1Number());
			//设置IMEI
			if(tm.getDeviceId() == null 
					|| tm.getDeviceId().length() < 1)
				imei1.setText("未知");
			else
				imei1.setText(tm.getDeviceId());
			//设置IMEI SV
			if(tm.getDeviceSoftwareVersion() == null 
					|| tm.getDeviceSoftwareVersion().length() < 1)
				imei_sv1.setText("未知");
			else
				imei_sv1.setText(tm.getDeviceSoftwareVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		//IP地址
		String ip = AndroidDev.getLocalIpAddress(context);
		ip_address.setText(ip);
		//WLAN MAC地址
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		String wlan = mWifiManager.getConnectionInfo().getMacAddress();
		wlan_address.setText(wlan);
		FyLog.v(TAG, "the ip is: " + ip + " wlan: " + wlan);
		//蓝牙MAC地址
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			bt_address.setText("未知");
		}else{
			if(mBluetoothAdapter.isEnabled()){
				String mac = mBluetoothAdapter.getAddress();
				bt_address.setText(mac);
			}else
				bt_address.setText("蓝牙地址不可用");
			
		}
		//序列号
		serial_number.setText(Build.SERIAL);
		//更新开机时间
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				long time = SystemClock.elapsedRealtime();
				int hour = (int) (time/1000.0/3600.0);
				int mini = (int) (time/1000.0/60.0%60);
				int sec = (int) (time/1000.0%60);
				FyLog.i(TAG, "the hour is: " + hour
						+ "the mini is: " + mini
						+ "the sec is: " + sec);
				Message msg = handler.obtainMessage();
				msg.obj =  (""+ hour +":"+ mini +":"+ sec);
				handler.sendMessage(msg);
			}
		}, 0, 1000);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		open_time.setText((String)msg.obj);
		return false;
	}
	
	private IntentFilter getFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction("android.intent.action.TIME_SET");
		filter.addAction("android.intent.action.SIM_STATE_CHANGED");
		return filter;
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_back:
			finish();
			break;
		case R.id.tv8:
			if(sim.getVisibility() == View.GONE){
				sim.setVisibility(View.VISIBLE);
				iv_sim.setImageResource(R.drawable.down);
			}else{
				sim.setVisibility(View.GONE);
				iv_sim.setImageResource(R.drawable.right);
			}
			break;
		case R.id.tv9:
			if(imei.getVisibility() == View.GONE){
				imei.setVisibility(View.VISIBLE);
				iv_imei.setImageResource(R.drawable.down);
			}else{
				imei.setVisibility(View.GONE);
				iv_imei.setImageResource(R.drawable.right);
			}
			break;

		default:
			break;
		}
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			FyLog.i(TAG, "the action is: " + action);
			if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
				// 是否在充电
				int status = arg1.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
				                     status == BatteryManager.BATTERY_STATUS_FULL;
				// 怎么充
				int chargePlug = arg1.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
				boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
				if(!isCharging)
					battery_status.setText("未在充电");
				if(usbCharge && !acCharge)
					battery_status.setText("正在通过USB充电");
				if(!usbCharge && acCharge)
					battery_status.setText("通过适配器充电");
				//获取剩余电量
				//当前剩余电量
				int level = arg1.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				//电量最大值
				int scale = arg1.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				//电量百分比
				float batteryPct = level / (float)scale * 100;
				power.setText(""+batteryPct);
			}else if(action.equals("android.intent.action.SIM_STATE_CHANGED")){
				 TelephonyManager tm = (TelephonyManager)arg0.
						 getSystemService(Service.TELEPHONY_SERVICE);   
				 switch (tm.getSimState()) {
					case TelephonyManager.SIM_STATE_ABSENT:
						service_status.setText("SIM卡未找到");
						break;
					case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
						service_status.setText("SIM卡网络被锁定，需要Network PIN解锁");
						break;
					case TelephonyManager.SIM_STATE_PIN_REQUIRED:
						service_status.setText("SIM卡PIN被锁定，需要User PIN解锁");
						break;
					case TelephonyManager.SIM_STATE_PUK_REQUIRED:
						service_status.setText("SIM卡PUK被锁定，需要User PUK解锁");
						break;
					case TelephonyManager.SIM_STATE_READY:
						service_status.setText("SIM卡可用");
						break;
					case TelephonyManager.SIM_STATE_UNKNOWN:
						service_status.setText("SIM卡未知");
						break;

					default:
						break;
					}
			}
		}
	};
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		mTimer.cancel();
		mTimer = null;
		mTelephonyManager = null;
		mMyPhoneStateListener = null;
		mWifiManager = null;
		mBluetoothAdapter = null;
		handler = null;
	};
	
	/* 重写PhoneStateListener */
	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each tiome there is an
		 * update 从得到的信号强度,每个tiome供应商有更新
		 * ASU与dBm之间的关系是：dBm=-113+（2*ASU）。
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			if(signalStrength.isGsm()){
				//GSM
				if (signalStrength.getGsmSignalStrength() != 99) 
	                net_db.setText("" + (signalStrength.getGsmSignalStrength() * 2 - 113) + " dbm "
	                		+ signalStrength.getGsmSignalStrength() + " asu");
	            else 
	                net_db.setText("无信号");
			}else{
				//CDMA
				net_db.setText("" + signalStrength.getCdmaDbm() + " dbm "
						+ signalStrength.getCdmaEcio() + " asu");
			}
		}
		
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			// TODO Auto-generated method stub
			super.onServiceStateChanged(serviceState);
			/*
			　　ServiceState.STATE_EMERGENCY_ONLY 仅限紧急呼叫
			　　ServiceState.STATE_IN_SERVICE 信号正常
			　　ServiceState.STATE_OUT_OF_SERVICE 不在服务区
			　　ServiceState.STATE_POWER_OFF 断电
			　　*/ 
			switch (serviceState.getState()) {
			case ServiceState.STATE_EMERGENCY_ONLY:
				
				break;
			case ServiceState.STATE_IN_SERVICE:
				
				break;
			case ServiceState.STATE_OUT_OF_SERVICE:
				
				break;
			case ServiceState.STATE_POWER_OFF:
				
				break;
			default:
				break;
			}
		}

	};/* End of private Class */
	
}
