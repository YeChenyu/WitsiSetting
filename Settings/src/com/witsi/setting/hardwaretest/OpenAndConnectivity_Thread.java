package com.witsi.setting.hardwaretest;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class OpenAndConnectivity_Thread implements Runnable {
	Context context;
	WifiAdmin mWifiAdmin;
	boolean if_start_wifi_open = true;

	public OpenAndConnectivity_Thread(Context context) {
		this.context = context;
		mWifiAdmin = new WifiAdmin(context);
	}

	@Override
	public void run() {
		if (mWifiAdmin.Wifistate() == 0) {
			mWifiAdmin.OpenWifi();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.i("wifi", "开始连接wifi");
					// TODO Auto-generated method stub
					mWifiAdmin.StartScan();
					mWifiAdmin.getAndContNoPassWordNet();
				}

			}, 2500);
		} else {
			if (mWifiAdmin.isWifiConnected(context) == false) {
				mWifiAdmin.StartScan();
				mWifiAdmin.getAndContNoPassWordNet();
			}
		}
		Log.i("wifi", "wifi线程已经连接好wifi");

	}
}
