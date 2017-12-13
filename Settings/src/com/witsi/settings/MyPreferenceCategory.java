package com.witsi.settings;

import com.wtisi.settings.R;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

public class MyPreferenceCategory extends PreferenceCategory{

	public MyPreferenceCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setLayoutResource(R.layout.preference_category);
	}

	public MyPreferenceCategory(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		setLayoutResource(R.layout.preference_category);
	}
}
