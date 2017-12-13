package com.witsi.settings;

import java.util.List;

import com.android.settings.DevelopmentSettings;
import com.witsi.debug.FyLog;
import com.wtisi.settings.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



public class MainActivity extends  SuptLwerVerPreferenceActivity{

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private List<Header> mCopyHeaders;
	private SharedPreferences mDevelopmentorSharePreference;
	private OnSharedPreferenceChangeListener mOnSharePreferenceListener;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final boolean isCustom =requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//activity切换效果  渐变
    	overridePendingTransition(R.drawable.activity_out, R.drawable.activity_in);
		super.onCreate(savedInstanceState);
		if(isCustom){
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.top_titlebar_layout);
		}
		
		if(mDevelopmentorSharePreference == null)
			mDevelopmentorSharePreference = getSharedPreferences(DevelopmentSettings.PREF_FILE, Context.MODE_PRIVATE);
		mOnSharePreferenceListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
				// TODO Auto-generated method stub
				invalidateHeaders();
			}
		};
		
		
		findViewById(R.id.ll_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				overridePendingTransition(R.drawable.activity_in, R.drawable.activity_out);
				finish();
			}
		});
		TextView title = ((TextView)findViewById(android.R.id.title)); 
        if(title != null)
        	title.setText("WITSI 设置");
		
		ListView listView = getListView();
		FyLog.e("fdff", getListView().getLayoutParams().getClass().getName());
		if (mCopyHeaders != null && mCopyHeaders.size() > 0) {
			setListAdapter(new HeaderAdapter(this, mCopyHeaders));
			listView.setBackgroundResource(R.color.background);
			listView.setDividerHeight(0);
			listView.setPadding(0, 0, 0, 0);
			listView.setSelector(R.drawable.bg_transport);
		}
	}
	
	
	@Override
	public void onBuildHeaders(List<Header> target) {
		this.loadHeadersFromResource(R.xml.settings_headers, target);
		if(mDevelopmentorSharePreference == null)
			mDevelopmentorSharePreference = getSharedPreferences(DevelopmentSettings.PREF_FILE, Context.MODE_PRIVATE);
		final boolean showDev = mDevelopmentorSharePreference.getBoolean(
                DevelopmentSettings.PREF_SHOW,
                android.os.Build.TYPE.equals("eng"));
		FyLog.d(TAG, "showDev="+ showDev);
		int i = 0;
		while(i < target.size()){
			if(target.get(i).id == R.id.development_settings && showDev){
				target.remove(i);
				break;
			}
			i++;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mDevelopmentorSharePreference.registerOnSharedPreferenceChangeListener(mOnSharePreferenceListener);
	}
	
	
	@Override
	public void onHeaderClick(Header arg0, int arg1) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "onHeaderClick()");
		if(arg0.id == R.id.hardwaretest_settings){
			FyLog.d(TAG, "the layout id="+ R.layout.bluetooth_settings_layout);
//			Intent intent = new Intent(MainActivity.this, com.example.android_equipment_test.RfActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(intent);
			return ;
		}
		super.onHeaderClick(arg0, arg1);
	};
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		FyLog.d(TAG, "MainActivity.onDesttroy()");
		mDevelopmentorSharePreference.unregisterOnSharedPreferenceChangeListener(mOnSharePreferenceListener);
		if(isTaskRoot())
			getSharedPreferences(DevelopmentSettings.PREF_FILE,
					Context.MODE_PRIVATE).edit().putBoolean(
							DevelopmentSettings.PREF_SHOW, true).apply();
	}
	
	
	
	private static class HeaderAdapter extends ArrayAdapter<Header> {

        private LayoutInflater mInflater;

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            Header header = getItem(position);
        	holder = new HeaderViewHolder();
        	if(header.id == R.id.wireless_section || header.id == R.id.device_section){
        		 convertView =  mInflater.inflate(R.layout.setting_category_item, parent, false);
                 holder.title = (TextView) convertView.findViewById(android.R.id.title);
                 holder.typeId = HeaderViewHolder.TYPE_1;
            Log.i(TAG, "convertView null index="+ position);
        	}else{
        		convertView =  mInflater.inflate(R.layout.setting_header_item, parent, false);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.title = (TextView) convertView.findViewById(android.R.id.title);
                holder.summary = (TextView) convertView.findViewById(android.R.id.summary);
                holder.typeId = HeaderViewHolder.TYPE_2;
            Log.d(TAG, "convertView null index="+ position);    
        	}
        	convertView.setTag(holder);
            
            holder.title.setText(header.getTitle(getContext().getResources()));
            if(header.id != R.id.wireless_section && header.id != R.id.device_section){
            	holder.icon.setImageResource(header.iconRes);
                CharSequence summary = header.getSummary(getContext().getResources());
                if (!TextUtils.isEmpty(summary)) {
                    holder.summary.setVisibility(View.VISIBLE);
                    holder.summary.setText(summary);
                } else {
                    holder.summary.setVisibility(View.GONE);
                }
            }
            return convertView;
        }
        
        private static class HeaderViewHolder {
        	public static final int TYPE_1 = 0;
        	public static final int TYPE_2 = 1;
        	long typeId;
            ImageView icon;
            TextView title;
            TextView summary;
        }
    }
	
	@Override
	public void loadHeadersFromResource(int resid, List<Header> target) {
		super.loadHeadersFromResource(resid, target);
		mCopyHeaders = target;
	}
	
}