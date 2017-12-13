package com.witsi.setting.hardwaretest;


import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;


public class GprsTest{
	String TAG = "WifiGprsTest";

	public static boolean isGprsTestSucc = false;
	WifiAdmin mWifiAdmin;
	String defaultaddr = "192.168.1.1";
	StringBuffer mStringBuffer = new StringBuffer();
	StringBuffer GPRS_ShowListBuffer = new StringBuffer();
	TelephonyManager mTelephonyManager;
	public void startTest(Context context){
		isGprsTestSucc = false;
		mWifiAdmin = new WifiAdmin(context);
		mWifiAdmin.CloseWifi();
		Log.i(TAG, "GprsTest startTest");
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//		mTelephonyManager.listen(new MyPhoneStateListener(),
//				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//		mTelephonyManager.listen(new TelLocationListener(),
//				PhoneStateListener.LISTEN_CELL_LOCATION);
		 new Handler().postDelayed(new Runnable() {// 这里设置自动开启数据对接
			 	@Override
				public void run() {
			 		
					try {
						/** 获得电话管理员 */
//						card_exit = true;
						TelephonyManager tm = mTelephonyManager;
						GPRS_ShowListBuffer.delete( 0, GPRS_ShowListBuffer.length());
						GPRS_ShowListBuffer.append("CCID:").append(tm.getSimSerialNumber())
								.append("\n");

						GPRS_ShowListBuffer.append("IMSI:").append(tm.getSubscriberId())
								.append("\n");
						GPRS_ShowListBuffer.append("IMEI:").append(tm.getDeviceId())
								.append("\n");
						GPRS_ShowListBuffer.append("num:").append(tm.getLine1Number())
								.append("\n");
						GPRS_ShowListBuffer.append("IMEI:")
								.append(tm.getDeviceSoftwareVersion()).append("\n");
						GPRS_ShowListBuffer.append("MCC+MNC：")
								.append(tm.getNetworkOperator()).append("\n");
						String Operator = tm.getNetworkOperator();

//						int mcc = Integer.valueOf(Operator.substring(0, 3));
						int mcc = Integer.valueOf(Operator.substring(0, 2));
						int mnc = Integer.valueOf(Operator.substring(3, 5));

						switch (tm.getPhoneType()) {
						case 0:
							GPRS_ShowListBuffer.append("TYPE:").append("PHONE_TYPE_NONE")
									.append("\n");
							break;
						case 1:
							GPRS_ShowListBuffer.append("TYPE:")
									.append("PHONE_TYPE_GSM GSM").append("\n");
							break;
						case 2:
							GPRS_ShowListBuffer.append("TYPE:")
									.append("PHONE_TYPE_CDMA CDMA").append("\n");
							break;
						default:
							break;
						}
						GPRS_ShowListBuffer.append("ISO:").append(tm.getSimCountryIso())
								.append("\n");
						GPRS_ShowListBuffer.append("MCC+MNC:").append(tm.getSimOperator())
								.append("\n");
						// 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
						Log.i(TAG, "isGprsTestSucc mPingThread");
				 		PingThread mPingThread = new PingThread(
									"www.baidu.com",1, handler);
						new Thread(mPingThread).start(); // ①
					} catch (Exception e) {
						e.printStackTrace();
						Log.i(TAG, "isGprsTestSucc = false 1");
					}
			 		

				}
			}, 3000);

		
	}
	
	Handler handler = new Handler() // ①
	{
		@Override
		public void handleMessage(Message msg) {
				// 如果消息来自于子线程
				// 将读取的内容追加显示在文本框中
				if (msg.what == 0x123) {
					isGprsTestSucc = true;
					Log.i(TAG, "isGprsTestSucc = true");
			}
		}
	};

	private class MyPhoneStateListener extends PhoneStateListener {
		/*
		 * Get the Signal strength from the provider, each tiome there is an
		 * update 从得到的信号强度,每个tiome供应商有更新
		 */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			signalStrength.getCdmaDbm();
		}

	};
	/**
	 * @author Administrator
	 * 获取基站信息
	 */
	public class TelLocationListener extends PhoneStateListener {
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
			try {
				GsmCellLocation loc = (GsmCellLocation) location;
				StringBuffer baseStationSB;
				baseStationSB = new StringBuffer();
				baseStationSB = baseStationSB.append("基站信息：").append("\n");
				baseStationSB = baseStationSB.append("CELLID：")
						.append(String.valueOf(loc.getCid())).append("\n");
				baseStationSB = baseStationSB.append("LAC：")
						.append(String.valueOf(loc.getLac())).append("\n");
				Log.i(TAG, baseStationSB.toString());
			} catch (Exception e) {

			}
		}
	}

	
}