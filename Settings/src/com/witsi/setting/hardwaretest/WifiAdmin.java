package com.witsi.setting.hardwaretest;

import java.util.List;

import com.witsi.debug.FyLog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

public class WifiAdmin {
	
	private boolean D = false;
	private String TAG = "WifiAdmin";
	// 定义WifiManager对象
	private WifiManager mWifiManager;
	// 定义WifiInfo对象
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	// 网络连接列表
	private List<WifiConfiguration> mWifiConfiguration;
	// 定义一个WifiLock
	WifiLock mWifiLock;
	Context context;

	// 构造器
	public WifiAdmin(Context context) {
		this.context = context;
		// 取得WifiManager对象
		mWifiManager = (WifiManager) context.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	// WIFI状态
	public byte Wifistate() {
		if (mWifiManager.isWifiEnabled()) {
			return (1);
		} else
			return (0);
	}

	// 打开WIFI
	public void OpenWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);

		}
	}

	// 关闭WIFI
	public void CloseWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public boolean checkState() {
		if (mWifiManager.isWifiEnabled()) {
			return true;
		} else {
			return false;
		}
	}

	// 锁定WifiLock
	public void AcquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁WifiLock
	public void ReleaseWifiLock() {
		// 判断时候锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 创建一个WifiLock
	public void CreatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	// 得到配置好的网络
	public List<WifiConfiguration> GetConfiguration() {
		return mWifiConfiguration;
	}

	// 指定配置好的网络进行连接
	public void ConnectConfiguration(int index) {
		// 索引大于配置好的网络索引返回
		if (index > mWifiConfiguration.size()) {
			return;
		}
		// 连接配置好的指定ID的网络
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}

	public void StartScan() {
		mWifiManager.startScan();
		// 得到扫描结果
		mWifiList = mWifiManager.getScanResults();
		// 得到配置好的网络连接
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	// 得到网络列表
	public List<ScanResult> GetWifiList() {
		return mWifiList;
	}
	//连接开放网络
	public void ContNoPassWordNet(String SSID){
		if (isWifiConnected(context) == true) {
			if(D)FyLog.d(TAG, "wifi已连接");
		} else {
			if(D)FyLog.d(TAG, "wifi未连接");
		}
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + SSID + "\"";
		config.hiddenSSID = true;
		config.allowedAuthAlgorithms
				.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.allowedGroupCiphers
				.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		config.status = WifiConfiguration.Status.ENABLED;
		AddNetwork(config);
	}
	// 获取连接无密码网络
	public void getAndContNoPassWordNet() {
		if (isWifiConnected(context) == true) {
			if(D)FyLog.d(TAG, "wifi已连接");
		} else {
			if(D)FyLog.d(TAG, "wifi未连接");
		}
		if(mWifiList == null)
			return;
		for (int i = 0; i < mWifiList.size(); i++) {
			if(D)FyLog.i(TAG, "name:" + mWifiList.get(i).SSID + "    "
					+ "_______type:" + mWifiList.get(i).toString());
			// FyLog.i("wifi",
			// "name:"+mWifiList.get(i).SSID+"    "+"type:"+mWifiList.get(i).capabilities);
			if (mWifiList.get(i).capabilities.equals("[WPS][ESS]")
					) {
				if(D)FyLog.i(TAG, "找到无密码的网络");
				WifiConfiguration config = new WifiConfiguration();
				config.SSID = "\"" + mWifiList.get(i).SSID + "\"";
				config.hiddenSSID = true;
				config.allowedAuthAlgorithms
						.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.TKIP);
				config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.status = WifiConfiguration.Status.ENABLED;
				AddNetwork(config);
				break;
				
			}
		}
	}

	// 查看扫描结果
	public StringBuilder LookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("\n");
		}
		return stringBuilder;
	}

	// 得到MAC地址
	public String GetMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String GetBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}
	// 得到接入点的SSID
	public String GetSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// 得到IP地址
	public int GetIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	public int GetNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到WifiInfo的所有信息包
	public String GetWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}
	// 得到WifiInfo的所有信息包
	public String GetWifiLevel() {
		return (mWifiInfo == null) ? "无" : ""+mWifiInfo.getRssi()+"dbm";
	}

	// 添加一个网络并连接
	public void AddNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		mWifiManager.enableNetwork(wcgID, true);
	}

	// 断开指定ID的网络
	public void DisconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();

	}
	//断开连接
	public void disconnected(){
		mWifiManager.disconnect();
	}
	
	//wifi是否连接
	public boolean isWifiConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isConnected();
			}
		}
		return false;
	}

}
