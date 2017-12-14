package com.witsi.setting.manager;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.setting.manager.MyViewPager.TransitionEffect;
import com.witsi.setting.manager.luncher.AppInfo;
import com.witsi.setting.manager.luncher.ScreenInfo;

import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;
import android.witsi.arq.ArqFileOps;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Rect;

public class ScreenConfigActivity extends Activity implements OnClickListener,
												OnLongClickListener{

	private String TAG = ScreenConfigActivity.class.getSimpleName();
	private Context context = ScreenConfigActivity.this;
	//竖屏
	private MyViewPager viewPager;
	private PageDataAdapter portAdapter;
	private List<LuncherView> lstItems;
	//包裹点点的LinearLayout
	private ViewGroup   pageController;
	private ImageView   imageView; 
	private ImageView[] points; 	
	
	private Display dis = null;
	private Rect r = new Rect();;
	private boolean isLandscape = false;
	private int main_height = 0;
	private int main_width = 0;
	
    private SharedPreferences sp = null;
    private Editor editor = null;
    private ArqFileOps arqFileOps = null;
    
	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		FyLog.d(TAG, "exec onCreate()");
		//设置无标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.manager_screen_activity);
        sp = getSharedPreferences("config", 0);
        editor = sp.edit();
//        createFile();
        //获取屏幕尺寸大小
  		dis = getWindowManager().getDefaultDisplay();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        FyLog.v(TAG, "the width is: " + dis.getWidth() + "the height is: " + dis.getHeight());
        if(dis.getWidth() > dis.getHeight())
        	isLandscape = true;
        else
        	isLandscape = false;
        
        if(!isLandscape){
        	/**竖屏构造分页浏览控件**/
        	FyLog.d(TAG, "init the portrail views");
            viewPager = (MyViewPager) findViewById(R.id.viewPager);
            //构造点点
            pageController = (ViewGroup)findViewById(R.id.pageController); 
            //设置切换动画
            viewPager.setTransitionEffect(TransitionEffect.Standard);
            //绑定监听事件
            viewPager.setOnPageChangeListener(new GuidePageChangeListener());
        }
        
        map = new HashMap<Integer, ScreenInfo>();
        map.put(0, new ScreenInfo(100));
        map.put(1, new ScreenInfo(100));
        map.put(2, new ScreenInfo(100));
        updateViews(map);
	}
	private Map<Integer, ScreenInfo> map;
	private void updateViews(Map<Integer, ScreenInfo> map)
	{
		FyLog.d(TAG, "exec updateViews()");
        if(!isLandscape){
        	/**竖屏**/
        	main_height = (dis.getHeight() - 100 - r.top);
            main_width = dis.getWidth() ; 
            FyLog.v(TAG, "the top is: " + r.top + "the main_width is: " + main_width + "the mainheight is: " + main_height);
            //获取Data Item数据
            lstItems = updateLuncherTable(map);
            FyLog.d(TAG, "get the app data");
    	    //有多少张图就有多少个点点
            points = new ImageView[lstItems.size()];
            pageController.removeAllViews();
            for(int i =0;i<lstItems.size();i++){
            	imageView = new ImageView(context);
            	imageView.setLayoutParams(new LayoutParams(30,30));
            	imageView.setPadding(5, 0, 5, 0); 
            	points[i] = imageView;   
            	//默认第一张图显示为选中状态
            	if (i == 0) {  
                    points[i].setBackgroundResource(R.drawable.btn_radio_on_holo_dark);  
                }else {  
                    points[i].setBackgroundResource(R.drawable.btn_radio_on_disabled_holo_dark);  
                }  
            	pageController.addView(points[i]);  
            }
          //绑定适配器
            portAdapter = new PageDataAdapter(context, viewPager, lstItems);
            viewPager.setAdapter(portAdapter);
            int index = 0;
            if(sp.contains("index"))
            	index = sp.getInt("index", 0);
            FyLog.v(TAG, "index is: " + index);
            viewPager.setCurrentItem(index);
            viewPager.setOnLongClickListener(this);
        }
	}
	
	private List<LuncherView> updateLuncherTable(Map<Integer, ScreenInfo> map){
    	/**
    	 * 在UI初始化前把脚本信息加入AppInfo
    	 */
    	List<LuncherView> items = new ArrayList<LuncherView>();
    	FyLog.e(TAG, "the map size:" + map.size());
    	/**竖屏**/
    	if(map.size() > 0){
    		for(int i = 0; i < map.size(); i++){
    			ScreenInfo screen = map.get(i);
    			if(screen != null){
    				items.add(getScreenPage(screen));
    			}else{
//    				int tmp = apps.size() % 12;
//    				for (int j = 0; j < apps.size() / 12; j++) {
//    					FyLog.e(TAG, "the apps is: " + apps.size());
//    					items.add(getOtherScreen(apps.subList(j*12, j*12 + 12)));
//					}
//    				if(tmp != 0)
//    					items.add(getOtherScreen(apps.subList(apps.size()- tmp, apps.size())));
    			}
        	}
    	} 
    	return items;
    }

	/**
	 * 竖屏获取每屏的应用列表
	 * @param apps
	 * @return
	 */
	@SuppressLint("ResourceAsColor")
	private LuncherView getScreenPage(ScreenInfo screen){
//		FyLog.v(TAG, "the row size is: " + screen.getMap().size());
		LuncherView table = new LuncherView(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		List<TipView> list = new ArrayList<TipView>();
		if(screen != null){
			FyLog.v(TAG, "the screen index is: " + screen.getScreen_index());
			Map<Integer, Map<Integer, AppInfo>> map = screen.getMap();
			//主屏自定义布局
			for (int i = 0; i < map.size(); i++) {
				Map<Integer, AppInfo> row = map.get(i);
				FyLog.v(TAG, "the cloum size is: " + row.size());
				for (int j = 0; j < row.size(); j++) {
					AppInfo app = row.get(j);
					TipView tip =  new TipView(context);
					tip.setWeightType(app.weight);
			    	tip.setPositionY(app.position_y);
			    	tip.setPositionX(app.position_x);
			    	tip.setHeight(main_height / 3);
//					if(app.getIntent() == null){
//			    		tip.setEnabled(false);
//			    	}else{
//			    		tip.setOnPressScreenListener(new onPressScreenListener(app));
//						tip.setImageBitmap( app.iconBitmap);
//						tip.setTextSize(app.label_size);
//						if(app.title != null)
//							tip.setText( app.title.toString());
//						tip.setBackgroundColor(app.back_color);
//						FyLog.v(TAG, "the color is: " + String.format("%08x", app.back_color));
//			    	}
					list.add(tip);
				}
			}
		}
    	table.addAppInfoData(list);
    	table.addMainTipView(context);
        return table;
    }
	/**
	 * 竖屏获取其他全部应用
	 * @param apps
	 * @return
	 */
	private LuncherView getOtherScreen(List<AppInfo> apps){
		
		LuncherView table = new LuncherView(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		List<TipView> list = new ArrayList<TipView>();
		
		int cnt = 0;
		for (int i = 0; i < apps.size() / 3; i++) {
			for (int j = 0; j < 3; j++) {
				AppInfo app = apps.get(cnt++);
				TipView tip =  new TipView(context);
				if(app != null){
					tip.setHeight(main_height / 4);
//					tip.setImageBitmap( app.iconBitmap);
//					tip.setTextSize(20);
//					tip.setText( app.title.toString());
					tip.setWeightType((float) (1.0 / 3));
					tip.setBackgroundColor(Color.WHITE);
			    	tip.setPositionY(i);
			    	tip.setPositionX(j);
//			    	if(app.getIntent() == null){
//			    		tip.setEnabled(false);
//			    	}
				}
				list.add(tip);
			}
		}
		int tmp = apps.size() % 3;
		if(tmp != 0){
			for (int i = 0; i < tmp; i++) {
				AppInfo app = apps.get(cnt++);
				TipView tip =  new TipView(context);
				if(app != null){
					tip.setHeight(main_height / 4);
//					tip.setImageBitmap( app.iconBitmap);
					tip.setTextSize(20);
//					tip.setText( app.title.toString());
					tip.setWeightType((float) (1.0 / 3));
					tip.setBackgroundColor(Color.WHITE);
			    	tip.setPositionY(apps.size() / 3);
			    	tip.setPositionX(i);
//			    	if(app.getIntent() == null){
//			    		tip.setEnabled(false);
//			    	}
				}
				list.add(tip);
			}
			for (int i = 0; i < 3 - tmp; i++) {
				TipView tip =  new TipView(context);
				tip.setHeight(main_height / 4);
				tip.setWeightType((float) (1.0 / 3));
		    	tip.setPositionY(apps.size() / 3);
		    	tip.setPositionX(tmp + i);
		    	tip.setEnabled(false);
		    	tip.setBackgroundColor(Color.BLACK);
				list.add(tip);
			}
		}
		table.addAppInfoData(list);
    	table.addMainTipView(context);
    	
		return table;
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		default:
			break;
		}
		
	}
	
/**================ 滑页事件监听 ============================================================ */	
	@Override
	public boolean onLongClick(View arg0) {
		// TODO Auto-generated method stub
		
		return false;
	}
	
	
	//pageView监听器
    class GuidePageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}
		@Override
		//如果切换了，就把当前的点点设置为选中背景，其他设置未选中背景
		public void onPageSelected(int idx) {
			// TODO Auto-generated method stub
			FyLog.v(TAG, "scroll index is: " + idx);
			editor.putInt("index", idx);
			editor.commit();
			if(idx < points.length)
			for(int i=0;i<points.length;i++){
				points[idx].setBackgroundResource(R.drawable.btn_radio_on_holo_dark);
				if (idx != i) {  
	                points[i].setBackgroundResource(R.drawable.btn_radio_on_disabled_holo_dark);  
	            }  
			}
			lstItems.get(idx).setShowMsg("第" + (idx+1) + "屏");
		}
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "onResume()");
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "onDestroy()");
		if(editor != null){
			editor.putInt("index", 0);
			editor.commit();
		}
		super.onDestroy();
	}

}
















