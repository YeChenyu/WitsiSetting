package com.witsi.setting.manager;

import com.witsi.setting1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class ScreenConfigLayout {

	private View v;
	private Spinner sp;
	private int numScreen = 0;
	
	private EditText et;
	
	public ScreenConfigLayout(Context context, int numScreen) {
		// TODO Auto-generated constructor stub
		v = LayoutInflater.from(context).inflate(R.layout.manager_screen_config_item,
				null);
		sp = (Spinner) v.findViewById(R.id.sp);
		et = (EditText) v.findViewById(R.id.et);
		
		String[] index = new String[4];
		for (int i = 0; i < index.length; i++) {
			index[i] = "" + (i+2) + "ÐÐ";
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				R.layout.manager_spinner_item, R.id.tv, index);
		sp.setAdapter(adapter);
		sp.setSelection(1);
	}
	
	
	public View getView() {
		return v;
	}
	public int getSpinnerSelected(){
		return sp.getSelectedItemPosition() + 2;
	}
	
	public int getRowNum(){
		return Integer.parseInt(et.getText().toString());
	}
}
