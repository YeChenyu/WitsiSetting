package com.witsi.activitys;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.witsi.activitys.WifiAdmin.WifiCipherType;
import com.witsi.adapter.WifiLvAdapter;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.views.SlipButton;
import com.witsi.views.SlipButton.OnChangedListener;
import com.witsi.views.WifiConnectLayout;
import com.witsi.views.WifiDisconLayout;
import com.witsi.views.WifiInfoLayout;
import com.witsi.views.WifiInfoLayout.OnWifiClickListener;

import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WifiActivity extends Activity implements OnItemClickListener,
				OnChangedListener, OnItemLongClickListener, OnClickListener{

	private String TAG = WifiActivity.class.getSimpleName();
	private Context context = WifiActivity.this;
	
	private LinearLayout ll_back;
	private SlipButton sw;
	private GridView gv_wifi_list;
	private WifiLvAdapter adapter;
	private List<ScanResult> lstWifi;
	private TextView tv_show;
	
	private WifiAdmin wifiAdmin;
	private ScanResult mScanResult;
	private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		setContentView(R.layout.wifi_activity);
		
		initViews();
		
		initDatas();
	}

	private void initViews() {
		gv_wifi_list = (GridView) findViewById(R.id.gv_wifi_list);
		ll_back = (LinearLayout)(findViewById(R.id.action_back).findViewById(R.id.ll_back));
		((TextView)(findViewById(R.id.action_back).findViewById(R.id.tv))).setText("WI-FI");
		sw = (SlipButton) findViewById(R.id.action_back).findViewById(R.id.sw);
		tv_show = (TextView) findViewById(R.id.tv_show);
		
		sw.setOnChangedListener(this);
		gv_wifi_list.setOnItemClickListener(this);
		gv_wifi_list.setOnItemLongClickListener(this);
		ll_back.setOnClickListener(this);
	}

	private void initDatas() {
		wifiAdmin = new WifiAdmin(context);
		lstWifi = new ArrayList<ScanResult>();
		adapter = new WifiLvAdapter(context, wifiAdmin,  lstWifi);
		gv_wifi_list.setAdapter(adapter);
		//���wifi�Ѵ򿪣���ֱ����ʾ
		if(wifiAdmin.Wifistate() == 1){
			sw.setCheck(true);
			List<ScanResult> list = getAllNetworkLisr();
			if(list != null){
				for (ScanResult scanResult : list) {
					lstWifi.add(0, scanResult);
				}
				adapter.notifyDataSetChanged();
				tv_show.setVisibility(View.GONE);
			}
		}else{
			sw.setCheck(false);
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		registerReceiver(mReceiver, getIntentFilter());
		isUnregister = false;
		
		fresh_cnt = 1;
		fresh_first_cnt = 0;
	}
	
	public IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction("android.net.wifi.STATE_CHANGE");
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		return filter;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onItemClick(AdapterView<?> arg0, final View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		final ScanResult result = lstWifi.get(arg2);
		FyLog.d(TAG, "SSID: " + wifiAdmin.GetSSID() + " BSSID: " + wifiAdmin.GetBSSID());
		FyLog.d(TAG, "SSID: " + result.SSID + " BSSID: " + result.BSSID
				+ " capabilities: " + result.capabilities);
		
		if(wifiAdmin.GetSSID().contains(result.SSID)
//				&& wifiAdmin.GetBSSID().equals(result.BSSID)
				){
			//���������wifi
			if(wifiAdmin.isWifiConnected(context)){
				WifiInfoLayout layout = new WifiInfoLayout(context, result);
				layout.setOnWifiClickListener(new OnWifiClickListener() {
					@Override
					public void onCancelSaveConfig() {
						// TODO Auto-generated method stub
						wifiAdmin.unSaveNetwork();
						dialog.dismiss();
					}
					@Override
					public void confirm() {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
				showMyDialog(layout.v, 0);
			//�����ͨδ����wifi
			}else{
				final WifiDisconLayout layout = new WifiDisconLayout(context,
						result);
				layout.setOnWifiClickListener(new WifiDisconLayout.OnClickNoPassListener() {
					@Override
					public void onConnectNoPwdWifi() {
						// TODO Auto-generated method stub
						//��ʾ������...
						adapter.setConnectingSsid(result.SSID);
						wifiAdmin.connect(result.SSID, layout.getPassWord(), 
								WifiCipherType.WIFICIPHER_WPA);
						dialog.dismiss();
					}
					@Override
					public void confirm() {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
				 showMyDialog(layout.v, 0);
			}
		//����ѱ��桢����δ���ӵ�wifi
		}else if(((TextView)arg1.findViewById(R.id.tv3)).getText().toString().contains("�ѱ���")){
			WifiConnectLayout layout = new WifiConnectLayout(context, result);
			layout.setOnWifiClickListener(new WifiConnectLayout.OnWifiClickListener() {
				
				@Override
				public void onCancelSaveConfig() {
					// TODO Auto-generated method stub
					List<WifiConfiguration> lst = wifiAdmin.GetConfiguration();
					for (WifiConfiguration wifi : lst) {
						if(wifi.SSID.equals("\"" + result.SSID + "\"")){
							FyLog.i(TAG, "SSID: " + result.SSID + " SSID: " + wifi.SSID);
							wifiAdmin.ConnectConfiguration(lst.indexOf(wifi));
							adapter.setConnectingSsid(result.SSID);
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									adapter.notifyDataSetChanged();
									tv_show.setVisibility(View.GONE);
								}
							}, 500);
							break;
						}
					}
					dialog.dismiss();
				}
				@Override
				public void confirm() {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			showMyDialog(layout.v, 0);
		//���δ���桢����δ���ӵ�wifi
		}else{
			final WifiDisconLayout layout = new WifiDisconLayout(context,
					result);
			layout.setOnWifiClickListener(new WifiDisconLayout.OnClickNoPassListener() {
				
				@Override
				public void onConnectNoPwdWifi() {
					// TODO Auto-generated method stub
					wifiAdmin.connect(result.SSID, layout.getPassWord(), 
							WifiCipherType.WIFICIPHER_WPA);
					adapter.setConnectingSsid(result.SSID);
					dialog.dismiss();
				}
				@Override
				public void confirm() {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			showMyDialog(layout.v, 0);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		if(false){
			final ScanResult result = lstWifi.get(arg2);
			FyLog.d(TAG, "SSID: " + wifiAdmin.GetSSID() + " BSSID: " + wifiAdmin.GetBSSID());
			FyLog.d(TAG, "SSID: " + result.SSID + " BSSID: " + result.BSSID
					+ " capabilities: " + result.capabilities);
//			if(wifiAdmin.GetSSID().contains(result.SSID)){
//				new MyAlertDialog.Builder(context)
//				.setTitle("MERCURY_witsi@1")
//				.setPositiveButton("ȡ����������", null)
//				.setNegativeButton("�޸�����", null)
//				.create()
//				.show();
//			}else{
//				new MyAlertDialog.Builder(context)
//				.setTitle("MERCURY_witsi@1")
//				.setPositiveButton("���ӵ�����", null)
//				.create()
//				.show();
//			}
		}
		return true;
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		WifiActivity.this.finish();
	}

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			FyLog.d(TAG, "the wifi state is: " + wifiAdmin.Wifistate());
			List<ScanResult> list = getAllNetworkLisr();
			List<WifiConfiguration> lstConfig = wifiAdmin.GetConfiguration();
			if(list != null){
				lstWifi.clear();
				timer.cancel();
				for (ScanResult scanResult : list) {
					if(!lstWifi.contains(scanResult))
					lstWifi.add(0, scanResult);
				}
				FyLog.d(TAG, "the list size is: " + list.size());
				getTheWifiList(list, lstConfig);
				gv_wifi_list.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				tv_show.setVisibility(View.GONE);
			}
		}
	};
	
	private Thread mClearThread = null;
	@Override
	public void onChanged(boolean checkState) {
		// TODO Auto-generated method stub
		boolean isSoftCheck = false;
		if(!isSoftCheck){
		if(checkState){
			Log.d(TAG,  "open the wifi");
			if (wifiAdmin.Wifistate() == 0) {
				if(isUnregister){
					context.registerReceiver(mReceiver, getIntentFilter());
					isUnregister = false;
				}
				fresh_cnt = 1;
				fresh_first_cnt = 0;
				wifiAdmin.OpenWifi();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						FyLog.d(TAG, "the timer is working");
						if(wifiAdmin.Wifistate() == 1){
							myHandler.sendEmptyMessage(0);
						}
					}
				}, 0, 1000);
			}
			mClearThread = new Thread(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					// TODO Auto-generated method stub
					while(wifiAdmin.GetConfiguration().size() == 0){
						try {
//							Log.i(TAG, "clear config...");
							Thread.sleep(200);
							if(wifiAdmin.GetConfiguration().size() > 0){
								wifiAdmin.StartScan();
								wifiAdmin.clearWifiConfig();
								break;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(!isUnregister){
						unregisterReceiver(mReceiver);
						isUnregister = true;
					}
					registerReceiver(mReceiver, getIntentFilter());
					isUnregister = false;
					myHandler.sendEmptyMessage(0);
				}
				
			});
			mClearThread.start();
			gv_wifi_list.setVisibility(View.VISIBLE);
		}else {
			Log.d(TAG,  "close the wifi");
			wifiAdmin.StartScan();
			wifiAdmin.clearWifiConfig();
			if(mClearThread != null){
				mClearThread.interrupt();
				mClearThread = null;
			}
			new Thread(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						while(wifiAdmin.isWifiConnected(context))
							Thread.sleep(500);
						
						if(wifiAdmin.Wifistate() == 1){
							wifiAdmin.clearWifiConfig();
							wifiAdmin.CloseWifi();
						}
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								FyLog.d(TAG, "close the wifi");
								lstWifi.clear();
								adapter.notifyDataSetChanged();
								gv_wifi_list.setVisibility(View.INVISIBLE);
								tv_show.setVisibility(View.VISIBLE);
							}
						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		}
		}
	}
	
	private int fresh_cnt = 1;
	public int fresh_first_cnt = 0;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		private int first_cnt = 0;
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			String action = arg1.getAction();
//			Log.i(TAG, "action: " + arg1.getAction());
			if(action.equals("android.net.wifi.STATE_CHANGE")){
				
				NetworkInfo info = arg1.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(info != null){
					if(info.getState() == NetworkInfo.State.CONNECTED){
						List<ScanResult> list = getAllNetworkLisr();
						List<WifiConfiguration> lstConfig = wifiAdmin.GetConfiguration();
						getTheWifiList(list, lstConfig);
						gv_wifi_list.setAdapter(adapter);
						Log.e(TAG, "CONNECTED the wifi");
						adapter.notifyDataSetChanged();
					}else if(info.getState() == NetworkInfo.State.CONNECTING){
						Log.e(TAG, "CONNECTING the wifi");
						adapter.setConnectingSsid(wifiAdmin.GetSSID());
						adapter.notifyDataSetChanged();
					}else if(info.getState() == NetworkInfo.State.DISCONNECTED){
//						Log.e(TAG, "DISCONNECTED the wifi");
						if(!isUnregister){
							unregisterReceiver(mReceiver);
							isUnregister = true;
						}
						registerReceiver(mReceiver, getIntentFilter());
						isUnregister = false;
					}else{
						Log.e(TAG, "the wifi info.getState()="+ info.getState());
					}
				}
			}else if(action.equals("android.net.wifi.WIFI_STATE_CHANGED")){
				int state = arg1.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
				if(state == WifiManager.WIFI_STATE_ENABLED){
					FyLog.e(TAG, "the wifi is enable");
					tv_show.setVisibility(View.GONE);
				}else if(state == WifiManager.WIFI_STATE_ENABLING){
					FyLog.e(TAG, "the wifi is enabling");
					tv_show.setText("���ڴ�WIFI...");
				}else if(state == WifiManager.WIFI_STATE_DISABLING){
					FyLog.e(TAG, "the wifi is disabling");
					tv_show.setText("����WIFI�������豸�ſ����������硣");
					tv_show.setVisibility(View.VISIBLE);
				}else if(state == WifiManager.WIFI_STATE_DISABLED){
					FyLog.e(TAG, "the wifi is disable");
					if(!isUnregister){
						unregisterReceiver(mReceiver);
						isUnregister = true;
					}
				}
			}else if(action.equals("android.net.wifi.SCAN_RESULTS")){
				List<ScanResult> list = getAllNetworkLisr();
				List<WifiConfiguration> lstConfig = wifiAdmin.GetConfiguration();
				if(fresh_first_cnt++ % 3 == 0){
					fresh_first_cnt = 0;
					first_cnt++;
					if(first_cnt < 3){
						if(list != null){
							fresh_cnt = 1;
							FyLog.e(TAG, "�״�ˢ�£� " + first_cnt);
							getTheWifiList(list, lstConfig);
							gv_wifi_list.setAdapter(adapter);
							adapter.notifyDataSetChanged();
							tv_show.setVisibility(View.GONE);
						}
					}
				}
				if(fresh_cnt++ % 20 == 0){
					fresh_cnt = 5;
					if(list != null){
						FyLog.e(TAG, "ˢ�£� " + fresh_cnt);
						getTheWifiList(list, lstConfig);
						gv_wifi_list.setAdapter(adapter);
						adapter.notifyDataSetChanged();
						tv_show.setVisibility(View.GONE);
					}
				}
			}
		}
	};
	/**
	 * ��ȡ���к��wifi�б�����
	 */
	private List<ScanResult> lstUnSave = new ArrayList<ScanResult>();
	private List<ScanResult> lstSave = new ArrayList<ScanResult>();
	private void getTheWifiList(List<ScanResult> list, List<WifiConfiguration> lstConfig) {
		//�������
		lstWifi.clear(); lstSave.clear(); lstUnSave.clear();
		//���Ƿ񱣴����������֡�
		if(list != null)
		for (ScanResult scanResult : list) {
			if(lstConfig.size() > 0){
				for (int i = 0; i < lstConfig.size(); i++) {
					WifiConfiguration config = lstConfig.get(i);
					//wifi���ѱ������õ�wifi��һ��
					if(("\""+scanResult.SSID+"\"").equals(config.SSID)){
						//wifi�͵�ǰ���ӵ�wifiһ��
						if(("\""+scanResult.SSID+"\"").equals(wifiAdmin.GetSSID())){
							if(!lstSave.contains(scanResult))
								lstSave.add(0, scanResult);
						//wifiΪ�ѱ����wifi
						}else{
							if(!lstSave.contains(scanResult)){
								if(lstSave.size() == 0)
									lstSave.add(0, scanResult);
								else
									lstSave.add(1, scanResult);
							}
						}
						FyLog.i(TAG, "the SSID is: " + wifiAdmin.GetSSID() + "the SSID is: " + scanResult.SSID);
					}else{
						if(!lstUnSave.contains(scanResult))
							lstUnSave.add(0, scanResult);
					}
				}
			}else{
				lstUnSave.add(0, scanResult);
			}
		}
		//���Ա����wifi��ʾ��ǰ��
		for(ScanResult scanResult : lstSave){
			if(!lstWifi.contains(scanResult))
				lstWifi.add(scanResult);
		}
		for(ScanResult scanResult : lstUnSave){
			if(!lstWifi.contains(scanResult))
				lstWifi.add(scanResult);
		}
	}
	
	private boolean isUnregister = false;
	@Override
	protected void onStop() {
		super.onStop();
		if(!isUnregister){
			Log.d(TAG, "unregister receiver");
			unregisterReceiver(mReceiver);
			isUnregister = true;
		}
	};
	/**
	 * �����źŴ�С���Ƿ��Ա���������
	 * @return
	 */
	private List<ScanResult> getAllNetworkLisr() {
		if (wifiAdmin.Wifistate() == 1) {
			// ��ʼɨ������
			wifiAdmin.StartScan();
			//��ȡȫ��wifi�б�
			List<ScanResult> list = wifiAdmin.GetWifiList();
			List<ScanResult> lstScan = new ArrayList<ScanResult>();
			if (list != null && list.size() > 0) {
				//�����һ��wifi
				lstScan.add(list.get(0));
				list.remove(0);
				while(list.size()>0){
					//��ȡ�ڶ���wifi
					mScanResult = (ScanResult) list.get(0);
					//lstScan�д���2����Ҫ���жԱ�ǰ���������  1  4  6
					if(lstScan.size() >= 2){
						int index = 0;
						for (int j = 0; j < lstScan.size()-1; j++) {
							if(mScanResult.level > lstScan.get(j).level){
								if(mScanResult.level > lstScan.get(j+1).level){
									index = j+2;
								}else{
									index = j+1;
									break;
								}
							}
						}
						if(!lstScan.contains(mScanResult))
							lstScan.add(index, mScanResult);
					//lstScanС��2����ֻҪ�Ա�һ�ξͿ������
					}else{
						if(mScanResult.level > lstScan.get(0).level)
							lstScan.add(mScanResult);
						else
							lstScan.add(0, mScanResult);
					}
					list.remove(0);
				}
//				for(ScanResult result : lstScan){
//					Log.e(TAG, "RESULT SCAN="+ result.SSID+ " B="+ result.BSSID+ " level="+ result.level);
//				}
				return lstScan;
			}
		} 
		return null;
	}

	private Dialog dialog = null;
	private void showMyDialog(View layout, int location) {
		dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
		dialog.setContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// ������ʾ����
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = location;
		// ������������Ϊ�˱�֤��ť����ˮƽ����
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		// ������ʾλ��
		dialog.onWindowAttributesChanged(wl);
		// ���õ����Χ��ɢ
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
}
