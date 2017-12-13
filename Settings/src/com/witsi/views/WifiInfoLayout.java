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

public class WifiInfoLayout {

	private Context context = null;
	
	public LinearLayout v;
	private TextView name, secure, status, wifiLevel, speed, rate;
	private Button btn, btn1;
	
	@SuppressLint("NewApi")
	public WifiInfoLayout(Context context, ScanResult scanResult) {
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
		
		name.setText(scanResult.SSID);
		//���� wifi��ȫ��
		secure.setText(getWifiSecure(scanResult.capabilities));
        //��������״̬
		status.setText("������");
		//�����ź�ǿ��
		int level = Math.abs(scanResult.level);
		if(level > 100){
			wifiLevel.setText("����");
		}else if(level > 70){
			wifiLevel.setText("����");
		}else if(level > 65){
			wifiLevel.setText("��");
		}else if(level > 60){
			wifiLevel.setText("��");
		}else if(level > 55){
			wifiLevel.setText("ǿ");
		}else if(level > 50){
			wifiLevel.setText("ǿ");
		}else{
			wifiLevel.setText("ǿ");
		}
		//���������ٶ�
		speed.setText("" + scanResult.timestamp);
		//��������
		rate.setText("" + scanResult.frequency + "Hz");
	}
	
	public interface OnWifiClickListener{
		public void onCancelSaveConfig();
		public void confirm();
	}
	
	private OnWifiClickListener mListener = null;
	public void setOnWifiClickListener(OnWifiClickListener listener){
		this.mListener = listener;
		btn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(mListener != null)
					mListener.onCancelSaveConfig();
			}
		});
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(mListener != null)
					mListener.confirm();
			}
		});
	}
	/**
	 * ��ȡWIFI��ȫ����
	 * @param capabilities
	 * @return
	 */
	private String getWifiSecure(String capabilities){
		if(capabilities.equals("[ESS]"))
			return "��";
		else if(capabilities.contains("WPA") 
				&& !capabilities.contains("WPA2"))
			return "WPA PSK";
		else if(capabilities.contains("WPA") 
				&& capabilities.contains("WPA2"))
			return "WPA/WPA2 PSK";
		else 
			return "δ֪";
	}
}
