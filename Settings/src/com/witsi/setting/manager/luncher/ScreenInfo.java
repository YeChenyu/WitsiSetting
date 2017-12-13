package com.witsi.setting.manager.luncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.witsi.debug.FyLog;
import com.witsi.setting.manager.Book;
import com.witsi.setting.manager.luncher.AppInfo;
import android.os.Parcel;
import android.os.Parcelable;


public class ScreenInfo implements Parcelable{

	private static final String TAG = ScreenInfo.class.getSimpleName();
	
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
			//若不可见，则视为消失
			if(info.getIntent() != null){
				mapRow.put(""+(row)+(cloum)+ info.weight, info.intent.getComponent().getPackageName());
				FyLog.d(TAG, info.getIntent().getComponent().getPackageName());
			}
		}
	}
	
	public void removeAppInfo(int x, int y, AppInfo info){
		if(map != null && map.size() != 0){
			map.get(x).remove(y);
		}
		Set<String> row = mapRow.keySet();
		for (Iterator iterator = row.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if(info.getIntent().getComponent() == null){
				if(info != null && info.componentName.getPackageName()
				.equals(mapRow.get(key))){
					FyLog.e(TAG, "remove the screen index: " + info.componentName.getPackageName());
					mapRow.remove(key);
					break;
				}
			}else{
				if(info != null && info.getIntent().getComponent().getPackageName()
				.equals(mapRow.get(key))){
					FyLog.e(TAG, "remove the screen index: " + info.getIntent().getComponent().getPackageName());
					mapRow.remove(key);
					break;
				}
			}
		}
	}
	public void removeMapRow(AppInfo info){
		Set<String> row = mapRow.keySet();
		for (Iterator iterator = row.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if(info.getIntent().getComponent() == null){
				if(info != null && info.componentName.getPackageName()
				.equals(mapRow.get(key))){
					FyLog.e(TAG, "remove the screen index: " + info.componentName.getPackageName());
					mapRow.remove(key);
					break;
				}
			}else{
				if(info != null && info.getIntent().getComponent().getPackageName()
				.equals(mapRow.get(key))){
					FyLog.e(TAG, "remove the screen index: " + info.getIntent().getComponent().getPackageName());
					mapRow.remove(key);
					break;
				}
			}
		}
	}
	public int getRow_height() {
		return screenHeight / row_num;
	}
	public Map<String, String> getMapRow() {
		return mapRow;
	}
	
	private Map<String, String> mapRow = new HashMap<String, String>();
	
	public static final Parcelable.Creator<ScreenInfo> CREATOR =
			new Creator<ScreenInfo>() {  
        @SuppressWarnings("unchecked")
		public ScreenInfo createFromParcel(Parcel source) {  
        	ScreenInfo screen = new ScreenInfo();  
            screen.screen_index = source.readInt();
            screen.row_num = source.readInt();
            screen.cloum_num = source.readInt();
            screen.screenHeight = source.readInt();
            screen.mapRow = (Map<String, String>) source.readHashMap(HashMap.class.getClassLoader());
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
        parcel.writeMap(mapRow);
    } 
    
}
