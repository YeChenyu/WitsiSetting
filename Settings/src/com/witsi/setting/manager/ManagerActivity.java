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
	//����
	private MyViewPager viewPager;
	private PageDataAdapter portAdapter;
	private List<LuncherView> lstItems;
	//��������LinearLayout
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
        //��ȡ��Ļ�ߴ��С
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
		((TextView)action_back.findViewById(R.id.tv)).setText("�û�����");
		tool = (LinearLayout) findViewById(R.id.tool);
		background = (LinearLayout) findViewById(R.id.background);
		
		viewPager = (MyViewPager) findViewById(R.id.viewPager);
		findViewById(R.id.btn).setOnClickListener(this);
		findViewById(R.id.btn1).setOnClickListener(this);
		findViewById(R.id.btn2).setOnClickListener(this);
		findViewById(R.id.btn3).setOnClickListener(this);
		
		/**���������ҳ����ؼ�**/
    	FyLog.d(TAG, "init the portrail views");
        viewPager = (MyViewPager) findViewById(R.id.viewPager);
        //������
        pageController = (ViewGroup)findViewById(R.id.pageController); 
        //�����л�����
        viewPager.setTransitionEffect(TransitionEffect.Standard);
        //�󶨼����¼�
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
        	/**����**/
            //��ȡData Item����
            lstItems = updateLuncherTable(map, clickable);
            FyLog.d(TAG, "get the app data");
    	    //�ж�����ͼ���ж��ٸ����
            points = new ImageView[lstItems.size()];
            pageController.removeAllViews();
            for(int i =0;i<lstItems.size();i++){
            	imageView = new ImageView(context);
            	imageView.setLayoutParams(new LayoutParams(30,30));
            	imageView.setPadding(5, 0, 5, 0); 
            	points[i] = imageView;   
            	//Ĭ�ϵ�һ��ͼ��ʾΪѡ��״̬
            	if (i == 0) {  
                    points[i].setBackgroundResource(R.drawable.btn_radio_on_holo_dark);  
                }else {  
                    points[i].setBackgroundResource(R.drawable.btn_radio_on_disabled_holo_dark);  
                }  
            	pageController.addView(points[i]);  
            }
          //��������
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
    	 * ��UI��ʼ��ǰ�ѽű���Ϣ����AppInfo
    	 */
    	List<LuncherView> items = new ArrayList<LuncherView>();
    	FyLog.e(TAG, "the map size:" + map.size());
    	/**����**/
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
		//��Ļ����
		case R.id.btn:{
			final ScreenConfigLayout view = new ScreenConfigLayout(context, map.size());
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("���õ�ǰ��Ļ����");
			dialog.setView(view.getView());
			dialog.setPositiveButton("ȡ��", null);
			dialog.setNegetiveButton("ȷ��", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					FyLog.d(TAG, "the select is: " + view.getSpinnerSelected());
					int index = sp.getInt("index", -1);
					//���õ�ǰ��Ļ����
					if(index > -1){
						map.get(index).setRow_num(view.getSpinnerSelected());
						updateViews(map, true);
					}
				}
			});
			dialog.show(0);
		}break;
		//�༭��Ļ
		case R.id.btn3:{
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("��Ļ��ɾ");
			dialog.setPositiveButton("ɾ����ǰ��", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					int index = sp.getInt("index", -1);
					//ȷ����һ������ɾ
					if(index > 0 ){
						//�м���ɾ��
						if(index < map.size()-1){
							//���������ǰ��
							for (int i = index+1; i < map.size(); i++) {
								ScreenInfo screen = map.get(i);
								screen.setScreen_index(i-1);
								map.put(i-1, screen);
							}
							//ɾ�����һ��
							map.remove(map.size()-1);
					        viewPager.setCurrentItem(index);
					        updateViews(map, true);
						//ĩ��ɾ��
						}else{
							map.remove(index);
					        viewPager.setCurrentItem(index - 1);
					        updateViews(map, true);
						}
					}else{
						if(index == 0){
							Toast.makeText(context, "�������ܱ�ɾ��", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(context, "����", Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
			dialog.setMultiButton("ȡ��", null);
			dialog.setNegetiveButton("����һ��", new OnDialogClickListener() {
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
		//����Ԥ��
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
		//��������
		case R.id.btn2:{
			MyDialog dialog = new MyDialog(context);
			dialog.setTitle("���浱ǰ���ò�����");
			dialog.setPositiveButton("ȡ��", null);
			dialog.setNegetiveButton("ȷ��", new OnDialogClickListener() {
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
		//���Ӧ��
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
				//�и���
				FyLog.d(TAG, "11111 the row map is already exist");
				screen.getMap().get(app.position_y).put(app.position_x, app);
				FyLog.e(TAG, "the row map is already exist and the row is: " + screen.getMap().get(app.position_y).size());
			}else{
				//����һ��
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
				//�и���
				FyLog.d(TAG, "22222 the row map is already exist and the row is: " + screen.getMap().get(app.position_y).size());
				screen.getMap().get(app.position_y).put(app.position_x, app);
			}else{
				//����һ��
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
		//�жϵ�ǰ���Ƿ��п���λ�ã����û�У�����Ӱ�ť���ɼ�
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
					//���������һ�л��п���λ�ã����ж�������
					if(weight < 0.99f){
						add.setVisibility(View.VISIBLE);
						break;
					}
					//ֱ�����һ��Ϊֹ
					if(i == row-1){
						add.setVisibility(View.GONE);
						FyLog.d(TAG, "the visibility is gone");
					}
					weight = 0;
				}
			}
		}
	}
	
/**================ ��ҳ�¼����� ============================================================ */	
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
		//����л��ˣ��Ͱѵ�ǰ�ĵ������Ϊѡ�б�������������δѡ�б���
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
			lstItems.get(idx).setShowMsg("��" + (idx+1) + "��");
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

/** ================= �����¼����� ========================================================================*/   
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
			dialog.setTitle("�Ƴ���ǰ���ã�");
			dialog.setPositiveButton("ȡ��", null);
			dialog.setNegetiveButton("ȷ��", new OnDialogClickListener() {
				@Override
				public void onClick() {
					// TODO Auto-generated method stub
					if(true){
						//��ȡ������
						Map<Integer, AppInfo> mapRow = map.get(sp.getInt("index", -1))
								.getMap().get(mAppInfo.position_y);
						FyLog.d(TAG, "the remove row size is: " + mapRow.size());
						//ɾ����ǰtip
						if(mapRow.containsValue(mAppInfo)){
							mapRow.remove(mAppInfo.position_x);
							FyLog.d(TAG, "the remove position is: " + mAppInfo.position_x);
							//ɾ����Ӧ����
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
	 * ������ȡÿ����Ӧ���б�
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
			//����ǵ�һ���������
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
			//��ȡÿ��������
			Map<Integer, Map<Integer, AppInfo>> map = screen.getMap();
			//����ÿ��������ȥ����
			for (int i = 0; i < screen.getRow_num(); i++) {
				//�Ѵ��ڸ���
				FyLog.d(TAG, "the row is: " + i);
				if(map != null && map.containsKey(i)){
					//��ȡ���е�����
					Map<Integer, AppInfo> row = map.get(i);
					FyLog.v(TAG, "the cloum size is: " + row.size());
					//��ȡ�б�������
					if(row != null){
						if(row.size() != 0){
							Set<Integer>set = row.keySet();	
							int clum = 0;
							boolean isInsert = false;
							for (Iterator iterator = set.iterator(); iterator.hasNext();) {
								//��ȡ �� ����
								Integer j = (Integer) iterator.next();
								AppInfo app = row.get(j);
								//�������겻��ȣ����������
								FyLog.e(TAG, "the tip x is: " + i + " y: " + j + " clum: " + clum);
								//�ж����Ƿ�����п�ʼ
								if(j != 0 && j != clum){
									for (int j2 = clum; j2 < j; j2++) {
										clum++;
										FyLog.e(TAG, "the extern insert tip x is: " + i + " y: " + j2);
										//�½�һ�е�һ�е�����
										TipView tip =  new TipView(context);
										tip.setWeightType(row.get(j).weight);
								    	tip.setPositionY(i);
								    	tip.setPositionX(j2);
								    	tip.setHeight(content_height / (screen.getRow_num()));
								    	tip.setVisibility(View.INVISIBLE);
										list.add(tip);
									}
								}
								//��ȡ��Ӧ���������
								clum++;
								//���ò���
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
										//���ñ߿�󱳾���ɫ��������
//											tip.setBoundary(2);
										FyLog.v(TAG, "the color is: " + String.format("%08x", app.back_color));
							    	}
									//��ӵ������б�
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
				//�����ڸ���
				}else{
					//�½�һ�е�һ�е�����
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
	 * ������ȡÿ����Ӧ���б�
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
			//����ǵ�һ���������
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
			//��ȡÿ��������
			Map<Integer, Map<Integer, AppInfo>> map = screen.getMap();
			//����ÿ��������ȥ����
			FyLog.d(TAG, "the row is: " + screen.getRow_num());
			for (int i = 0; i < screen.getRow_num(); i++) {
				//�Ѵ��ڸ���
				if(map != null && map.containsKey(i)){
					//��ȡ���е�����
					Map<Integer, AppInfo> row = map.get(i);
					FyLog.v(TAG, "the cloum size is: " + row.size());
					//��ȡ�б�������
					if(row != null){
						Set<Integer>set = row.keySet();	
						for (Iterator iterator = set.iterator(); iterator.hasNext();) {
							//��ȡ �� ����
							Integer j = (Integer) iterator.next();
							//��ȡ��Ӧ���������
							AppInfo app = row.get(j);
							//���ò���
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
									//���ñ߿�󱳾���ɫ��������
//									tip.setBoundary(2);
									FyLog.v(TAG, "the color is: " + String.format("%08x", app.back_color));
								}
								//��ӵ������б�
								list.add(tip);
							}
						}
					}
					//�����ڸ���
				}else{
					//�½�һ�е�һ�е�����
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
	 * ������ȡ����ȫ��Ӧ��
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
