package com.witsi.setting.manager;


import com.witsi.setting1.R;

import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

public class AppInfo extends ResolveInfo{

	//是否为脚本配置
	public boolean isConfig = false;
	
	public int position_screen = -1; // 屏幕索引
    public int position_x = -1; // 列位置
    public int position_y = -1;//  行位置
    public float weight = 1.0f;//  TipView在行中所占的比重
    public int back_color = R.color.white;// 背景颜色
    public String icons = "ic_launcher.png";  //应用图标
    public Bitmap bitIcon = null; //应用图标位图
    public int label_size = 15;// 应用标签字体大小
    
	public AppInfo() {
		// TODO Auto-generated constructor stub
	}
	
	
}
