package com.witsi.activitys;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;
import android.util.Log;

public class WifiAdmin {
	
	private String TAG = WifiAdmin.class.getSimpleName();
	private final static boolean D = false;
	private Context context = null;
	// 定义WifiManager对象
	private WifiManager mWifiManager;
	// 定义WifiInfo对象
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> lstWifi;
	// 网络连接列表
	private List<WifiConfiguration> lstWifiConfiguration;
	// 定义一个WifiLock
	private WifiLock mWifiLock;
	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, 
		WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	// 构造器
	public WifiAdmin(Context context) {
		this.context = context;
		// 取得WifiManager对象
		mWifiManager = (WifiManager) context.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	/**
	 * 根据 SSID 和 密码 连接WIFI
	 * @param ssid
	 * @param pwd
	 * @param type
	 */
	public void connect(String ssid, String pwd, WifiCipherType type){
		Thread thread = new Thread(new ConnectRunnable(ssid, pwd, type));
		thread.start();
	}
	/**
	 * WIFI状态
	 * @return：1开启  0关闭
	 */
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

	/**
	 * 检查WIFI状态
	 * @return
	 */
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
		if(lstWifiConfiguration != null)
			return lstWifiConfiguration;
		else
			return new ArrayList<WifiConfiguration>();
	}

	// 指定配置好的网络进行连接
	public void ConnectConfiguration(int index) {
		// 索引大于配置好的网络索引返回
		if (index > lstWifiConfiguration.size()) {
			return;
		}
		// 连接配置好的指定ID的网络
		mWifiManager.enableNetwork(lstWifiConfiguration.get(index).networkId,
				true);
	}

	public void StartScan() {
		mWifiManager.startScan();
		if(lstWifi != null) lstWifi.clear();
		if(lstWifiConfiguration != null) lstWifiConfiguration.clear();
		// 得到扫描结果
		lstWifi = mWifiManager.getScanResults();
		// 得到配置好的网络连接
		lstWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	// 得到网络列表
	public List<ScanResult> GetWifiList() {
		List<ScanResult> list = new ArrayList<ScanResult>();
		if(lstWifi != null && lstWifi.size() > 0){
			list.add(lstWifi.get(0));
			lstWifi.remove(0);
		}
		for(ScanResult result : lstWifi){
			boolean exist = false;
			for(ScanResult scan : list){
				if(result.SSID.equalsIgnoreCase(scan.SSID)){
					exist = true;
					break;
				}	
			}
			if(exist)
				continue;
			list.add(result);
		}
		return list;
	}
	//连接开放网络
	public void ContNoPassWordNet(String SSID){
		if (isWifiConnected(context) == true) {
			if(D)Log.i("wifi", "wifi已连接");
		} else {
			if(D)Log.i("wifi", "wifi未连接");
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
			Log.i("wifi", "wifi已连接");
		} else {
			Log.i("wifi", "wifi未连接");
		}
		if(lstWifi == null)
			return;
		for (int i = 0; i < lstWifi.size(); i++) {
			if(D)Log.i("wifi", "name:" + lstWifi.get(i).SSID + "    "
					+ "_______type:" + lstWifi.get(i).toString());
			// Log.i("wifi",
			// "name:"+lstWifi.get(i).SSID+"    "+"type:"+lstWifi.get(i).capabilities);
			if (lstWifi.get(i).SSID.equals("WIFI_TEST")
					) {
			
				Log.i("wifi", "找到WIFI_TEST的网络");
				WifiConfiguration config = new WifiConfiguration();
				config.SSID = "\"" + lstWifi.get(i).SSID + "\"";
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
		for (int i = 0; i < lstWifi.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			stringBuilder.append((lstWifi.get(i)).toString());
			stringBuilder.append("\n");
		}
		return stringBuilder;
	}

	// 得到MAC地址
	public String GetMacAddress() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String GetBSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}
	// 得到接入点的SSID
	public String GetSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// 得到IP地址
	public int GetIPAddress() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	public int GetNetworkId() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到WifiInfo的所有信息包
	public String GetWifiInfo() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// 添加一个网络并连接
	public void AddNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		mWifiManager.enableNetwork(wcgID, true);
	}
	
	public boolean clearWifiConfig(){
		List<WifiConfiguration> list = GetConfiguration();
		for(WifiConfiguration config : list){
			Log.d(TAG, "clear config="+ config.networkId);
			mWifiManager.disableNetwork(config.networkId);
			mWifiManager.removeNetwork(config.networkId);
		}
		return true;
	}
	
	public boolean unSaveNetwork(){
		int networkid = mWifiManager.getConnectionInfo().getNetworkId();
		return mWifiManager.removeNetwork(networkid);
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
	
	public int getWifiState(){
		return mWifiManager.getWifiState();
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
	/**
	 * 连接网络
	 * @author Administrator
	 *
	 */
	class ConnectRunnable implements Runnable {
		
		private String ssid;
		private String password;
		private WifiCipherType type;

		public ConnectRunnable(String ssid, String password, WifiCipherType type) {
			this.ssid = ssid;
			this.password = password;
			this.type = type;
		}

		@Override
		public void run() {
			// 打开wifi
			OpenWifi();
			// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
			// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
			while (mWifiManager.getWifiState() 
					== WifiManager.WIFI_STATE_ENABLING){
				try {
					// 为了避免程序一直while循环，让它睡个100毫秒检测……
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			//根据SSID 密码 安全类型创建WIFI配置
			WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
			if (wifiConfig == null) {
				Log.d(TAG, "wifiConfig is null!");
				return;
			}
			//判断WIFI配置是否已存在 若存在则先移除，再重新添加
			WifiConfiguration tempConfig = isWifiConfigExsits(ssid);
			if (tempConfig != null) {
				mWifiManager.removeNetwork(tempConfig.networkId);
			}
			//添加新的网络配置
			int netID = mWifiManager.addNetwork(wifiConfig);
			//启用新添加的WIFI配置
			boolean enabled = mWifiManager.enableNetwork(netID, true);
			Log.d(TAG, "enableNetwork status enable=" + enabled);
			//会从新连接
			boolean connected = mWifiManager.reconnect();
			Log.d(TAG, "enableNetwork connected=" + connected);
		}
	}
	
	// 查看以前是否也配置过这个网络
	private WifiConfiguration isWifiConfigExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	/**
	 * 根据已给的信息创建WIFI配置
	 * @param SSID 网络名
	 * @param Password 密码
	 * @param Type 安全类型
	 * @return
	 */
	private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear(); //这个配置支持的身份验证协议。
		config.allowedGroupCiphers.clear(); //组密码支持的配置
		config.allowedKeyManagement.clear();//密钥管理支持的协议
		config.allowedPairwiseCiphers.clear(); //成对的集合为WPA密码支持的这个配置。
		config.allowedProtocols.clear(); //支持的协议
		config.SSID = "\"" + SSID + "\"";
		// nopass
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
		config.wepKeys[0] = "";
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.wepTxKeyIndex = 0;
		}
		// wep
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			if (!TextUtils.isEmpty(Password)) {
				if (isHexWepKey(Password)) {
					config.wepKeys[0] = Password;
				} else {
					config.wepKeys[0] = "\"" + Password + "\"";
				}
			}
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		// wpa
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// 此处需要修改否则不能自动重联
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); 
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	private static boolean isHexWepKey(String wepKey) {
		final int len = wepKey.length();
		// WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
		if (len != 10 && len != 26 && len != 58) {
			return false;
		}
		return isHex(wepKey);
	}

	private static boolean isHex(String key) {
		for (int i = key.length() - 1; i >= 0; i--) {
			final char c = key.charAt(i);
			if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
				return false;
			}
		}
		return true;
	}



}
