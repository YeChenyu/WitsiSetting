package com.witsi.settings.system;

import com.witsi.settings.SettingsPreferenceFragment;
import com.wtisi.settings.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SystemSettings extends SettingsPreferenceFragment{

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.list_preference, container, false);
    	return view;
	}
	
	
	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.system_settings);
		
		TextView title = ((TextView)getActivity().findViewById(android.R.id.title));
		if(title != null)
			title.setText(getString(R.string.system_settings_title));
		
//		removePreference("system_application");
	}
}
