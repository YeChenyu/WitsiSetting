package com.witsi.settings.display_media;

import com.witsi.views.MyAlertDialog;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MyListPreference extends ListPreference{

	public MyListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	
	@Override
	protected View onCreateView(ViewGroup parent) {
		// TODO Auto-generated method stub
		return super.onCreateView(parent);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		// TODO Auto-generated method stub
	}
}
