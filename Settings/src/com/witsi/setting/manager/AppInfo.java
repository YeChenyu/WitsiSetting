package com.witsi.setting.manager;


import com.witsi.setting1.R;

import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

public class AppInfo extends ResolveInfo{

	//�Ƿ�Ϊ�ű�����
	public boolean isConfig = false;
	
	public int position_screen = -1; // ��Ļ����
    public int position_x = -1; // ��λ��
    public int position_y = -1;//  ��λ��
    public float weight = 1.0f;//  TipView��������ռ�ı���
    public int back_color = R.color.white;// ������ɫ
    public String icons = "ic_launcher.png";  //Ӧ��ͼ��
    public Bitmap bitIcon = null; //Ӧ��ͼ��λͼ
    public int label_size = 15;// Ӧ�ñ�ǩ�����С
    
	public AppInfo() {
		// TODO Auto-generated constructor stub
	}
	
	
}
