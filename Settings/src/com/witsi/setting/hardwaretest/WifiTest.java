package com.witsi.setting.hardwaretest;


import java.util.List;

import com.witsi.debug.FyLog;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;


public class WifiTest{
	String TAG = "WifiGprsTest";

	public static boolean isWifiTestSucc = false;
	WifiAdmin mWifiAdmin;
	String defaultaddr = "192.168.1.1";
	Handler handler;
	public Context mContext;
	GprsTest mGprsTest;
	StringBuffer mStringBuffer = new StringBuffer();
	int waitTime;
	public void startTest(Context context){
		
		this.mContext = context;
		isWifiTestSucc = false;
		mWifiAdmin = new WifiAdmin(context);
		mWifiAdmin.OpenWifi();
		
		if(mWifiAdmin.isWifiConnected(context) == false){
			mWifiAdmin.StartScan();
			mWifiAdmin.getAndContNoPassWordNet();
		}
		
		handler = new Handler() // ��
		{
			@Override
			public void handleMessage(Message msg) {
				// �����Ϣ���������߳�
				// ����ȡ������׷����ʾ���ı�����
				if (msg.what == 0x123) {
					if (msg.obj.toString() != "") {
						 msg.obj.toString();
						 mStringBuffer.append("\n");
						 mStringBuffer.append((msg.obj.toString()));
						 WifiTestResult();
					}
				}else if (msg.what == 1)
				{
					mGprsTest.startTest(mContext);
				}
			}
		};
		
		if (mWifiAdmin.isWifiConnected(mContext) == true)
		{
			waitTime = 1000;
		}else
		{
			waitTime = 15000;
		}
		FyLog.e(TAG, "waitTime: " + waitTime);
		new Handler().postDelayed(new Runnable() {// ���������Զ��������ݶԽ�
			 	@Override
				public void run() {
			 		FyLog.v(TAG, "is wifi connected: " + mWifiAdmin.isWifiConnected(mContext));
					if (mWifiAdmin.isWifiConnected(mContext) == true) {
						getAllNetworkLisr();
						// if (isburning == false) {

						// �ͻ�������ClientThread�̴߳����������ӡ���ȡ���Է�����������
						mStringBuffer.delete( 0, mStringBuffer.toString().length());
				 		PingThread mPingThread = new PingThread(defaultaddr, 4, handler, 
				 				new PingThread.ThreadListener(){
	
							@Override
							public void onComplete() {
								// TODO Auto-generated method stub
								handler.sendEmptyMessage(1);
							}
							
						});
						new Thread(mPingThread).start(); // ��
					} else {
						FyLog.i(TAG,"isWifiTestSucc = false 1");
						isWifiTestSucc = false;
						mGprsTest.startTest(mContext);
					}
				}
			 	
			}, waitTime);
			
			
		
	}
	
	private void WifiTestResult()
	{
		String pingdata = mStringBuffer.toString();
		FyLog.e(TAG, "WifiTestResult: " + pingdata);
		char[] pingdatachar = new char[pingdata.length()];
		
		pingdata.getChars(0, pingdata.length(), pingdatachar, 0);
		for(int a = 0;a<pingdatachar.length;a++){
			
			if((pingdatachar[a] == '��'))
			{
				FyLog.i(TAG, "pingdata:"+"\n\n\n"+pingdata);
				FyLog.i(TAG,(a+3)+","+(pingdata.length()-2));
				String lostdata = pingdata.subSequence(a+2, pingdata.length()-2).toString();
				FyLog.i(TAG, "lostdata="+lostdata);
				FyLog.i(TAG, "lostdata="+" +4 errors, 100");
				
				if(lostdata.equals(" +4 errors, 100")){
					FyLog.i(TAG,"isWifiTestSucc = false 2");
				}else{
					FyLog.i(TAG,"isWifiTestSucc = true");
					isWifiTestSucc = true;
				}
				break;
			}
		}
		
	}
	
	
	private void getAllNetworkLisr() {// ��ȡwifi�б�
		FyLog.e(TAG, "��ȡwifi�б�");
		if (mWifiAdmin.Wifistate() == 1) {
			// Toast.makeText(WifiActivity.this, "����ɨ��wifi", Toast.LENGTH_SHORT)
			// .show();
			// ÿ�ε��ɨ��֮ǰ�����һ�ε�ɨ����
			StringBuffer stringBuffer = new StringBuffer();

			// ��ʼɨ������
			mWifiAdmin.StartScan();
			List<ScanResult> list = mWifiAdmin.GetWifiList();
			stringBuffer = stringBuffer.append("");
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					ScanResult mScanResult = (ScanResult) list.get(i);
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
				list = null;
				FyLog.e( TAG, "ɨ�赽������Wifi���磺\n"+ stringBuffer.toString());
				stringBuffer = stringBuffer.delete(0, stringBuffer.length());
			}
		} else {
			isWifiTestSucc = false;
		}
	}
	
}