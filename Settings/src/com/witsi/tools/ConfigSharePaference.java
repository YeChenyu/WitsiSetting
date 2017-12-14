package com.witsi.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigSharePaference {

	public static final String CONFIG_FILE_NAME = "config";
	private SharedPreferences sp = null;
	
	public static SharedPreferences getSharedPreferences(Context context){
		
		return context.getSharedPreferences(CONFIG_FILE_NAME, 0);
	}
		
		
}
