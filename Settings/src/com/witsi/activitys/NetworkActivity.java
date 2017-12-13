package com.witsi.activitys;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.views.SlipButton;
import com.witsi.views.SlipButton.OnChangedListener;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class NetworkActivity extends Activity implements OnCheckedChangeListener
						, OnChangedListener, OnClickListener{

	private String TAG = NetworkActivity.class.getSimpleName();
	private Context context = NetworkActivity.this;
	
	private LinearLayout ll_back;
	private RelativeLayout gprs, source;
	private CheckBox cb, cb1, cb2, cb4;
	private SlipButton sw;
	private ScrollView sv;
	private TextView tv, tv1, tv2;
	private RelativeLayout rl;
	
	private TelephonyManager tm ;
	private ConnectivityManager cm;
	private LocationManager lm;
	
	private StringBuffer sbApn = new StringBuffer();
	private SettingsValuesChangeObserver observer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network_activity);
		
		initViews();
		
//		initDatas();
		
	}
	private void initViews() {
		ll_back = (LinearLayout) findViewById(R.id.ll_back);
		ll_back.setOnClickListener(this);
		((TextView)findViewById(R.id.tv)).setText("网络定位");
		findViewById(R.id.action_back).findViewById(R.id.sw).setVisibility(View.GONE);
		
		cb = (CheckBox) findViewById(R.id.cb);
		cb1 = (CheckBox) findViewById(R.id.cb1);
		cb2 = (CheckBox) findViewById(R.id.cb2);
		cb4 = (CheckBox) findViewById(R.id.cb4);
		sw = (SlipButton) findViewById(R.id.sw);
		gprs = (RelativeLayout) findViewById(R.id.rl2);
		gprs.setOnClickListener(this);
		source = (RelativeLayout) findViewById(R.id.rl3);
		source.setOnClickListener(this);
		findViewById(R.id.ll).setOnClickListener(this);
		
		sv = (ScrollView) findViewById(R.id.sv);
		tv = (TextView) findViewById(R.id.tv3);
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		sw.setOnChangedListener(this);
		
		PackageManager manager = getPackageManager();
		if(!manager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)){
			gprs.setEnabled(false);
			cb2.setEnabled(false);
			gprs.setBackgroundResource(R.color.gray);
		}
		
		findViewById(R.id.rl).setOnClickListener(this);
		findViewById(R.id.rl1).setOnClickListener(this);
		findViewById(R.id.ll).setOnClickListener(this);
		findViewById(R.id.ll1).setOnClickListener(this);
		findViewById(R.id.ll2).setOnClickListener(this);
		findViewById(R.id.rl4).setOnClickListener(this);
		
		
		cb.setOnCheckedChangeListener(this);
		cb1.setOnCheckedChangeListener(this);
		cb2.setOnCheckedChangeListener(this);
		cb4.setOnCheckedChangeListener(this);
		
		
		observer = new SettingsValuesChangeObserver();
		registerContentResolver();
	}

	
	public void registerContentResolver(){
		getContentResolver().registerContentObserver(
	    		Settings.System.getUriFor(Settings.Global.AIRPLANE_MODE_ON), true, observer);//注册监听
		getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.Global.NETWORK_PREFERENCE), true, observer);//注册监听
		getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.Global.DATA_ROAMING), true, observer);//注册监听
		IntentFilter filter = new IntentFilter();
//        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
//        registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initDatas();
	}
	
	@SuppressLint("NewApi")
	private void initDatas() {
		//飞行模式
		boolean state = getAirplaneMode(context);
		if(state)
			cb4.setChecked(true);
		else
			cb4.setChecked(false);
		//移动数据网络
		tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		boolean net = getMobileDataEnabled(context);
		FyLog.d(TAG, "net available: " + net);
		if(net)
			cb.setChecked(true);
		else 
			cb.setChecked(false);
		//移动漫游
        try {
			int data = Settings.Global.getInt(context.getContentResolver(), Settings.Global.DATA_ROAMING);
			FyLog.d(TAG, "is roaming: " + data);
			if(data == 1)
				cb1.setChecked(true);
			else 
				cb1.setChecked(false);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//首选网络类型
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
		FyLog.e(TAG, "THE DEFAULT NETWORK IS: "  + cm.getNetworkPreference());
		int type = cm.getNetworkPreference();
		if(type == ConnectivityManager.TYPE_MOBILE)
			tv.setText("2G/3G优先");
		else if(type == ConnectivityManager.TYPE_WIFI)
			tv.setText("优先使用WIFI热点");
		else 
			tv.setText("自动");
		
		//接入点名称(APN)
		NetworkInfo info = cm.getActiveNetworkInfo();   
		//获取网络接入点，中国移动:cmwap和cmnet; 中国电信ctwap，ctnet  
		if(info != null){
			String apn = info.getExtraInfo();  
			if(apn != null)
				tv1.setText(apn);
			else
				tv1.setText("未知");
		}else
			tv1.setText("未知");
		
		//获取网络类型
//		set = checkNetworkType(context);
		switch (0) {
		case TYPE_CM_CU_WAP:
			FyLog.d(TAG, "the type si； TYPE_CM_CU_WAP" );
			break;
		case TYPE_CT_WAP:
			FyLog.d(TAG, "the type si； TYPE_CT_WAP" );
			break;
		case TYPE_NET_WORK_DISABLED:
			FyLog.d(TAG, "the type si； TYPE_NET_WORK_DISABLED" );
			break;
		case TYPE_OTHER_NET:
			FyLog.d(TAG, "the type si； TYPE_OTHER_NET" );
			break;
		default:
			break;
		}
		//获取运营商
//		String name = tm.getNetworkOperatorName();
//		FyLog.d(TAG, "net name is: " + name);
//		if(name.length() > 0)
//			tv2.setText(name);
//		else
//			tv2.setText("未知");
		String operator = tm.getNetworkOperator();
		if (operator != null) {
			if (operator.equals("46000") || operator.equals("46002")) {
		    // operatorName="中国移动";
				tv2.setText("中国移动");
			} else if (operator.equals("46001")) {
		    // operatorName="中国联通";
				tv2.setText("中国联通");
		   } else if (operator.equals("46003")) {
		    // operatorName="中国电信";
			   tv2.setText("中国电信");
		   }
		}else{
			tv2.setText("未知");
		}
		//位置访问信息来源
		lm = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
		//获得手机是不是设置了GPS开启状态true：gps开启，false：GPS未开启
		boolean status = lm.isProviderEnabled(
				LocationManager.GPS_PROVIDER);
		//另一种Gpsprovider（Google网路地图）
	    boolean NETWORK_status = lm.isProviderEnabled(
	    		 LocationManager.NETWORK_PROVIDER);
		FyLog.d(TAG, "gps is: " + status + " : " + NETWORK_status);
		if(status){
			sw.setCheck(true);
		}
	}
	
	/**
	 * 监听Settings下的内容变化
	 * @author chenyuye
	 *
	 */
	class SettingsValuesChangeObserver extends ContentObserver{
		
		boolean airplane = false;
		public SettingsValuesChangeObserver() {
			// TODO Auto-generated constructor stub
			super(new Handler());
		}
		
		 @SuppressLint("NewApi")
		@Override
	        public void onChange(boolean selfChange) {
	            super.onChange(selfChange);
	            FyLog.d(TAG, "the settings values change");
	            //飞行模式
	            if(airplane != getAirplaneMode(context)){
	            	if(!airplane)
		    			cb4.setChecked(true);
		    		else
		    			cb4.setChecked(false);
	            }
	          //移动数据网络
	    		int net;
				try {
					net = Settings.Global.getInt(context.getContentResolver()
							, Settings.System.NETWORK_PREFERENCE);
					if(net == 1)
		    			cb.setChecked(true);
		    		else 
		    			cb.setChecked(false);
				} catch (SettingNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    		//移动漫游
	            try {
	    			int data = Settings.Global.getInt(context.getContentResolver(), Settings.Global.DATA_ROAMING);
	    			FyLog.d(TAG, "is roaming: " + data);
	    			if(data == 1)
	    				cb1.setChecked(true);
	    			else 
	    				cb1.setChecked(false);
	    		} catch (SettingNotFoundException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    		//首选网络类型
	    		//接入点名称(APN)
	    		//获取网络类型
	    		//获取运营商
	    		//位置访问信息来源
	        } 
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getContentResolver().unregisterContentObserver(observer);
	}
	private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;
    public static final String RESTORE_CARRIERS_URI =
            "content://telephony/carriers/restore";
        public static final String PREFERRED_APN_URI1 =
            "content://telephony/carriers/preferapn";
    private static final Uri DEFAULTAPN_URI = Uri.parse(RESTORE_CARRIERS_URI);
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI1);
    
	private String getSelectedApnKey() {
        String key = null;

        Cursor cursor = getContentResolver().query(PREFERAPN_URI, new String[] {"_id"},
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(ID_INDEX);
        }
        cursor.close();
        return key;
    }
	
	@SuppressLint("NewApi")
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.ll_back:
			finish();
			break;
		//移动网络
		case R.id.rl:
			if(!cb.isChecked()){
				cb.setChecked(true);
			}else{
				cb.setChecked(false);
			}
			break;
		//漫游
		case R.id.rl1:
			if(!cb1.isChecked()){
				cb1.setChecked(true);
			}else{
				cb1.setChecked(false);
			}
			break;
		case R.id.ll:
			// change mode  
			if(cm.getNetworkPreference() == ConnectivityManager.TYPE_MOBILE){
				cm.setNetworkPreference(1);
				tv.setText("优先使用WIFI热点");
			}else if(cm.getNetworkPreference() == ConnectivityManager.TYPE_WIFI){
				cm.setNetworkPreference(0);
				tv.setText("2G/3G优先");
			}else {
				cm.setNetworkPreference(3);
				tv.setText("自动");
			}
			break;
		case R.id.ll1:
			TextView tv = new TextView(context);
			tv.setText(sbApn.toString());
			tv.setTextSize(25);
			tv.setPadding(15, 0, 0, 0);
			tv.setTextColor(Color.BLACK);
//			new MyAlertDialog.Builder(context)
//			.setTitle("接入点APN信息")
//			.setView(tv)
//			.setPositiveButton("确认", null)
//			.create()
//			.show();
			break;
		case R.id.ll2:
			break;
		case R.id.rl2:
			if(!sw.isCheck()){
				sw.setCheck(true);
			}else{
				sw.setCheck(false);
			}
			new Handler().postDelayed(new Runnable() {
			    @Override
			    public void run() {
			        sv.fullScroll(ScrollView.FOCUS_DOWN);
			    }
			}, 100);
			break;
		case R.id.rl3:
			if(!cb2.isChecked()){
				cb2.setChecked(true);
			}else{
				cb2.setChecked(false);
			}
			break;
		case R.id.rl4:
			if(!cb4.isChecked()){
				cb4.setChecked(true);
			}else{
				cb4.setChecked(false);
			}
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		//开启移动数据网络
		case R.id.cb:
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			setMobileDataEnabled(context, arg1);
			break;
		//开启移动数据漫游网络
		case R.id.cb1:
			if(ProjectConfig.system_app){
				ContentResolver resolver = context.getContentResolver();
		        Settings.Global.putInt(resolver, Settings.Global.DATA_ROAMING, arg1 ? 1 : 0);
			}
			break;
		case R.id.cb2:
			break;
		case R.id.cb4:
			//飞行模式
			FyLog.e(TAG, "change the device mode");
			setAirplaneMode(arg1);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onChanged(boolean checkState) {
		// TODO Auto-generated method stub
		FyLog.d(TAG, "the slipbutton is: " + checkState);
		if(checkState){
			source.setVisibility(View.VISIBLE);
			//打开GPS定位
			openGps();
		}else{
			//关闭GPS定位
			source.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 设置手机飞行模式
	 * @param context
	 * @param enabling true:设置为飞行模式	false:取消飞行模式
	 */
	@SuppressLint("NewApi")
	protected void setAirplaneMode(boolean setAirPlane) {
		Settings.Global.putInt(getContentResolver(),
		Settings.Global.AIRPLANE_MODE_ON, setAirPlane ? 1 : 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("TestCode", "ellic");
		sendBroadcast(intent);
		}
	/**
	 * 判断手机是否是飞行模式
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	public static boolean getAirplaneMode(Context context){
		int isAirplaneMode = Settings.Global.getInt(context.getContentResolver(),
                           Settings.System.AIRPLANE_MODE_ON, 0) ;
		return (isAirplaneMode == 1) ? true : false;
	}
	
	private void openGps(){
		String serviceString = Context.LOCATION_SERVICE;// 获取的是位置服务
		// 调用getSystemService()方法来获取LocationManager对象
		LocationManager locationManager = (LocationManager) getSystemService(serviceString);
		String provider = LocationManager.GPS_PROVIDER;// 指定LocationManager的定位方法
		// 调用getLastKnownLocation()方法获取当前的位置信息
		Location location = locationManager.getLastKnownLocation(provider);
		if(location != null)
			FyLog.e(TAG, "the location is: " + location.getLatitude() + " : " + location.getLongitude());
		boolean is = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		FyLog.e(TAG, "the enable si: " + is);
	}
	
	private final LocationListener locationListener = new LocationListener() {
		 
        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
             
        }
 
        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub
             
        }
 
        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub
             
        }
 
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub
 
        }
 
    };
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	/**
	 * 控制GPS开启关闭
	 * 当GPS开启时调用则关闭GPS
	 * 当Gps关闭时调用则开启GPS
	 */
	private void toggleGPS() { 
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}
	private void toggleGoogle() { 
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.APP_MAPS");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 设置移动数据开关状态
	 * 适用于Android5.0以下。
	 * @param context
	 * @param enabled
	 */
	private void setMobileDataEnabled(Context context, boolean enabled) {
		final String TAG = "setMobileDataEnabled";
		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class conmanClass;
		try {
			conmanClass = Class.forName(conman.getClass().getName());
			final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);
 
			setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "ClassNotFoundException");
		} catch (NoSuchFieldException e) {
			Log.e(TAG, "NoSuchFieldException");
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException");
		} catch (IllegalAccessException e) {
			Log.e(TAG, "IllegalAccessException");
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "NoSuchMethodException");
		} catch (InvocationTargetException e) {
			Log.e(TAG, "InvocationTargetException");
		}
	}
	/**
	 * 获取移动数据开关状态
	 * 适用于Android5.0以下。
	 * @param context
	 * @param enabled
	 */
	private boolean getMobileDataEnabled(Context context) {
		
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Class<Object>[] getArgArray = null;
		Object[] getArgInvoke = null;
		try {
		    Method mGetMethod = cm.getClass().getMethod("getMobileDataEnabled", getArgArray);
		    boolean isOpen = (Boolean) mGetMethod.invoke(cm, getArgInvoke);
		    return isOpen;
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return false;
	}
	/**
	 * 判断网络是否可用
	 * @param context
	 * @param enabled
	 */
	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}
	 
	public static final    String CTWAP = "ctwap"; 
  public static final    String CMWAP = "cmwap"; 
  public static final    String WAP_3G = "3gwap"; 
  public static final    String UNIWAP = "uniwap"; 
  public static final    int TYPE_NET_WORK_DISABLED = 0;// 网络不可用 
  public static final    int TYPE_CM_CU_WAP = 4;// 移动联通wap10.0.0.172 
  public static final    int TYPE_CT_WAP = 5;// 电信wap 10.0.0.200 
  public static final    int TYPE_OTHER_NET = 6;// 电信,移动,联通,wifi 等net网络 
  public static Uri PREFERRED_APN_URI = Uri 
  .parse("content://telephony/carriers/preferapn"); 
	public static int checkNetworkType(Context mContext) { 
		try { 
	      final ConnectivityManager connectivityManager = (ConnectivityManager) mContext 
	          .getSystemService(Context.CONNECTIVITY_SERVICE); 
	      final NetworkInfo mobNetInfoActivity = connectivityManager 
	          .getActiveNetworkInfo(); 
	      if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) { 
	        // 注意一： 
	        // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络， 
	        // 但是有些电信机器，仍可以正常联网， 
	        // 所以当成net网络处理依然尝试连接网络。 
	        // （然后在socket中捕捉异常，进行二次判断与用户提示）。 
	        Log.i("", "=====================>无网络"); 
	        return TYPE_OTHER_NET; 
	      } else { 
	        // NetworkInfo不为null开始判断是网络类型 
	        int netType = mobNetInfoActivity.getType(); 
	        if (netType == ConnectivityManager.TYPE_WIFI) { 
	          // wifi net处理 
	          Log.i("", "=====================>wifi网络"); 
	          return TYPE_OTHER_NET; 
	        } else if (netType == ConnectivityManager.TYPE_MOBILE) { 
	          // 注意二： 
	          // 判断是否电信wap: 
	          //不要通过getExtraInfo获取接入点名称来判断类型， 
	          // 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null， 
	          // 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码, 
	          // 所以可以通过这个进行判断！ 
	          final Cursor c = mContext.getContentResolver().query( 
	              PREFERRED_APN_URI, null, null, null, null); 
	          if (c != null) { 
	        	  c.moveToFirst(); 
	        	  final String user = c.getString(c.getColumnIndex("user")); 
	        	  if (!TextUtils.isEmpty(user)) { 
		              Log.i("", 
		                  "=====================>代理：" 
		                      + c.getString(c 
		                          .getColumnIndex("proxy"))); 
		              if (user.startsWith(CTWAP)) { 
		            	  Log.i("", "=====================>电信wap网络"); 
		            	  return TYPE_CT_WAP; 
		              } 
	        	  } 
	          } 
	          c.close(); 
	          // 注意三： 
	          // 判断是移动联通wap: 
	          // 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip 
	          //来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在 
	          //实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等... 
	          // 所以采用getExtraInfo获取接入点名字进行判断 
	          String netMode = mobNetInfoActivity.getExtraInfo(); 
	          Log.i("", "netMode ================== " + netMode); 
	          if (netMode != null) { 
	        	  // 通过apn名称判断是否是联通和移动wap 
	        	  netMode=netMode.toLowerCase(); 
	        	  if (netMode.equals(CMWAP) || netMode.equals(WAP_3G) 
	                || netMode.equals(UNIWAP)) { 
		              Log.i("", "=====================>移动联通wap网络"); 
		              return TYPE_CM_CU_WAP; 
	        	  } 
	          } 

	        } 
	      } 
		} catch (Exception ex) { 
			ex.printStackTrace(); 
			return TYPE_OTHER_NET; 
	    } 
	    return TYPE_OTHER_NET; 
	  } 
}
