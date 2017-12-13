package com.witsi.setting.manager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.witsi.setting.manager.luncher.AppInfo;
import com.witsi.setting.manager.luncher.AppInfo;



import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;


public class ScreenInfo implements Parcelable{

	private int screen_index = -1; //屏幕索引
	private boolean isSelfConfig = false; //自定义屏幕参数 
	private int row_num = 0;   //行数
	private int cloum_num = 0;  //列数
//	private List<AppInfo> list = null;  //每屏的应用列表
    private int screenHeight = 0;  //显示的屏幕高度
    private Map<Integer, Map<Integer, AppInfo>> map = null;
    
	public ScreenInfo(int screenHeight) {
		// TODO Auto-generated constructor stub
//		list = new ArrayList<AppInfo>();
		map = new HashMap<Integer, Map<Integer,AppInfo>>();
		this.screenHeight = screenHeight;
	}
	public ScreenInfo() 
	{
		map = new HashMap<Integer, Map<Integer,AppInfo>>();
	}
	
	public void initScreen(int screen_index, boolean isSelfConfig
			, int row_num, int cloum_num){
		this.screen_index = screen_index;
		this.isSelfConfig = isSelfConfig;
		this.row_num = row_num;
		this.cloum_num = cloum_num;
		
	}
	
//	public List<AppInfo> getList() {
//		return list;
//	}
//	public void setList(List<AppInfo> list) {
//		this.list = list;
//	}
//
//	public void addAppInfoList(AppInfo info){
//		list.add(info);
//	}
	
	public int getScreen_index() {
		return screen_index;
	}

	public boolean isSelfConfig() {
		return isSelfConfig;
	}

	public int getRow_num() {
		return row_num;
	}

	public int getCloum_num() {
		return cloum_num;
	}

	public void setRow_num(int row_num) {
		this.row_num = row_num;
	}
	
	public void setScreen_index(int screen_index) {
		this.screen_index = screen_index;
	}
	public Map<Integer, Map<Integer, AppInfo>> getMap() {
		return map;
	}

	public void setMap(Map<Integer, Map<Integer, AppInfo>> map) {
		this.map = map;
	}
	/**
	 *  根据行列索引添加应用数据
	 * @param info 应用数据
	 * @param row 行索引
	 * @param cloum 列索引
	 */
	public void addAppInfoMap(AppInfo info, int row, int cloum){
		Map<Integer, AppInfo> map_row;
		if(map.containsKey(row)){
			map_row = map.get(row);
		}else{
			map_row = new HashMap<Integer, AppInfo>();
			map.put(row, map_row);
		}
		if(map_row != null){
			map_row.put(cloum, info);
		}
	}
	
	public void addAppInfoMap(AppInfo info){
		Map<Integer, AppInfo> map_row;
		int row = info.position_y;
		int cloum  = info.position_x;
		if(map.containsKey(row)){
			map_row = map.get(row);
		}else{
			map_row = new HashMap<Integer, AppInfo>();
			map.put(row, map_row);
		}
		if(map_row != null){
			map_row.put(cloum, info);
		}
	}
	
	public int getRow_height() {
		return screenHeight / row_num;
	}
	
	public static final Parcelable.Creator<ScreenInfo> CREATOR = new Creator<ScreenInfo>() {  
        public ScreenInfo createFromParcel(Parcel source) {  
        	ScreenInfo screen = new ScreenInfo();  
            screen.screen_index = source.readInt();
            screen.row_num = source.readInt();
            screen.cloum_num = source.readInt();
            screen.screenHeight = source.readInt();
            screen.map = (Map<Integer, Map<Integer, AppInfo>>) source.readValue(HashMap.class.getClassLoader());
            return screen;  
        }  
        public ScreenInfo[] newArray(int size) {  
            return new ScreenInfo[size];  
        }  
    };  
      
    public int describeContents() {  
        return 0;  
    }  
    public void writeToParcel(Parcel parcel, int flags) {  
        parcel.writeInt(screen_index);
        parcel.writeInt(row_num);  
        parcel.writeInt(cloum_num);  
        parcel.writeInt(screenHeight);  
        parcel.writeValue(map);
    } 
}
