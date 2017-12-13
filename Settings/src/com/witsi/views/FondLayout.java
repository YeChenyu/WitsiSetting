package com.witsi.views;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.FondType;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

public class FondLayout implements OnClickListener, OnCheckedChangeListener{

	private String TAG = FondLayout.class.getSimpleName();
	private Context context;
	
	private View v;
	
	private RelativeLayout rl, rl1, rl2, rl3;
	private RadioButton cb, cb1, cb2, cb3;
	
	private OnFondDislogDismissListener listener;
	private Configuration mCurConfig = new Configuration();
	
	public FondLayout(Context context, OnFondDislogDismissListener listener) {
		super();
		this.context = context;
		this.listener = listener;
		v = LayoutInflater.from(context).inflate(R.layout.display_fond_layout,
				null);
		
		v.findViewById(R.id.rl).setOnClickListener(this);
		v.findViewById(R.id.rl1).setOnClickListener(this);
		v.findViewById(R.id.rl2).setOnClickListener(this);
		v.findViewById(R.id.rl3).setOnClickListener(this);
		cb = (RadioButton) v.findViewById(R.id.cb);
		cb1 = (RadioButton) v.findViewById(R.id.cb1);
		cb2 = (RadioButton) v.findViewById(R.id.cb2);
		cb3 = (RadioButton) v.findViewById(R.id.cb3);
		
		String str = FondType.getFontSzie();
		try {
            mCurConfig.updateFrom(
                ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
        }
		
		if(str.equals("小")){
			cb.setChecked(true);
		}else if(str.equals("普通")){
			cb1.setChecked(true);
		}else if(str.equals("大")){
			cb2.setChecked(true);
		}else if(str.equals("超大")){
			cb3.setChecked(true);
		}
		cb.setOnCheckedChangeListener(this);
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
		cb3.setOnCheckedChangeListener(this);
		
	}

	public View getView(){
		return v;
	}

	public interface OnFondDislogDismissListener{
		public void onDismiss(String fontSize);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.rl:
			if(!cb.isChecked())
				cb.setChecked(true);
			else
				cb.setChecked(false);
			break;
		case R.id.rl1:
			if(!cb1.isChecked())
				cb1.setChecked(true);
			else
				cb1.setChecked(false);
			break;
		case R.id.rl2:
			if(!cb2.isChecked())
				cb2.setChecked(true);
			else
				cb2.setChecked(false);
			break;
		case R.id.rl3:
			if(!cb3.isChecked())
				cb3.setChecked(true);
			else
				cb3.setChecked(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "check change: " + arg1);
		switch (arg0.getId()) {
		case R.id.cb:
			if(arg1){
				listener.onDismiss("小");
				mCurConfig.fontScale = 0.75f;
			}
			break;
		case R.id.cb1:
			if(arg1){
				listener.onDismiss("普通");
				mCurConfig.fontScale = 1.0f;
			}
			break;
		case R.id.cb2:
			if(arg1){
				listener.onDismiss("大");
				mCurConfig.fontScale = 1.25f;
			}
			break;
		case R.id.cb3:
			if(arg1){
				listener.onDismiss("超大");
				mCurConfig.fontScale = 1.35f;
			}
			break;
		default:
			break;
		}
		try {
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
        	e.printStackTrace();
        }
	}

}
