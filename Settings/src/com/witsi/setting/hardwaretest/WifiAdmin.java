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
	// ����WifiManager����
	private WifiManager mWifiManager;
	// ����WifiInfo����
	private WifiInfo mWifiInfo;
	// ɨ��������������б�
	private List<ScanResult> mWifiList;
	// ���������б�
	private List<WifiConfiguration> mWifiConfiguration;
	// ����һ��WifiLock
	WifiLock mWifiLock;
	Context context;

	// ������
	public WifiAdmin(Context context) {
		this.context = context;
		// ȡ��WifiManager����
		mWifiManager = (WifiManager) context.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		// ȡ��WifiInfo����
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	// WIFI״̬
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
		return mWifiConfiguration;
	}

	// ָ�����úõ������������
	public void ConnectConfiguration(int index) {
		// �����������úõ�������������
		if (index > mWifiConfiguration.size()) {
			return;
		}
		// �������úõ�ָ��ID������
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}

	public void StartScan() {
		mWifiManager.startScan();
		// �õ�ɨ����
		mWifiList = mWifiManager.getScanResults();
		// �õ����úõ���������
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	// �õ������б�
	public List<ScanResult> GetWifiList() {
		return mWifiList;
	}
	//���ӿ�������
	public void ContNoPassWordNet(String SSID){
		if (isWifiConnected(context) == true) {
			if(D)FyLog.d(TAG, "wifi������");
		} else {
			if(D)FyLog.d(TAG, "wifiδ����");
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
			if(D)FyLog.d(TAG, "wifi������");
		} else {
			if(D)FyLog.d(TAG, "wifiδ����");
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
				if(D)FyLog.i(TAG, "�ҵ������������");
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

	// �鿴ɨ����
	public StringBuilder LookUpScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// ��ScanResult��Ϣת����һ���ַ�����
			// ���аѰ�����BSSID��SSID��capabilities��frequency��level
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("\n");
		}
		return stringBuilder;
	}

	// �õ�MAC��ַ
	public String GetMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// �õ�������BSSID
	public String GetBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}
	// �õ�������SSID
	public String GetSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// �õ�IP��ַ
	public int GetIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// �õ����ӵ�ID
	public int GetNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// �õ�WifiInfo��������Ϣ��
	public String GetWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}
	// �õ�WifiInfo��������Ϣ��
	public String GetWifiLevel() {
		return (mWifiInfo == null) ? "��" : ""+mWifiInfo.getRssi()+"dbm";
	}

	// ���һ�����粢����
	public void AddNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		mWifiManager.enableNetwork(wcgID, true);
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

}
