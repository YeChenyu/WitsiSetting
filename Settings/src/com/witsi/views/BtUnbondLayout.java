package com.witsi.views;

import com.witsi.setting1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class BtUnbondLayout {

	private Context context;
	private EditText et_name;
	private Button btn, btn1;
	private String name = null;
	
	public View layout;
	private OnRemoveBondListener listener;
	
	public BtUnbondLayout(Context context, String name) {
		// TODO Auto-generated constructor stub
		this.context = context;
		layout = LayoutInflater.from(context).inflate(R.layout.bluetooth_item, null);
		et_name = (EditText) layout.findViewById(R.id.tv);
		et_name.setText(name);
	}
	
	public interface OnRemoveBondListener{
		public void removeBond();
		public void setDeviceName(String name);
	}
	
	
	public void setOnRemoveBondListener(OnRemoveBondListener listener){
		this.listener = listener;
		layout.findViewById(R.id.btn1).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(BtUnbondLayout.this.listener != null)
					BtUnbondLayout.this.listener.removeBond();
			}
		});
		layout.findViewById(R.id.btn).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(BtUnbondLayout.this.listener != null)
					BtUnbondLayout.this.listener.setDeviceName(et_name.getText().toString());
			}
		});
	}
	public View getLayout(){
		return layout;
	}
	
	public String getDeviceName(){
		if(name == null)
			return null;
		else
			return name;
	}
}
