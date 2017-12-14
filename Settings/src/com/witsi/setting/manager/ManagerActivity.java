package com.witsi.setting.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.views.MyDialog;
import com.witsi.views.MyDialog.OnDialogClickListener;
import com.witsi.setting.manager.MyViewPager.TransitionEffect;
import com.witsi.setting.manager.TipView.OnPressScreenListener;
import com.witsi.setting.manager.luncher.AppInfo;
import com.witsi.setting.manager.luncher.ScreenInfo;
import com.witsi.setting.manager.luncher.WorkSpace;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ManagerActivity extends Activity implements 
									OnLongClickListener, OnClickListener{

	private String TAG = ManagerActivity.class.getSimpleName();
	private Context context = ManagerActivity.this;
	
	private View action_back;
	private LinearLayout tool;
	private TextView marquee;
	private ImageView add;
	private LinearLayout background;
	//竖屏
	private MyViewPager viewPager;
	private PageDataAdapter portAdapter;
	private List<LuncherView> lstItems;
	//包裹点点的LinearLayout
	private ViewGroup   pageController;
	private ImageView   imageView; 
	private ImageView[] points; 	
		
	private Display dis = null;
	private Rect r = new Rect();
	private boolean isLandscape = false;
	private int main_height = 0, content_height = 0;
	private int main_width = 0;
	
    private SharedPreferences sp = null;
    private Editor editor = null;
    private WorkSpace workSpace;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manager_activity);
		if(savedInstanceState == null)
			FyLog.e(TAG, "the bundle is null:");
		
		sp = getSharedPreferences("config", 0);
        editor = sp.edit();
        workSpace = new WorkSpace(context);
        //获取屏幕尺寸大小
  		dis = getWindowManager().getDefaultDisplay();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        FyLog.v(TAG, "the width is: " + dis.getWidth() + "the height is: " + dis.getHeight());
        main_width = dis.getWidth() ; 
        if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102)
        	main_height = dis.getHeight() - r.top - 200;
        if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029)
        	main_height = dis.getHeight() - r.top - 190;
        FyLog.v(TAG, "the top is: " + r.top + "the main_width is: " + main_width + "the mainheight is: " + main_height);
        if(dis.getWidth() > dis.getHeight())
        	isLandscape = true;
        else
        	isLandscape = false;
        
        
		initViews();
		
		initDatas();
	}

	private void initViews() {
		// TODO Auto-generated method stub
		action_back = findViewById(R.id.action_back);
		add = (ImageView) findViewById(R.id.action_back).findViewById(R.id.iv);
		add.setOnClickListener(this);
		action_back.findViewById(R.id.ll_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		((TextView)action_back.findViewById(R.id.tv)).setText("用户管理");
		tool = (LinearLayout) findViewById(R.id.tool);
		background = (LinearLayout) findViewById(R.id.background);
		
		viewPager = (MyViewPager) findViewById(R.id.viewPager);
		findViewById(R.id.btn).setOnClickListener(this);
		findViewById(R.id.btn1).setOnClickListener(this);
		findViewById(R.id.btn2).setOnClickListener(this);
		findViewById(R.id.btn3).setOnClickListener(this);
		
		/**竖屏构造分页浏览控件**/
    	FyLog.d(TAG, "init the portrail views");
        viewPager = (MyViewPager) findViewById(R.id.viewPager);
        //构造点点
        pageController = (ViewGroup)findViewById(R.id.pageController); 
        //设置切换动画
        viewPager.setTransitionEffect(TransitionEffect.Standard);
        //绑定监听事件
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());
        viewPager.setOnLongClickListener(this);
	}

	@SuppressLint("NewApi")
	private void initDatas() {
		// TODO Auto-generated method stub
//		List<AppInfo> list = new ArrayList<AppInfo>();
//		List<ResolveInfo> tmp = getThirdAppInfo();
//		for (ResolveInfo resolveInfo : tmp) {
//			AppInfo info = new AppInfo();
//			info.activityInfo = resolveInfo.activityInfo;
//			info.filter = resolveInfo.filter;
//			info.icon = resolveInfo.icon;
//			info.labelRes = resolveInfo.labelRes;
//			info.resolvePackageName = resolveInfo.resolvePackageName;
//			list.add(info);
//		}
		map = workSpace.loadWorkspace();
        updateViews(map, true);
	}
	
	private Map<Integer, ScreenInfo> map;
	private void updateViews(Map<Integer, ScreenInfo> map, boolean clickable)
	{
		FyLog.d(TAG, "exec updateViews()");
        if(!isLandscape){
        	/**竖屏**/
            //获取Data Item数据
            lstItems = updateLuncherTable(map, clickable);
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
            else{
            	editor.putInt("index", 0);
            	editor.commit();
            }
            FyLog.v(TAG, "index is: " + index);
            viewPager.setCurrentItem(index);
        }
        //
        initAddScreenButton();
	}
	
	private List<LuncherView> updateLuncherTable(Map<Integer, ScreenInfo> map, boolean clickable){
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
    				items.add(getScreenPage(screen, clickable));
    			}
        	}
    	} 
    	return items;
    }
	
	int i = 2;
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//屏幕设置
		case R.id.btn:{
			final ScreenConfigLayout view = new ScreenConfigLayout(context, map.size());
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("设置当前屏幕配置");
			dialog.setView(view.getView());
			dialog.setPositiveButton("取消", null);
			dialog.setNegetiveButton("确定", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					FyLog.d(TAG, "the select is: " + view.getSpinnerSelected());
					int index = sp.getInt("index", -1);
					//设置当前屏幕行数
					if(index > -1){
						map.get(index).setRow_num(view.getSpinnerSelected());
						updateViews(map, true);
					}
				}
			});
			dialog.show(0);
		}break;
		//编辑屏幕
		case R.id.btn3:{
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("屏幕增删");
			dialog.setPositiveButton("删除当前屏", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					int index = sp.getInt("index", -1);
					//确保第一屏不被删
					if(index > 0 ){
						//中间屏删除
						if(index < map.size()-1){
							//将后面的往前移
							for (int i = index+1; i < map.size(); i++) {
								ScreenInfo screen = map.get(i);
								screen.setScreen_index(i-1);
								map.put(i-1, screen);
							}
							//删除最后一项
							map.remove(map.size()-1);
					        viewPager.setCurrentItem(index);
					        updateViews(map, true);
						//末屏删除
						}else{
							map.remove(index);
					        viewPager.setCurrentItem(index - 1);
					        updateViews(map, true);
						}
					}else{
						if(index == 0){
							Toast.makeText(context, "主屏不能被删除", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(context, "请重", Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
			dialog.setMultiButton("取消", null);
			dialog.setNegetiveButton("新增一屏", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					FyLog.d(TAG, "add the new screen");
					ScreenInfo screen = new ScreenInfo(100);
					screen.setScreen_index(map.size());
					screen.setRow_num(3);
					map.put(map.size(), screen);
			        updateViews(map, true);
			        viewPager.setCurrentItem(map.size());
				}
			});
			dialog.show(0);
		}break;
		//桌面预览
		case R.id.btn1:{
			main_height = (dis.getHeight() - 42 - r.top);
			updateViews(map, false);
			action_back.setVisibility(View.GONE);
			background.setBackgroundResource(R.drawable.trade_back9);
			tool.setVisibility(View.GONE);
			for (int i = 0; i < lstItems.size(); i++) {
				lstItems.get(i).setShowVisibility(View.GONE);
				lstItems.get(i).setAddVisibility(View.VISIBLE);
			}
		}break;
		//保存配置
		case R.id.btn2:{
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("保存当前配置并启用");
			dialog.setPositiveButton("取消", null);
			dialog.setNegetiveButton("确定", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					workSpace.saveConfiguration(map);
					Intent intent = new Intent();
					intent.setAction("com.witsi.setting.manager.luncher.CONFIGURATION_CHANGED");
					sendBroadcast(intent);
					initDatas();
				}
			});
			dialog.show(0);
		}break;
		//添加应用
		case R.id.iv:{
			Intent intent = new Intent(context, ConfigActivity.class);
			ScreenInfo screen = map.get(sp.getInt("index", -1));
			intent.putExtra("request", true);
			intent.putExtra("screen", screen);
			startActivityForResult(intent, 0);
		}break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode == 1){
			AppInfo app = (AppInfo)data.getParcelableExtra("app");
			FyLog.d(TAG, "add the config get the app info: " + app.componentName.getPackageName()+":"+app.componentName.getClassName());
			ScreenInfo screen = map.get(app.position_screen);
			if(screen.getMap().containsKey(app.position_y)){
				//有该行
				FyLog.d(TAG, "11111 the row map is already exist");
				screen.getMap().get(app.position_y).put(app.position_x, app);
				FyLog.e(TAG, "the row map is already exist and the row is: " + screen.getMap().get(app.position_y).size());
			}else{
				//新增一行
				FyLog.d(TAG, "add the new row");
				FyLog.d(TAG, "get the app info: " + app.title);
				Map<Integer, AppInfo> mapRow = new HashMap<Integer, AppInfo>();
				screen.getMap().put(app.position_y, mapRow);
				mapRow.put(app.position_x, app);
			}
		}else if(resultCode == 2){
			AppInfo app = (AppInfo)data.getParcelableExtra("app");
			FyLog.d(TAG, "set the config get the app info: " + app.title + " the x is: " + app.position_x + " the y is: " + app.position_y);
			FyLog.d(TAG, "the index is: " + sp.getInt("index", -1));
			ScreenInfo screen = map.get(sp.getInt("index", -1));
			if(screen.getMap().containsKey(app.position_y)){
				//有该行
				FyLog.d(TAG, "22222 the row map is already exist and the row is: " + screen.getMap().get(app.position_y).size());
				screen.getMap().get(app.position_y).put(app.position_x, app);
			}else{
				//新增一行
				FyLog.d(TAG, "add the new row");
				FyLog.d(TAG, "get the app info: " + app.title);
				Map<Integer, AppInfo> mapRow = new HashMap<Integer, AppInfo>();
				screen.getMap().put(app.position_y, mapRow);
				mapRow.put(app.position_x, app);
			}
		}
		
		updateViews(map, true);
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initAddScreenButton() {
		int index = sp.getInt("index", -1);
		FyLog.d(TAG, "the screen index is: " + index);
		float weight = 0; 
		//判断当前屏是否还有空余位置，如果没有，则将添加按钮不可见
		if(index >= 0 && map.containsKey(index)){
			ScreenInfo info = map.get(index);
			int row = info.getRow_num();
			Map<Integer, Map<Integer, AppInfo>> maps = info.getMap();
			if(maps.size() < row){
				FyLog.e(TAG, "88888888888888888888888888888888");
				add.setVisibility(View.VISIBLE);
			}
			FyLog.e(TAG, "the row is: " +row + " true row is: " + maps.size());
			for (int i = 0; i < row; i++) {
				Map<Integer, AppInfo> mapp = maps.get(i);
				if(mapp != null){
					FyLog.d(TAG, "the clum is: " + mapp.size());
					Set<Integer> clum = mapp.keySet();
					for (Iterator iterator = clum.iterator(); iterator.hasNext();) {
						Integer j = (Integer) iterator.next();
						AppInfo app = mapp.get(j);
						if(app != null && app.getIntent() != null){
							weight += app.weight;
							FyLog.d(TAG, "the app is: " + app.title);
						}
					}
					FyLog.d(TAG, "the weight is: " + weight);
					//如果有其中一行还有空余位置，则不判断其他行
					if(weight < 0.99f){
						add.setVisibility(View.VISIBLE);
						break;
					}
					//直到最后一行为止
					if(i == row-1){
						add.setVisibility(View.GONE);
						FyLog.d(TAG, "the visibility is gone");
					}
					weight = 0;
				}
			}
		}
	}
	
/**================ 滑页事件监听 ============================================================ */	
	@Override
	public boolean onLongClick(View arg0) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "on long click");
		map.remove(map.get(sp.getInt("index", 0)));
		updateViews(map, true);
		return false;
	}
	
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
			if(tool.getVisibility() != View.GONE)
				initAddScreenButton();
		}
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	if(tool.getVisibility() == View.GONE){
			action_back.setVisibility(View.VISIBLE);
			tool.setVisibility(View.VISIBLE);
			background.setBackgroundResource(R.color.white);
//			for (int i = 0; i < lstItems.size(); i++) {
//				lstItems.get(i).setShowVisibility(View.VISIBLE);
//			}
			if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102)
				main_height = lstItems.get(0).getLayoutHeight() - 200;
	        if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029)
	        	main_height = lstItems.get(0).getLayoutHeight() - 190;
			
			FyLog.d(TAG, "the layout height is: " + main_height);
			updateViews(map, true);
    	}else{
    		super.onBackPressed();
    	}
    }

/** ================= 磁贴事件监听 ========================================================================*/   
    class onPressScreenListener implements OnPressScreenListener
    {
    	AppInfo mAppInfo;
    	public onPressScreenListener(AppInfo appInfo)
    	{
    		mAppInfo = appInfo;
    	}
    	
    	public void setAppInfo(AppInfo info){
    		mAppInfo = info;
    	}
		@Override
		public void onShortClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(context, ConfigActivity.class);
			intent.putExtra("app", mAppInfo);
			intent.putExtra("request", false);
			FyLog.d(TAG, "the request is false");
			startActivityForResult(intent, 1);
		}


		@Override
		public void onDelPess(View v) {
			// TODO Auto-generated method stub
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("移除当前配置？");
			dialog.setPositiveButton("取消", null);
			dialog.setNegetiveButton("确定", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					if(true){
						//获取行数据
						Map<Integer, AppInfo> mapRow = map.get(sp.getInt("index", -1))
								.getMap().get(mAppInfo.position_y);
						FyLog.d(TAG, "the remove row size is: " + mapRow.size());
						//删除当前tip
						if(mapRow.containsValue(mAppInfo)){
							mapRow.remove(mAppInfo.position_x);
							FyLog.d(TAG, "the remove position is: " + mAppInfo.position_x);
							//删除对应索引
							map.get(sp.getInt("index", -1)).removeMapRow(mAppInfo);
						}
						updateViews(map, true);
					}
				}
			});
			dialog.show(0);
		}

		@Override
		public void setAppInfo(com.witsi.setting.manager.AppInfo info) {
			// TODO Auto-generated method stub
			
		}
    }
    /**
	 * 竖屏获取每屏的应用列表
	 * @param apps
	 * @return
	 */
	@SuppressLint("ResourceAsColor")
	private LuncherView getScreenPage(ScreenInfo screen, boolean clickable){
		LuncherView table = new LuncherView(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		table.setAddOnClickListener(this);
		List<TipView> list = new ArrayList<TipView>();
		if(screen != null){
			FyLog.e(TAG, "the screen index is: " + screen.getScreen_index());
			//如果是第一屏，则添加
			if(screen.getScreen_index() == 0){
				View v = LayoutInflater.from(context).inflate(R.layout.manager_logo_main, null);
				marquee = (TextView) v.findViewById(R.id.tv);
				marquee.setMovementMethod(ScrollingMovementMethod.getInstance());
				TableRow.LayoutParams lp = new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
				content_height = main_height - 250;
				lp.height = 250;
				lp.weight = 1.0f;
				v.setLayoutParams(lp);
				table.addLogo(context, v);
			}else{
				content_height = main_height;
			}
			//获取每屏的数据
			Map<Integer, Map<Integer, AppInfo>> map = screen.getMap();
			//根据每屏的行数去遍历
			for (int i = 0; i < screen.getRow_num(); i++) {
				//已存在该行
				FyLog.d(TAG, "the row is: " + i);
				if(map != null && map.containsKey(i)){
					//获取该行的数据
					Map<Integer, AppInfo> row = map.get(i);
					FyLog.v(TAG, "the cloum size is: " + row.size());
					//获取行遍历对象
					if(row != null){
						if(row.size() != 0){
							Set<Integer>set = row.keySet();	
							int clum = 0;
							boolean isInsert = false;
							for (Iterator iterator = set.iterator(); iterator.hasNext();) {
								//获取 列 索引
								Integer j = (Integer) iterator.next();
								AppInfo app = row.get(j);
								//若行坐标不想等，则添加新行
								FyLog.e(TAG, "the tip x is: " + i + " y: " + j + " clum: " + clum);
								//判断列是否从首列开始
								if(j != 0 && j != clum){
									for (int j2 = clum; j2 < j; j2++) {
										clum++;
										FyLog.e(TAG, "the extern insert tip x is: " + i + " y: " + j2);
										//新建一行第一列的数据
										TipView tip =  new TipView(context);
										tip.setWeightType(row.get(j).weight);
								    	tip.setPositionY(i);
								    	tip.setPositionX(j2);
								    	tip.setHeight(content_height / (screen.getRow_num()));
								    	tip.setVisibility(View.INVISIBLE);
										list.add(tip);
									}
								}
								//获取对应坐标的数据
								clum++;
								//设置参数
								if(app != null){
									TipView tip =  new TipView(context);
									tip.setWeightType(app.weight);
							    	tip.setPositionY(app.position_y);
							    	tip.setPositionX(app.position_x);
							    	tip.setHeight(content_height / (screen.getRow_num()));
									if(app.getIntent() == null){
							    		tip.setEnabled(false);
							    	}else{
							    		if(clickable)
							    			tip.setOnPressScreenListener(new onPressScreenListener(app));
							    		if(app.bitIcon != null)
							    			tip.setImageBitmap( app.bitIcon);
							    		else
							    			tip.setImageBitmap( app.iconBitmap);
										FyLog.i(TAG, "the icon is: " + app.icons);
										tip.setTextSize(app.label_size);
										if(app.title != null)
											tip.setText( app.title.toString());
//											tip.setBackgroundColor(app.back_color);
										//设置边框后背景颜色不起作用
//											tip.setBoundary(2);
										FyLog.v(TAG, "the color is: " + String.format("%08x", app.back_color));
							    	}
									//添加到数据列表
									list.add(tip);
								}
							}
						}else{
							FyLog.e(TAG, "screen: " + screen.getScreen_index() + " the row "+ i +"is not exist");
							TipView tip =  new TipView(context);
							tip.setWeightType(1);
					    	tip.setPositionY(i);
					    	tip.setPositionX(0);
					    	tip.setHeight(content_height / (screen.getRow_num()));
					    	tip.setVisibility(View.INVISIBLE);
							list.add(tip);
						}
					}
				//不存在该行
				}else{
					//新建一行第一列的数据
					TipView tip =  new TipView(context);
					tip.setWeightType(1);
			    	tip.setPositionY(i);
			    	tip.setPositionX(0);
			    	tip.setHeight(content_height / (screen.getRow_num()));
			    	tip.setVisibility(View.INVISIBLE);
					list.add(tip);
				}
			}
		}
    	table.addAppInfoData(list);
    	table.addMainTipView(context);
    	
        return table;
    }
	/**
	 * 竖屏获取每屏的应用列表
	 * @param apps
	 * @return
	 */
	@SuppressLint("ResourceAsColor")
	private LuncherView getScreenPage1(ScreenInfo screen, boolean clickable){
		LuncherView table = new LuncherView(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		table.setAddOnClickListener(this);
		List<TipView> list = new ArrayList<TipView>();
		if(screen != null){
			FyLog.v(TAG, "the screen index is: " + screen.getScreen_index());
			//如果是第一屏，则添加
			if(screen.getScreen_index() == 0){
				View v = LayoutInflater.from(context).inflate(R.layout.manager_logo_main, null);
				marquee = (TextView) v.findViewById(R.id.tv);
				marquee.setMovementMethod(ScrollingMovementMethod.getInstance());
				TableRow.LayoutParams lp = new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
				content_height = main_height - 250;
				lp.height = 250;
				lp.weight = 1.0f;
				v.setLayoutParams(lp);
				table.addLogo(context, v);
			}else{
				content_height = main_height;
			}
			//获取每屏的数据
			Map<Integer, Map<Integer, AppInfo>> map = screen.getMap();
			//根据每屏的行数去遍历
			FyLog.d(TAG, "the row is: " + screen.getRow_num());
			for (int i = 0; i < screen.getRow_num(); i++) {
				//已存在该行
				if(map != null && map.containsKey(i)){
					//获取该行的数据
					Map<Integer, AppInfo> row = map.get(i);
					FyLog.v(TAG, "the cloum size is: " + row.size());
					//获取行遍历对象
					if(row != null){
						Set<Integer>set = row.keySet();	
						for (Iterator iterator = set.iterator(); iterator.hasNext();) {
							//获取 列 索引
							Integer j = (Integer) iterator.next();
							//获取对应坐标的数据
							AppInfo app = row.get(j);
							//设置参数
							if(app != null){
								TipView tip =  new TipView(context);
								tip.setWeightType(app.weight);
								tip.setPositionY(app.position_y);
								tip.setPositionX(app.position_x);
								tip.setHeight(content_height / (screen.getRow_num()));
								if(app.getIntent() == null){
									tip.setEnabled(false);
								}else{
									if(clickable)
										tip.setOnPressScreenListener(new onPressScreenListener(app));
									if(app.bitIcon != null)
										tip.setImageBitmap( app.bitIcon);
									else
										tip.setImageBitmap( app.iconBitmap);
									FyLog.i(TAG, "the icon is: " + app.icons);
									tip.setTextSize(app.label_size);
									if(app.title != null)
										tip.setText( app.title.toString());
//									tip.setBackgroundColor(app.back_color);
									//设置边框后背景颜色不起作用
//									tip.setBoundary(2);
									FyLog.v(TAG, "the color is: " + String.format("%08x", app.back_color));
								}
								//添加到数据列表
								list.add(tip);
							}
						}
					}
					//不存在该行
				}else{
					//新建一行第一列的数据
					TipView tip =  new TipView(context);
					tip.setWeightType(0.5f);
					tip.setPositionY(i);
					tip.setPositionX(1);
					tip.setHeight(content_height / (screen.getRow_num()));
					tip.setVisibility(View.INVISIBLE);
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
}
