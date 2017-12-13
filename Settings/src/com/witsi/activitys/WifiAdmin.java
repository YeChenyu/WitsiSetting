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
	// ����WifiManager����
	private WifiManager mWifiManager;
	// ����WifiInfo����
	private WifiInfo mWifiInfo;
	// ɨ��������������б�
	private List<ScanResult> lstWifi;
	// ���������б�
	private List<WifiConfiguration> lstWifiConfiguration;
	// ����һ��WifiLock
	private WifiLock mWifiLock;
	// ���弸�ּ��ܷ�ʽ��һ����WEP��һ����WPA������û����������
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, 
		WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	// ������
	public WifiAdmin(Context context) {
		this.context = context;
		// ȡ��WifiManager����
		mWifiManager = (WifiManager) context.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		// ȡ��WifiInfo����
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	/**
	 * ���� SSID �� ���� ����WIFI
	 * @param ssid
	 * @param pwd
	 * @param type
	 */
	public void connect(String ssid, String pwd, WifiCipherType type){
		Thread thread = new Thread(new ConnectRunnable(ssid, pwd, type));
		thread.start();
	}
	/**
	 * WIFI״̬
	 * @return��1����  0�ر�
	 */
	public byte Wifistate() {
		if (mWifiManager.isWifiEnabled()) {
			return (1);
		} else
			return (0);
	}

	// ��WIFI
	public void OpenWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);

		}
	}

	// �ر�WIFI
	public void CloseWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	/**
	 * ���WIFI״̬
	 * @return
	 */
	public boolean checkState() {
		if (mWifiManager.isWifiEnabled()) {
			return true;
		} else {
			return false;
		}
	}

	// ����WifiLock
	public void AcquireWifiLock() {
		mWifiLock.acquire();
	}

	// ����WifiLock
	public void ReleaseWifiLock() {
		// �ж�ʱ������
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// ����һ��WifiLock
	public void CreatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	// �õ����úõ�����
	public List<WifiConfiguration> GetConfiguration() {
		if(lstWifiConfiguration != null)
			return lstWifiConfiguration;
		else
			return new ArrayList<WifiConfiguration>();
	}

	// ָ�����úõ������������
	public void ConnectConfiguration(int index) {
		// �����������úõ�������������
		if (index > lstWifiConfiguration.size()) {
			return;
		}
		// �������úõ�ָ��ID������
		mWifiManager.enableNetwork(lstWifiConfiguration.get(index).networkId,
				true);
	}

	public void StartScan() {
		mWifiManager.startScan();
		if(lstWifi != null) lstWifi.clear();
		if(lstWifiConfiguration != null) lstWifiConfiguration.clear();
		// �õ�ɨ����
		lstWifi = mWifiManager.getScanResults();
		// �õ����úõ���������
		lstWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	// �õ������б�
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
	//���ӿ�������
	public void ContNoPassWordNet(String SSID){
		if (isWifiConnected(context) == true) {
			if(D)Log.i("wifi", "wifi������");
		} else {
			if(D)Log.i("wifi", "wifiδ����");
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
	// ��ȡ��������������
	public void getAndContNoPassWordNet() {
		if (isWifiConnected(context) == true) {
			Log.i("wifi", "wifi������");
		} else {
			Log.i("wifi", "wifiδ����");
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
			
				Log.i("wifi", "�ҵ�WIFI_TEST������");
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

	// �鿴ɨ����
	public StringBuilder LookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lstWifi.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// ��ScanResult��Ϣת����һ���ַ�����
			// ���аѰ�����BSSID��SSID��capabilities��frequency��level
			stringBuilder.append((lstWifi.get(i)).toString());
			stringBuilder.append("\n");
		}
		return stringBuilder;
	}

	// �õ�MAC��ַ
	public String GetMacAddress() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// �õ�������BSSID
	public String GetBSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}
	// �õ�������SSID
	public String GetSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// �õ�IP��ַ
	public int GetIPAddress() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// �õ����ӵ�ID
	public int GetNetworkId() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// �õ�WifiInfo��������Ϣ��
	public String GetWifiInfo() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// ���һ�����粢����
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

	// �Ͽ�ָ��ID������
	public void DisconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();

	}
	//�Ͽ�����
	public void disconnected(){
		mWifiManager.disconnect();
	}
	
	public int getWifiState(){
		return mWifiManager.getWifiState();
	}
	//wifi�Ƿ�����
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
	 * ��������
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
			// ��wifi
			OpenWifi();
			// ����wifi������Ҫһ��ʱ��(�����ֻ��ϲ���һ����Ҫ1-3������)������Ҫ�ȵ�wifi
			// ״̬���WIFI_STATE_ENABLED��ʱ�����ִ����������
			while (mWifiManager.getWifiState() 
					== WifiManager.WIFI_STATE_ENABLING){
				try {
					// Ϊ�˱������һֱwhileѭ��������˯��100�����⡭��
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			//����SSID ���� ��ȫ���ʹ���WIFI����
			WifiConfiguration wifiConfig = createWifiInfo(ssid, password, type);
			if (wifiConfig == null) {
				Log.d(TAG, "wifiConfig is null!");
				return;
			}
			//�ж�WIFI�����Ƿ��Ѵ��� �����������Ƴ������������
			WifiConfiguration tempConfig = isWifiConfigExsits(ssid);
			if (tempConfig != null) {
				mWifiManager.removeNetwork(tempConfig.networkId);
			}
			//����µ���������
			int netID = mWifiManager.addNetwork(wifiConfig);
			//��������ӵ�WIFI����
			boolean enabled = mWifiManager.enableNetwork(netID, true);
			Log.d(TAG, "enableNetwork status enable=" + enabled);
			//���������
			boolean connected = mWifiManager.reconnect();
			Log.d(TAG, "enableNetwork connected=" + connected);
		}
	}
	
	// �鿴��ǰ�Ƿ�Ҳ���ù��������
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
	 * �����Ѹ�����Ϣ����WIFI����
	 * @param SSID ������
	 * @param Password ����
	 * @param Type ��ȫ����
	 * @return
	 */
	private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear(); //�������֧�ֵ������֤Э�顣
		config.allowedGroupCiphers.clear(); //������֧�ֵ�����
		config.allowedKeyManagement.clear();//��Կ����֧�ֵ�Э��
		config.allowedPairwiseCiphers.clear(); //�ɶԵļ���ΪWPA����֧�ֵ�������á�
		config.allowedProtocols.clear(); //֧�ֵ�Э��
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
			// �˴���Ҫ�޸ķ������Զ�����
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
