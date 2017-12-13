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
		((TextView)findViewById(R.id.tv)).setText("���綨λ");
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
	    		Settings.System.getUriFor(Settings.Global.AIRPLANE_MODE_ON), true, observer);//ע�����
		getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.Global.NETWORK_PREFERENCE), true, observer);//ע�����
		getContentResolver().registerContentObserver(
				Settings.System.getUriFor(Settings.Global.DATA_ROAMING), true, observer);//ע�����
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
		//����ģʽ
		boolean state = getAirplaneMode(context);
		if(state)
			cb4.setChecked(true);
		else
			cb4.setChecked(false);
		//�ƶ���������
		tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		boolean net = getMobileDataEnabled(context);
		FyLog.d(TAG, "net available: " + net);
		if(net)
			cb.setChecked(true);
		else 
			cb.setChecked(false);
		//�ƶ�����
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
		//��ѡ��������
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
		FyLog.e(TAG, "THE DEFAULT NETWORK IS: "  + cm.getNetworkPreference());
		int type = cm.getNetworkPreference();
		if(type == ConnectivityManager.TYPE_MOBILE)
			tv.setText("2G/3G����");
		else if(type == ConnectivityManager.TYPE_WIFI)
			tv.setText("����ʹ��WIFI�ȵ�");
		else 
			tv.setText("�Զ�");
		
		//���������(APN)
		NetworkInfo info = cm.getActiveNetworkInfo();   
		//��ȡ�������㣬�й��ƶ�:cmwap��cmnet; �й�����ctwap��ctnet  
		if(info != null){
			String apn = info.getExtraInfo();  
			if(apn != null)
				tv1.setText(apn);
			else
				tv1.setText("δ֪");
		}else
			tv1.setText("δ֪");
		
		//��ȡ��������
//		set = checkNetworkType(context);
		switch (0) {
		case TYPE_CM_CU_WAP:
			FyLog.d(TAG, "the type si�� TYPE_CM_CU_WAP" );
			break;
		case TYPE_CT_WAP:
			FyLog.d(TAG, "the type si�� TYPE_CT_WAP" );
			break;
		case TYPE_NET_WORK_DISABLED:
			FyLog.d(TAG, "the type si�� TYPE_NET_WORK_DISABLED" );
			break;
		case TYPE_OTHER_NET:
			FyLog.d(TAG, "the type si�� TYPE_OTHER_NET" );
			break;
		default:
			break;
		}
		//��ȡ��Ӫ��
//		String name = tm.getNetworkOperatorName();
//		FyLog.d(TAG, "net name is: " + name);
//		if(name.length() > 0)
//			tv2.setText(name);
//		else
//			tv2.setText("δ֪");
		String operator = tm.getNetworkOperator();
		if (operator != null) {
			if (operator.equals("46000") || operator.equals("46002")) {
		    // operatorName="�й��ƶ�";
				tv2.setText("�й��ƶ�");
			} else if (operator.equals("46001")) {
		    // operatorName="�й���ͨ";
				tv2.setText("�й���ͨ");
		   } else if (operator.equals("46003")) {
		    // operatorName="�й�����";
			   tv2.setText("�й�����");
		   }
		}else{
			tv2.setText("δ֪");
		}
		//λ�÷�����Ϣ��Դ
		lm = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
		//����ֻ��ǲ���������GPS����״̬true��gps������false��GPSδ����
		boolean status = lm.isProviderEnabled(
				LocationManager.GPS_PROVIDER);
		//��һ��Gpsprovider��Google��·��ͼ��
	    boolean NETWORK_status = lm.isProviderEnabled(
	    		 LocationManager.NETWORK_PROVIDER);
		FyLog.d(TAG, "gps is: " + status + " : " + NETWORK_status);
		if(status){
			sw.setCheck(true);
		}
	}
	
	/**
	 * ����Settings�µ����ݱ仯
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
	            //����ģʽ
	            if(airplane != getAirplaneMode(context)){
	            	if(!airplane)
		    			cb4.setChecked(true);
		    		else
		    			cb4.setChecked(false);
	            }
	          //�ƶ���������
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
	    		//�ƶ�����
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
	    		//��ѡ��������
	    		//���������(APN)
	    		//��ȡ��������
	    		//��ȡ��Ӫ��
	    		//λ�÷�����Ϣ��Դ
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
		//�ƶ�����
		case R.id.rl:
			if(!cb.isChecked()){
				cb.setChecked(true);
			}else{
				cb.setChecked(false);
			}
			break;
		//����
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
				tv.setText("����ʹ��WIFI�ȵ�");
			}else if(cm.getNetworkPreference() == ConnectivityManager.TYPE_WIFI){
				cm.setNetworkPreference(0);
				tv.setText("2G/3G����");
			}else {
				cm.setNetworkPreference(3);
				tv.setText("�Զ�");
			}
			break;
		case R.id.ll1:
			TextView tv = new TextView(context);
			tv.setText(sbApn.toString());
			tv.setTextSize(25);
			tv.setPadding(15, 0, 0, 0);
			tv.setTextColor(Color.BLACK);
//			new MyAlertDialog.Builder(context)
//			.setTitle("�����APN��Ϣ")
//			.setView(tv)
//			.setPositiveButton("ȷ��", null)
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
		//�����ƶ���������
		case R.id.cb:
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			setMobileDataEnabled(context, arg1);
			break;
		//�����ƶ�������������
		case R.id.cb1:
			if(ProjectConfig.system_app){
				ContentResolver resolver = context.getContentResolver();
		        Settings.Global.putInt(resolver, Settings.Global.DATA_ROAMING, arg1 ? 1 : 0);
			}
			break;
		case R.id.cb2:
			break;
		case R.id.cb4:
			//����ģʽ
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
			//��GPS��λ
			openGps();
		}else{
			//�ر�GPS��λ
			source.setVisibility(View.GONE);
		}
	}
	
	/**
	 * �����ֻ�����ģʽ
	 * @param context
	 * @param enabling true:����Ϊ����ģʽ	false:ȡ������ģʽ
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
	 * �ж��ֻ��Ƿ��Ƿ���ģʽ
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
		String serviceString = Context.LOCATION_SERVICE;// ��ȡ����λ�÷���
		// ����getSystemService()��������ȡLocationManager����
		LocationManager locationManager = (LocationManager) getSystemService(serviceString);
		String provider = LocationManager.GPS_PROVIDER;// ָ��LocationManager�Ķ�λ����
		// ����getLastKnownLocation()������ȡ��ǰ��λ����Ϣ
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
	 * ����GPS�����ر�
	 * ��GPS����ʱ������ر�GPS
	 * ��Gps�ر�ʱ��������GPS
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
	 * �����ƶ����ݿ���״̬
	 * ������Android5.0���¡�
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
	 * ��ȡ�ƶ����ݿ���״̬
	 * ������Android5.0���¡�
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
	 * �ж������Ƿ����
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
  public static final    int TYPE_NET_WORK_DISABLED = 0;// ���粻���� 
  public static final    int TYPE_CM_CU_WAP = 4;// �ƶ���ͨwap10.0.0.172 
  public static final    int TYPE_CT_WAP = 5;// ����wap 10.0.0.200 
  public static final    int TYPE_OTHER_NET = 6;// ����,�ƶ�,��ͨ,wifi ��net���� 
  public static Uri PREFERRED_APN_URI = Uri 
  .parse("content://telephony/carriers/preferapn"); 
	public static int checkNetworkType(Context mContext) { 
		try { 
	      final ConnectivityManager connectivityManager = (ConnectivityManager) mContext 
	          .getSystemService(Context.CONNECTIVITY_SERVICE); 
	      final NetworkInfo mobNetInfoActivity = connectivityManager 
	          .getActiveNetworkInfo(); 
	      if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) { 
	        // ע��һ�� 
	        // NetworkInfo Ϊ�ջ��߲������õ�ʱ���������Ӧ���ǵ�ǰû�п������磬 
	        // ������Щ���Ż������Կ������������� 
	        // ���Ե���net���紦����Ȼ�����������硣 
	        // ��Ȼ����socket�в�׽�쳣�����ж����ж����û���ʾ���� 
	        Log.i("", "=====================>������"); 
	        return TYPE_OTHER_NET; 
	      } else { 
	        // NetworkInfo��Ϊnull��ʼ�ж����������� 
	        int netType = mobNetInfoActivity.getType(); 
	        if (netType == ConnectivityManager.TYPE_WIFI) { 
	          // wifi net���� 
	          Log.i("", "=====================>wifi����"); 
	          return TYPE_OTHER_NET; 
	        } else if (netType == ConnectivityManager.TYPE_MOBILE) { 
	          // ע����� 
	          // �ж��Ƿ����wap: 
	          //��Ҫͨ��getExtraInfo��ȡ������������ж����ͣ� 
	          // ��Ϊͨ��Ŀǰ���Ŷ��ֻ��Ͳ��Է��ֽ�������ƴ�Ϊ#777����null�� 
	          // ���Ż���wap�������Ҫ���ƶ���ͨwap����������һ���û���������, 
	          // ���Կ���ͨ����������жϣ� 
	          final Cursor c = mContext.getContentResolver().query( 
	              PREFERRED_APN_URI, null, null, null, null); 
	          if (c != null) { 
	        	  c.moveToFirst(); 
	        	  final String user = c.getString(c.getColumnIndex("user")); 
	        	  if (!TextUtils.isEmpty(user)) { 
		              Log.i("", 
		                  "=====================>����" 
		                      + c.getString(c 
		                          .getColumnIndex("proxy"))); 
		              if (user.startsWith(CTWAP)) { 
		            	  Log.i("", "=====================>����wap����"); 
		            	  return TYPE_CT_WAP; 
		              } 
	        	  } 
	          } 
	          c.close(); 
	          // ע������ 
	          // �ж����ƶ���ͨwap: 
	          // ��ʵ����һ�ַ���ͨ��getString(c.getColumnIndex("proxy")��ȡ����ip 
	          //���жϽ���㣬10.0.0.172�����ƶ���ͨwap��10.0.0.200���ǵ���wap������ 
	          //ʵ�ʿ����в��������л������ܻ�ȡ������������Ϣ����������M9 ��2.2����... 
	          // ���Բ���getExtraInfo��ȡ��������ֽ����ж� 
	          String netMode = mobNetInfoActivity.getExtraInfo(); 
	          Log.i("", "netMode ================== " + netMode); 
	          if (netMode != null) { 
	        	  // ͨ��apn�����ж��Ƿ�����ͨ���ƶ�wap 
	        	  netMode=netMode.toLowerCase(); 
	        	  if (netMode.equals(CMWAP) || netMode.equals(WAP_3G) 
	                || netMode.equals(UNIWAP)) { 
		              Log.i("", "=====================>�ƶ���ͨwap����"); 
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
