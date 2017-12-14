package com.witsi.views;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.witsi.setting1.R;

public class WifiConnectLayout {

	private Context context = null;
	
	public LinearLayout v;
	private TextView name, secure, status, wifiLevel, speed, rate;
	private Button btn, btn1;
	
	@SuppressLint("NewApi")
	public WifiConnectLayout(Context context, ScanResult scanResult) {
		// TODO Auto-generated constructor stub
		this.context = context;
		
		v = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.wifi_con_click_dialog, null);
		name = (TextView) v.findViewById(R.id.tv);
		secure = (TextView) v.findViewById(R.id.tv1);
		status = (TextView) v.findViewById(R.id.tv2);
		wifiLevel = (TextView) v.findViewById(R.id.tv3);
		speed = (TextView) v.findViewById(R.id.tv4);
		rate = (TextView) v.findViewById(R.id.tv5);
		btn = (Button) v.findViewById(R.id.btn);
		btn1 = (Button) v.findViewById(R.id.btn1);
		
		secure.setVisibility(View.GONE);
		status.setVisibility(View.GONE);
		wifiLevel.setVisibility(View.GONE);
		rate.setVisibility(View.GONE);
		btn.setText("取消");
		btn1.setText("连接");
		name.setText(scanResult.SSID);
		//设置 wifi安全性
		secure.setText(getWifiSecure(scanResult.capabilities));
        //设置连接状态
		status.setText("已连接");
		//设置信号强度
		int level = Math.abs(scanResult.level);
		if(level > 100){
			wifiLevel.setText("较弱");
		}else if(level > 70){
			wifiLevel.setText("较弱");
		}else if(level > 65){
			wifiLevel.setText("弱");
		}else if(level > 60){
			wifiLevel.setText("弱");
		}else if(level > 55){
			wifiLevel.setText("强");
		}else if(level > 50){
			wifiLevel.setText("强");
		}else{
			wifiLevel.setText("强");
		}
		//设置连接速度
		speed.setText("" + scanResult.timestamp);
		//设置评率
		rate.setText("" + scanResult.frequency + "Hz");
	}
	
	public interface OnWifiClickListener{
		public void onCancelSaveConfig();
		public void confirm();
	}
	
	private OnWifiClickListener mListener = null;
	public void setOnWifiClickListener(OnWifiClickListener listener){
		this.mListener = listener;
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(mListener != null)
					mListener.confirm();
			}
		});
		btn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(mListener != null)
					mListener.onCancelSaveConfig();
			}
		});
	}
	/**
	 * 获取WIFI安全类型
	 * @param capabilities
	 * @return
	 */
	private String getWifiSecure(String capabilities){
		if(capabilities.equals("[ESS]"))
			return "无";
		else if(capabilities.contains("WPA") 
				&& !capabilities.contains("WPA2"))
			return "WPA PSK";
		else if(capabilities.contains("WPA") 
				&& capabilities.contains("WPA2"))
			return "WPA/WPA2 PSK";
		else 
			return "未知";
	}
}
