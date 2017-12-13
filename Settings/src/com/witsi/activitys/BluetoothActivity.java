package com.witsi.activitys;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.witsi.adapter.BTBondGvAdapter;
import com.witsi.adapter.BTUnbondLvAdapter;
import com.witsi.bluetooth.WtBluetoothDevice;
import com.witsi.bluetooth.WtBtListener.ConnectListerner;
import com.witsi.bluetooth.WtBtListener.SearchListerner;
import com.witsi.bluetooth.WtScanBtDevice;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ClsUtils;
import com.witsi.views.BtUnbondLayout;
import com.witsi.views.BtUnbondLayout.OnRemoveBondListener;
import com.witsi.views.BtVisibleTimeOutLayout;
import com.witsi.views.BtVisibleTimeOutLayout.OnVisibleTimeOutListener;
import com.witsi.views.SlipButton;
import com.witsi.views.SlipButton.OnChangedListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.Handler.Callback;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends Activity implements OnItemClickListener, Callback
					, OnChangedListener, OnClickListener, OnItemLongClickListener{

	private String TAG = BluetoothActivity.class.getSimpleName();
	private Context context = BluetoothActivity.this;
	
	private LinearLayout ll_back;
	private LinearLayout ll_list;
	private TextView tv_show;
	private SlipButton sw;
	private Button btn_scan;
	private Button btn_visible;
	private TextView tv_name;
	private TextView tv_visible;
	
	private GridView gv_unbond_list;
	private BTBondGvAdapter unbondAdapter;
	private List<BluetoothDevice> lstBond ;
	
	private ListView lv_bond_list;
	private BTUnbondLvAdapter bondAdapter;
	private List<BluetoothDevice> lstUnbond;
	
	private WtScanBtDevice bt_scan = null;
	private WtBluetoothDevice bt_device = null;
	private BluetoothAdapter bt_adapter = null;
	private Handler handler = null;
	private Timer timer;
	private int timeout = 0;
	private String tempBack = null;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_activity);
		
		tv_name = ((TextView)(findViewById(R.id.action_back).findViewById(R.id.tv)));
		tv_name.setText("蓝牙");
		ll_back = (LinearLayout)(findViewById(R.id.action_back).findViewById(R.id.ll_back));
		ll_list = (LinearLayout) findViewById(R.id.sv_list);
		tv_show = (TextView) findViewById(R.id.tv_show);
		sw = (SlipButton) findViewById(R.id.action_back).findViewById(R.id.sw);
		lv_bond_list = (ListView) findViewById(R.id.lv_bond_list);
		gv_unbond_list = (GridView) findViewById(R.id.gv_unbond_list);
		btn_scan = (Button) findViewById(R.id.btn_scan);
		btn_visible = (Button) findViewById(R.id.btn_visible);
		tv_visible = (TextView) findViewById(R.id.visible);
		lv_bond_list.setOnItemClickListener(this);
		gv_unbond_list.setOnItemClickListener(this);
		lv_bond_list.setOnItemLongClickListener(this);
		gv_unbond_list.setOnItemLongClickListener(this);
		ll_back.setOnClickListener(this);
		sw.setOnChangedListener(this);
		btn_scan.setOnClickListener(this);
		btn_visible.setOnClickListener(this);
		
		registerReceiver(receiver, getIntentFilter());
		
		handler = new Handler(this);
		lstBond = new ArrayList<BluetoothDevice>();
		lstUnbond = new ArrayList<BluetoothDevice>();
		bondAdapter = new BTUnbondLvAdapter(context, lstBond);
		unbondAdapter = new BTBondGvAdapter(context, lstUnbond);
		gv_unbond_list.setAdapter(unbondAdapter);
		lv_bond_list.setAdapter(bondAdapter);
		
		bt_scan = new WtScanBtDevice(context, true);
		bt_device = new WtBluetoothDevice(context, true);
		tv_show.setVisibility(View.VISIBLE);
		ll_list.setVisibility(View.GONE);
		btn_scan.setEnabled(false);
		btn_visible.setEnabled(false);
		//根据系统的蓝牙状态初始化界面
		bt_adapter = BluetoothAdapter.getDefaultAdapter(); 
        // Checks if Bluetooth is supported on the device.
        if (bt_adapter == null) {
        	Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(bt_adapter.isEnabled()){
        	//打开蓝牙，开始搜索蓝牙设备
        	btn_scan.setText("搜索中...");
			bt_scan.startScanDevice(searchListener);
    		sw.setCheck(true);
    		tv_show.setVisibility(View.GONE);
			ll_list.setVisibility(View.VISIBLE);
			btn_scan.setEnabled(true);
			btn_visible.setEnabled(true);
			if(bt_adapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE)
				tv_visible.setText("已配对蓝牙可以查看该设备");
			else if(bt_adapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE)
				tv_visible.setText("附近蓝牙均可检测到该设备");
				
        }
        tv_name.setText(bt_adapter.getName());
        tv_name.setSingleLine();
        
        
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	private IntentFilter getIntentFilter(){
		IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        return filter;
	}
	private SearchListerner searchListener = new SearchListerner() {
		
		@Override
		public void onFinishFindDevice() {
			// TODO Auto-generated method stub
			btn_scan.setText("搜索设备");
		}
		
		@Override
		public void onFindDevice(BluetoothDevice arg0) {
			// TODO Auto-generated method stub
			FyLog.v(TAG, "device is fond");
			if(arg0.getBondState() == BluetoothDevice.BOND_BONDED){
				Log.e(TAG, arg0.getName()+ " BOND_BONDED");
				if(!lstBond.contains(arg0)){
					lstBond.add(arg0);
				}
				bondAdapter.notifyDataSetChanged();
			}else if(arg0.getBondState() == BluetoothDevice.BOND_NONE){
				Log.d(TAG, arg0.getName()+ " BOND_NONE");
				if(!lstUnbond.contains(arg0)){
					lstUnbond.add(arg0);
					unbondAdapter.notifyDataSetChanged();
				}
				if(lstBond.contains(arg0)){
					lstBond.remove(arg0);
					bondAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
	private ConnectListerner connectListener = new ConnectListerner() {
		
		@Override
		public void onStartConnect() {
			// TODO Auto-generated method stub
			FyLog.d(TAG, "onStartConnect");
		}
		
		@Override
		public void onDeviceConnectedFail() {
			// TODO Auto-generated method stub
			FyLog.d(TAG, "onDeviceConnectedFail");
			if(tempBack != null)
    			tv_visible.setText(tempBack);
		}
		
		@Override
		public void onDeviceConnected() {
			// TODO Auto-generated method stub
			FyLog.d(TAG, "onDeviceConnected");
			if(device.getBondState() == BluetoothDevice.BOND_BONDED){
				if(tempBack != null)
        			tv_visible.setText(tempBack);
				if(!lstBond.contains(device))
					lstBond.add(device);
				lstUnbond.remove(device);
				unbondAdapter.notifyDataSetChanged();
				bondAdapter.notifyDataSetChanged();
			}
		}
	};
	
	@Override
	public void onChanged(boolean checkState) {
		// TODO Auto-generated method stub
		if(checkState){
			if(!bt_adapter.isEnabled())
				bt_adapter.enable();
		}else{
			//关闭蓝牙。
			if(bt_adapter.isEnabled())
				bt_adapter.disable();
		}
	}
	 private final BroadcastReceiver receiver = new BroadcastReceiver() {
		 private BluetoothDevice device = null;
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            FyLog.v(TAG, "the action is: " + action);
	            //蓝牙状态改变
	            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
	            	Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
	            	//蓝牙状态改变
	            	if(state == BluetoothAdapter.STATE_ON){
	            		FyLog.e(TAG, "the bluetooth is open");
	            		tv_show.setVisibility(View.GONE);
	        			ll_list.setVisibility(View.VISIBLE);
	        			btn_scan.setEnabled(true);
	        			btn_visible.setEnabled(true);
	        			//打开蓝牙，开始搜索蓝牙设备
	        			bt_scan.startScanDevice(searchListener);
	            	}else if(state == BluetoothAdapter.STATE_TURNING_ON){
	            		tv_show.setText("正在打开蓝牙中...");
	            	}else if(state == BluetoothAdapter.STATE_TURNING_OFF){
	            		tv_show.setText("开启蓝牙后，您的设备可以与附近的其他蓝牙设备通信。");
	            		tv_show.setVisibility(View.VISIBLE);
	        			ll_list.setVisibility(View.GONE);
	        			btn_scan.setEnabled(false);
	        			btn_visible.setEnabled(false);
	            	}else if(state == BluetoothAdapter.STATE_OFF){
	            		lstBond.clear();
	        			lstUnbond.clear();
	        			unbondAdapter.notifyDataSetChanged();
	        			bondAdapter.notifyDataSetChanged();
	            	}
	            //蓝牙配对状态改变
	            }else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
	            	device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            	if(device.getBondState() == BluetoothDevice.BOND_BONDED){
	            		if(tempBack != null)
	            			tv_visible.setText(tempBack);
	            		FyLog.d(TAG, "the bluetooth is unbond");
	            		for(BluetoothDevice dev : lstUnbond){
	            			if(device.getAddress().equals(dev.getAddress())){
	            				lstUnbond.remove(dev);
	            				unbondAdapter.notifyDataSetChanged();
	            				if(!lstBond.contains(dev))
	            					lstBond.add(dev);
	            				bondAdapter.notifyDataSetChanged();
	            				break;
	            			}
	            				
	            		}
	            	}else if(device.getBondState() == BluetoothDevice.BOND_NONE){
	            		FyLog.d(TAG, "the bluetooth is unbond");
	            		if(tempBack != null)
	            			tv_visible.setText(tempBack);
	            		for(BluetoothDevice dev : lstBond){
	            			if(device.getAddress().equals(dev.getAddress())){
	            				lstBond.remove(dev);
	            				bondAdapter.notifyDataSetChanged();
	            				if(!lstUnbond.contains(dev))
	            					lstUnbond.add(dev);
	            				unbondAdapter.notifyDataSetChanged();
	            				break;
	            			}
	            		}
	            	}
	            //搜索状态改变
	            }else if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
	            	FyLog.d(TAG, "the scan mode change");
	            	this.abortBroadcast();
	            }
		};
	};
	private BluetoothDevice device = null;
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.gv_unbond_list:
			FyLog.d(TAG, "unbond list: " + arg2);
			tempBack = tv_visible.getText().toString();
			device = lstUnbond.get(arg2);
			tv_visible.setText("与 "+ device.getName()+ " 配对中...");
//			bt_device.conectionDevice(device, connectListener);
			try {
				ClsUtils.createBond(lstUnbond.get(arg2).getClass(), lstUnbond.get(arg2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.lv_bond_list:
			FyLog.d(TAG, "bond list");
			bt_device.conectionDevice(lstBond.get(arg2), connectListener);
			break;
		default:
			break;
		}
	}
	
	private Dialog dialog = null;
	private void showMyDialog(View layout, int location) {
		dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
		dialog.setContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = location;
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
	
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.lv_bond_list:
			FyLog.d(TAG, "unbond list");
			final BtUnbondLayout layout = new BtUnbondLayout(context, lstBond.get(arg2).getName());
			layout.setOnRemoveBondListener(new OnRemoveBondListener() {
				
				@Override
				public void setDeviceName(String name) {
					// TODO Auto-generated method stub
					if(name != null){
						bt_adapter.setName(name);
					}
					if(dialog != null)
						dialog.dismiss();
				}
				
				@Override
				public void removeBond() {
					// TODO Auto-generated method stub
					try {
						ClsUtils.removeBond(lstBond.get(arg2).getClass(), lstBond.get(arg2));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(dialog != null)
						dialog.dismiss();
				}
			});
			showMyDialog(layout.getLayout(), 0);
			break;
		default:
			break;
		}
		return false;
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_back:
			BluetoothActivity.this.finish();
			break;
		case R.id.btn_scan:
			if(btn_scan.getText().toString().trim()
					.equals("搜索设备")){
				lstUnbond.clear();
				unbondAdapter.notifyDataSetChanged();
				btn_scan.setText("搜索中...");
				bt_scan.startScanDevice(searchListener);
			}
			break;
		case R.id.btn_visible:
			final BtVisibleTimeOutLayout layout = new BtVisibleTimeOutLayout(context, bt_adapter);
			layout.setOnVisibleTimeOutListener(new OnVisibleTimeOutListener() {
				
				@Override
				public void setTimeOut(int timeout) {
					// TODO Auto-generated method stub
					if(timeout != -2){
						Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						if(timeout > 0){
							BluetoothActivity.this.timeout = timeout;
							intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeout);
						}else{
							intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
						}
						startActivity(intent);
						if(timer != null)
							timer.cancel();
						timer = new Timer();
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								FyLog.d(TAG, "the timer is working");
								if(BluetoothActivity.this.timeout-- <= 0){
									timer.cancel();
									closeDiscoverableTimeout();
									handler.sendEmptyMessage(0);
									return;
								}
								handler.sendEmptyMessage(1);
							}
						}, 0, 1000);
					}
					if(dialog != null)
						dialog.dismiss();
				}
			});
			showMyDialog(layout.getLayout(), 0);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case 0:
			tv_visible.setText("已配对蓝牙可以查看该设备");
			break;
		case 1:
			tv_visible.setText("可检测到此设备(" + BluetoothActivity.this.timeout + ")");
			break;
		default:
			break;
		}
		return false;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDiscoverableTimeout();
		if(bt_device != null)
			bt_device.close();
		if(bt_scan != null)
			bt_scan.stopDiscovering();
	}
	/**
	 * 设置蓝牙检测超时
	 * @param timeout
	 */
	public void setDiscoverableTimeout(int timeout) {
		BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
		try {
			Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
			setDiscoverableTimeout.setAccessible(true);
			Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
			setScanMode.setAccessible(true);
			
			setDiscoverableTimeout.invoke(adapter, timeout);
			setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 关闭蓝牙检测超时
	 */
	public void closeDiscoverableTimeout() {
		BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
		try {
			Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
			setDiscoverableTimeout.setAccessible(true);
			Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
			setScanMode.setAccessible(true);
			
			setDiscoverableTimeout.invoke(adapter, 1);
			setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE,1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
