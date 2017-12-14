package com.witsi.views;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Text;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.wifi.ScanResult;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

import com.witsi.setting1.R;
import com.witsi.views.WifiConnectLayout.OnWifiClickListener;

public class WifiDisconLayout implements OnCheckedChangeListener{

	private Context context ;
	
	public LinearLayout v;
	private TextView name, secure;
	private LinearLayout ll;
	private EditText pwd;
	private CheckBox cb1, cb2;
	private LinearLayout setting;
	private Spinner sp1, sp2;
	private Button btn, btn1;
	
	private String strPwd = "";
	private String tmpPwd = "";
	private boolean isShowPwd = false;
	private boolean isChange = false;
	
	public WifiDisconLayout(Context context, ScanResult scanResult) {
		// TODO Auto-generated constructor stub
		this.context = context;
		
		v = (LinearLayout) LayoutInflater.from(context)
				.inflate(R.layout.wifi_discon_click_dialog, null);
		name = (TextView) v.findViewById(R.id.tv);
		secure = (TextView) v.findViewById(R.id.tv1);
		secure.setText(getWifiSecure(scanResult.capabilities));
		ll = (LinearLayout) v.findViewById(R.id.ll);
		pwd = (EditText) v.findViewById(R.id.et1);
		cb1 = (CheckBox) v.findViewById(R.id.cb1);
		cb2 = (CheckBox) v.findViewById(R.id.cb2);
		setting = (LinearLayout) v.findViewById(R.id.setting);
		sp1 = (Spinner) v.findViewById(R.id.sp1);
		sp2 = (Spinner) v.findViewById(R.id.sp2);
		btn = (Button) v.findViewById(R.id.btn);
		btn1 = (Button) v.findViewById(R.id.btn1);
		btn1.setEnabled(false);
		name.setText(scanResult.SSID);
		//第一步：添加一个下拉列表项的list，这里添加的项就是下拉列表的菜单项   
		List<String> lst1 = new ArrayList<String>();
        lst1.add("无");    
        lst1.add("手动");  
        List<String> lst2 = new ArrayList<String>();
        lst2.add("DHCP");    
        lst2.add("静态");    
        //第二步：为下拉列表定义一个适配器，这里就用到里前面定义的list。     
        ArrayAdapter adapter1 = new ArrayAdapter<String>(context, 
        		android.R.layout.simple_spinner_item, lst1);    
        ArrayAdapter adapter2 = new ArrayAdapter<String>(context, 
        		android.R.layout.simple_spinner_item, lst2);    
        //第三步：为适配器设置下拉列表下拉时的菜单样式。     
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    
        //第四步：将适配器添加到下拉列表上     
		sp1.setAdapter(adapter1);
		sp2.setAdapter(adapter2);
		
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
		pwd.addTextChangedListener(watcher);
	}
	
	public String getPassWord(){
		return pwd.getText().toString();
	}
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.cb1:
			if(arg1){
				pwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}else{
				pwd.setInputType(InputType.TYPE_CLASS_TEXT 
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}
			pwd.setSelection(pwd.getText().toString().length());
			break;
		case R.id.cb2:
			if(arg1)
				setting.setVisibility(View.VISIBLE);
			else
				setting.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		
	}
	
	private TextWatcher watcher = new TextWatcher() {
		private int cntPwd = 0;
		private String mchar = "";
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			if(arg0.length() > 6)
				btn1.setEnabled(true);
		}
	};
	
	public interface OnClickNoPassListener{
		public void onConnectNoPwdWifi();
		public void confirm();
	}
	
	private OnClickNoPassListener mListener = null;
	public void setOnWifiClickListener(OnClickNoPassListener listener){
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
					mListener.onConnectNoPwdWifi();
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
