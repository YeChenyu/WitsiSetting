package com.witsi.config;


import com.witsi.debug.FyLog;

import android.os.Build;

public class ProjectConfig {

	public static int PROG_2102 = 0;
	public static int PROG_3023= 1;
	public static int PROG_3025 = 2;
	public static int PROG_3029 = 3;
	public static int PROG_3027R = 4;
	
	public static final boolean burn_config = true;
	public static final boolean system_app = true;
	
	public static int getTheMechineType(){
		FyLog.e(Build.MODEL, Build.MODEL);
		if(Build.MODEL.equals("I2102")){
			return PROG_2102;
		}else if(Build.MODEL.equals("I3029")){
			return PROG_3029;
		}else if(Build.MODEL.equals("SP300")){
			return PROG_3025;
		}else if(Build.MODEL.equals("SP210")){
			return PROG_3027R;
		}else if(Build.MODEL.equals("S5500")){
			return PROG_3029;
		}
		return -1;
	}
}
