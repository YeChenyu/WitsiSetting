package com.witsi.setting.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.android.internal.widget.DrawableHolder;
import com.witsi.adapter.ManagerGvAdapter;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.setting.manager.ImagePickLayout.OnImagePickItemClick;
import com.witsi.setting.manager.luncher.AppInfo;
import com.witsi.setting.manager.luncher.ScreenInfo;
import com.witsi.setting.manager.luncher.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.WorkSource;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigActivity extends Activity implements OnItemClickListener,
								OnClickListener{

	private String TAG = ConfigActivity.class.getSimpleName();
	private Context context = ConfigActivity.this;
	
	private GridView gv;
	private List<ResolveInfo> list;
	private ManagerGvAdapter<ResolveInfo> adapter;
	
	private ImageView icon;
	private Spinner sp_row, sp_clum, sp_weight, sp_font;
	private TextView tv, tv1, tv2, tv3;
	private Button btn, btn1;
	
	private ArrayAdapter<String> rowAdapter, clumAdapter,
						weightAdapter, fontAdapter;
	private List<String> lstRow, lstClom;
	
	private float[] rowWeight;
	int row = 0;
	ScreenInfo screen;
	AppInfo app;
	Bitmap bitMap;
	String StrIcon = "ic_launcher.png";
	private boolean request = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manager_config_activity);
		
		initViews();
		
		initDatas(savedInstanceState);
	}
	
	
	
	private void initViews() {
		// TODO Auto-generated method stub
		findViewById(R.id.action_back).findViewById(R.id.ll_back)
			.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		(findViewById(R.id.action_back).findViewById(R.id.sw)).setVisibility(View.GONE);
		((TextView)findViewById(R.id.action_back).findViewById(R.id.tv))
			.setText("应用配置");
		
		gv = (GridView) findViewById(R.id.gv);
		list = getThirdAppInfo();
		adapter = new ManagerGvAdapter<ResolveInfo>(context, list);
		gv.setAdapter(adapter);
		
		icon = (ImageView) findViewById(R.id.iv);
		sp_row = (Spinner) findViewById(R.id.sp);
		sp_clum = (Spinner) findViewById(R.id.sp1);
		sp_weight = (Spinner) findViewById(R.id.sp3);
		sp_font = (Spinner) findViewById(R.id.sp2);
		
		tv = (TextView) findViewById(R.id.tv1);
		tv1 = (TextView) findViewById(R.id.tv2);
		tv2 = (TextView) findViewById(R.id.tv3);
		tv3 = (TextView) findViewById(R.id.tv4);
		
		btn = (Button) findViewById(R.id.btn);
		btn1 = (Button) findViewById(R.id.btn1);
		
	}



	private void initDatas(Bundle bundle) {
		// TODO Auto-generated method stub
		//比重
		String[] arrWeight = {"33.3%", "50%", "100%" };
		weightAdapter = new ArrayAdapter<String>(context,
				R.layout.manager_spinner_item, R.id.tv, arrWeight);
		sp_weight.setAdapter(weightAdapter);
		//字体
		String[] arrFont = {"小", "普通", "大", "超大"};
		fontAdapter = new ArrayAdapter<String>(context,
				R.layout.manager_spinner_item, R.id.tv, arrFont);
		sp_font.setAdapter(fontAdapter);
		
		request = getIntent().getBooleanExtra("request", false);
		//请求添加配置
		if(request){
			//获取当前屏的数据
			screen = (ScreenInfo) getIntent().getParcelableExtra("screen");
			if(screen != null){
				FyLog.v(TAG, "the screen is not null and the size is: " + (screen.getMapRow()==null? 0 : screen.getMapRow().size()));
				//当前屏已经配置的就不显示
				for (int i = 0; i < list.size(); i++) {
					if(screen.getMapRow().containsValue(list.get(i).activityInfo.packageName)){
						FyLog.d(TAG, "remove the appinfo: " + list.get(i).activityInfo.packageName);
						list.remove(list.get(i));
						continue;
					}
				}
				adapter.notifyDataSetChanged();
/**  =========   =====================================================================*/
				//获取当前屏设置的行数 从1开始
				row = screen.getRow_num();
				rowWeight = new float[row];
				//判断每行是否有空位。如果没有，则改行不进行选择
				Set<String> set = screen.getMapRow().keySet();
				for (Iterator iterator = set.iterator(); iterator.hasNext();) {
					String str =  (String) iterator.next();
					FyLog.i(TAG, "the row key is: " + str);
					for (int i = 0; i < row; i++) {
						if(i == Integer.parseInt(str.substring(0, 1))){
							//计算每行的行比重
							rowWeight[i] += Float.parseFloat(str.substring(2, str.length()));
						}
					}
				}
				lstRow = new ArrayList<String>();
				lstClom = new ArrayList<String>();
				//剩余可配置的列。如果一行全部配置，则不显示该行
				for (int i = 0; i < row; i++) {
					FyLog.d(TAG, "the row weight is: " + rowWeight[i]);
					if(rowWeight[i] < 0.99f){
						lstRow.add("第" + (i+1) + "行");
					}
				}
				rowAdapter = new ArrayAdapter<String>(context,
						R.layout.manager_spinner_item, R.id.tv, lstRow);
				sp_row.setAdapter(rowAdapter);
				clumAdapter = new ArrayAdapter<String>(context,
						R.layout.manager_spinner_item, R.id.tv, lstClom);
				sp_clum.setAdapter(clumAdapter);
			}else{
				FyLog.v(TAG, "the screen is null and the size is: " + (screen.getMapRow()==null? 0 : screen.getMapRow().size()));
				//获取当前屏设置的行数 从1开始
				row = screen.getRow_num();
				lstRow = new ArrayList<String>();
				//对应的行，剩余列索引初始化 如果第一列已经配置，则不显示第一列
				lstClom = new ArrayList<String>();
				
				for (int i = 0; i < row; i++) {
					if(rowWeight[i] < 0.99f)
						lstRow.add("第" + (i+1) + "行");
				}
				rowAdapter = new ArrayAdapter<String>(context,
						R.layout.manager_spinner_item, R.id.tv, lstRow);
				sp_row.setAdapter(rowAdapter);
				for (int j = 0; j < 4; j++) {
					lstClom.add("第" + (j+1) + "列");
				}
				clumAdapter = new ArrayAdapter<String>(context,
						R.layout.manager_spinner_item, R.id.tv, lstClom);
				sp_clum.setAdapter(clumAdapter);
			}
			sp_font.setSelection(1);
		//请求修改指定配置	
		}else{
			//行索引初始化
			app = (AppInfo) getIntent().getParcelableExtra("app");
			FyLog.d(TAG, "the app is: " + app.title);
/**  =========   =====================================================================*/
			//获取当前屏设置的行数 从1开始
			lstRow = new ArrayList<String>();
			//对应的行，剩余列索引初始化 如果第一列已经配置，则不显示第一列
			lstClom = new ArrayList<String>();
			lstRow.add("第" + (app.position_y+1) + "行");
			lstClom.add("第" + (app.position_x+1) + "列");
			rowAdapter = new ArrayAdapter<String>(context,
					R.layout.manager_spinner_item, R.id.tv, lstRow);
			sp_row.setAdapter(rowAdapter);
			clumAdapter = new ArrayAdapter<String>(context,
					R.layout.manager_spinner_item, R.id.tv, lstClom);
			sp_clum.setAdapter(clumAdapter);
			
			tv2.setBackgroundColor(app.back_color);
			if(app.getIntent().getComponent() == null){
				tv.setText(app.componentName.getPackageName());
				tv1.setText(app.componentName.getClassName());
			}else{
				tv.setText(app.getIntent().getComponent().getPackageName());
				tv1.setText(app.getIntent().getComponent().getClassName());
			}
			if(app.bitIcon == null){
				icon.setImageBitmap(app.iconBitmap);
				bitMap = app.iconBitmap;
			}else{
				icon.setImageBitmap(app.bitIcon);
				bitMap = app.bitIcon;
			}
			StrIcon = app.icons;
			
			setSpinnerWeight(app.weight);
			setSpinnerFont(app.label_size);                                                       
		}
		gv.setOnItemClickListener(this);
		sp_font.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					tv3.setTextSize(15);
					break;
				case 1:
					tv3.setTextSize(20);
					break;
				case 2:
					tv3.setTextSize(25);
					break;
				case 3:
					tv3.setTextSize(35);
					break;
				default:
					break;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		sp_row.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(screen != null){
					//行
					lstClom.clear();
					int row = Integer.parseInt(((String)sp_row.getSelectedItem()).substring(1, 2));
					FyLog.d(TAG, "select the row is: " + row);
					//判断每行是否有空位。如果没有，则改行不进行选择
					Set<String> set = screen.getMapRow().keySet();
					int clum = 0;
					String weight = "0.333";
					boolean isExist = true;
					for (Iterator iterator = set.iterator(); iterator.hasNext();) {
						String str =  (String) iterator.next();
						FyLog.i(TAG, "the row is: " + str + (row-1));
						//判断当前行列和比重是否为空
						if(Integer.parseInt(str.substring(0, 1)) == row-1){
							isExist = true;
							clum++;
							weight = str.substring(2, str.length());
							break;
						}else{
							isExist = false;
						}
					}
					//该行无数据，添加默认列数
					if(!isExist){
						FyLog.i(TAG, "the row key is null");
						for (int j = 0; j < 3; j++) {
							lstClom.add("第" + (j+1) + "列");
						}
					}else{
						FyLog.i(TAG, "the row key is exist");
						//计算每行的个数
						int i = (int) (1 / Float.parseFloat(weight)) ;
						FyLog.i(TAG, "the weight is: " + weight + " the i is: " + i);
						for (int j = 0; j < i; j++) {
							FyLog.i(TAG, "the weight is: |" + "" + (row-1) + j + weight + "|");
							if(!screen.getMapRow().containsKey("" + (row-1) + j + weight)){
								lstClom.add("第" + (j+1) + "列");
							}
						}
					}
					clumAdapter.notifyDataSetChanged();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
		icon.setOnClickListener(this);
		btn.setOnClickListener(this);
		btn1.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}

	private ResolveInfo resolve ;
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		resolve = list.get(arg2);
		icon.setImageDrawable(resolve.loadIcon(getPackageManager()));
		tv.setText(resolve.activityInfo.packageName);
		tv1.setText(resolve.activityInfo.name);
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btn:
			if(request){
				FyLog.e(TAG, "add the new app!");
				if(resolve != null){
					Intent intent = new Intent();
					AppInfo info = new AppInfo();
					info.position_screen = screen.getScreen_index();
					info.position_x = Integer.parseInt((
							(String)sp_clum.getSelectedItem()).substring(1, 2))-1;
					info.position_y = Integer.parseInt((
							(String)sp_row.getSelectedItem()).substring(1, 2))-1;
					info.weight = getWeight(sp_weight.getSelectedItemPosition());
					info.back_color = ((ColorDrawable)tv2.getBackground()).getColor();
					info.icons = StrIcon;
					info.bitIcon = 
							(bitMap == null? 
									Utilities.createIconBitmap(resolve.loadIcon(getPackageManager()), context)
									: bitMap);
					info.label_size = getFont(sp_font.getSelectedItemPosition());
					info.title = (String) resolve.loadLabel(getPackageManager());
					info.componentName = new ComponentName(tv.getText().toString(),
														tv1.getText().toString());
					info.intent = new Intent();
					intent.putExtra("app", info);
					FyLog.d(TAG, "send the app info to the main config");
					setResult(1, intent);
					finish();
				}
			}else{
				FyLog.e(TAG, "the app is configed!");
				Intent intent = new Intent();
				if(resolve == null){
					app.componentName = new ComponentName(tv.getText().toString(),
							tv1.getText().toString());
					app.weight = getWeight(sp_weight.getSelectedItemPosition());
					app.back_color = ((ColorDrawable)tv2.getBackground()).getColor();
					app.icons = StrIcon;
					app.bitIcon = 
							(bitMap == null? 
									Utilities.createIconBitmap(icon.getDrawable(), context)
									: bitMap);
					app.label_size = getFont(sp_font.getSelectedItemPosition());
					intent.putExtra("app", app);
				}else{
					AppInfo info = new AppInfo();
					info.position_x = Integer.parseInt((
							(String)sp_clum.getSelectedItem()).substring(1, 2))-1;
					info.position_y = Integer.parseInt((
							(String)sp_row.getSelectedItem()).substring(1, 2))-1;
					info.componentName = new ComponentName(tv.getText().toString(),
							tv1.getText().toString());
					info.weight = getWeight(sp_weight.getSelectedItemPosition());
					info.back_color = ((ColorDrawable)tv2.getBackground()).getColor();
					info.icons = StrIcon;
					info.bitIcon = 
							(bitMap == null? 
									Utilities.createIconBitmap(resolve.loadIcon(getPackageManager()), context)
									: bitMap);
					info.label_size = getFont(sp_font.getSelectedItemPosition());
					info.title = (String) resolve.loadLabel(getPackageManager());
					info.intent = new Intent();
					intent.putExtra("app", info);
				}
				FyLog.d(TAG, "send the app info to the main config");
				setResult(2, intent);
				finish();
			}
			resolve = null;
			bitMap = null;
			break;
		case R.id.btn1:
			int alpha = ((ColorDrawable)tv2.getBackground()).getAlpha();
			int color = ((ColorDrawable)tv2.getBackground()).getColor();
			final ColorPickerLayout layout = new ColorPickerLayout(context);
			layout.setColor(color);
			layout.setA(alpha);
			new AlertDialog.Builder(context)
				.setTitle("调色取色")
				.setView(layout.getView())
				.setNegativeButton("确认", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						int color = layout.getColor();
						tv2.setBackgroundColor(color);
					}
				})
				.setPositiveButton("取消", null)
				.create()
				.show();
			break;
		case R.id.iv:
			final AlertDialog dialog = new AlertDialog.Builder(context).create();
			OnImagePickItemClick listener = new OnImagePickItemClick(){
				@Override
				public void onItemClick(String bitmap) {
					// TODO Auto-generated method stub
					StrIcon = bitmap;
					InputStream is;
					try {
						is = context.getResources().getAssets().open(bitmap);
						bitMap = BitmapFactory.decodeStream(is);
						icon.setImageBitmap(bitMap);
						dialog.dismiss();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			final ImagePickLayout imagePick = new ImagePickLayout(context, listener);
			dialog.setTitle("调色取色");
			dialog.setView(imagePick.getView());
			dialog.show();
			break;
		default:
			break;
		}
	}
	
	//获取第三方应用信息
	public List<ResolveInfo> getThirdAppInfo() {
		List<ResolveInfo> appList = getInstallAppInfo();
		List<ResolveInfo> thirdAppList = new ArrayList<ResolveInfo>();
		thirdAppList.clear();
		for (ResolveInfo app : appList) {  
            //非系统程序  
            if ((app.activityInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {  
            	thirdAppList.add((ResolveInfo) app);
            }   
            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了  
            else if ((app.activityInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){  
            	thirdAppList.add((ResolveInfo) app);
            }  
        }  
//					PackageManager mypm = context.getPackageManager();
//					ArrayList<String> thirdAppNameList = new ArrayList<String>();
//					for(ApplicationInfo app : thirdAppList) {
//						Log.v(TAG, "RunningAppInfoParam getThirdAppInfo app label = " + (String)app.loadLabel(mypm));
//						thirdAppNameList.add((String)app.loadLabel(mypm));
//					}
		
		return thirdAppList;
	}
	
	public List<ResolveInfo> getInstallAppInfo() {
        //应用过滤条件  
		PackageManager mypm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
        List<ResolveInfo> appInfoList = mypm.queryIntentActivities(mainIntent, 0); 
		Collections.sort(appInfoList, new ResolveInfo.DisplayNameComparator(mypm));// 排序  
		
		return appInfoList;
    }

	private float getWeight(int position){
		switch (position) {
		case 0:
			return 0.333f;
		case 1:
			return 0.5f;
		case 2:
			return 1.0f;
		default:
			break;
		}
		return 0.33f;
	}
	private int getFont(int position){
		switch (position) {
		case 0:
			return 15;
		case 1:
			return 20;
		case 2:
			return 25;
		case 3:
			return 35;
		default:
			break;
		}
		return 20;
	}

	private void setSpinnerFont(int label_size) {
		// TODO Auto-generated method stub
		tv3.setTextSize(label_size);
		FyLog.d(TAG, "the label size is: " + label_size);
		if(label_size == 15){
			sp_font.setSelection(0);
		}else if(label_size == 20){
			sp_font.setSelection(1);
		}else if(label_size == 25){
			sp_font.setSelection(2);
		}else if(label_size == 35){
			sp_font.setSelection(3);
		}
	}



	private void setSpinnerWeight(float weight) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "the weight is: " + weight);
		if(weight == 0.33f){
			sp_weight.setSelection(0);
		}else if(weight == 0.5f){
			sp_weight.setSelection(1);
		}else if(weight == 1.0f){
			sp_weight.setSelection(2);
		}
	}
	
}
