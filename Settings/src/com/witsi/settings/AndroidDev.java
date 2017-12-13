package com.witsi.settings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import com.witsi.debug.FyLog;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqMisc;

public class AndroidDev {

	private static String TAG = AndroidDev.class.getSimpleName();
	/**
	 * 获取系统内核版本，作者，日期
	 * @return
	 */
	public static String getKernel_Ver(){
		String version = null;
		String path = "/proc/version";
		String str;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(path);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str = localBufferedReader.readLine();
			Log.v(TAG, "proc: " + str);
			str = str.replace("(", "");
			str = str.replace(")", "");
			arrayOfString = str.split(" ");
			FyLog.d(TAG, "split: " + arrayOfString);
			if(arrayOfString.length < 19){
				version = arrayOfString[2]
//						+ "\n"
//						+ arrayOfString[3] + arrayOfString[11] 
								;
			}else{
				version = arrayOfString[2] 
//						+ "\n"
//						+ arrayOfString[3] + arrayOfString[11] + "\n"
//						+ arrayOfString[14] + " " + arrayOfString[15] + " "
//						+ arrayOfString[16] + " " + arrayOfString[17] + " "
//						+ arrayOfString[18] + " " + arrayOfString[19]
								; //KernelVersion
			}
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return version;
	}
	
	/**
	 * 获取基带版本
	 * @return
	 */
	public static String getBaseband_Ver(){  
		String Version = "";  
		try {  
			Class cl = Class.forName("android.os.SystemProperties");  
			Object invoker = cl.newInstance();  
			Method m = cl.getMethod("get", new Class[] { String.class,String.class });  
			Object result = m.invoke(invoker, new Object[]{"gsm.version.baseband", "no message"});  
			// System.out.println(">>>>>>><<<<<<<" +(String)result);   
			Version = (String)result;  
		} catch (Exception e) { 
			e.printStackTrace();
		}  
		return Version;  
	}  
	
	/**
	 * 获取序列号
	 * @param context
	 * @return
	 */
	public static String getSerialNumber(Context context){
		int ret;
		byte[] serialnumber = new byte[32];
		ret = new ArqMisc(context).getSystemInfo(0x02, serialnumber);
		if(ret >= 0){
			if("0000000000000000000000000000000000000000000000000000000000000000"
					.equals(ArqConverts.bytesToHexString(serialnumber)))
				return "未知";
			else{
				byte[] tmp = new byte[ret];
				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = serialnumber[i];
				}
				FyLog.d(TAG, "serialnumber="+ ArqConverts.bytesToHexString(tmp));
				return ArqConverts.bytesToHexString(tmp);
			}
		}else{
			return "未知";
		}
	}
	/**
	 * RxRDP_v2.0.3_3029 Jul 1 2016 10:18:53
	 * @param context
	 * @return
	 */
	public static String getRdpVersion(Context context){
		byte[] rdp = new byte[64];
		int ret = new ArqMisc(context).getRdpVer(rdp);
		if(ret > 0){
			byte[] tmp = new byte[ret];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = rdp[i];
			}
			FyLog.d(TAG, "rdpapp="+ new String(tmp));
			return new String(tmp);
		}else
			return "get versino error";
	}
	
	public static String getT1System(Context context){
		byte[] version = new byte[64];
		int ret = new ArqMisc(context).getSystemInfo(0x01, version);
		if(ret > 0){
			byte[] tmp = new byte[ret];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = version[i];
			}
			FyLog.d(TAG, "rxsys="+ ArqConverts.asciiBytesToString(tmp));
			return ArqConverts.asciiBytesToString(tmp);
		}else
			return "get versino error";
	}
	/**
	 * 获取当前ip地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getLocalIpAddress(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int i = wifiInfo.getIpAddress();
			return int2ip(i);
		} catch (Exception ex) {
			return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
		}
	}
	
	/**
	 * 将ip的整数形式转换成ip形式
	 * 
	 * @param ipInt
	 * @return
	 */
	private static String int2ip(int ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}
	
	/**
	 * 检查网络是否可用
	 * 
	 * @param paramContext
	 * @return
	 */
	public static boolean checkEnable(Context paramContext) {
		boolean i = false;
		NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext
				.getSystemService("connectivity")).getActiveNetworkInfo();
		if ((localNetworkInfo != null) && (localNetworkInfo.isAvailable()))
			return true;
		return false;
	}

	/**
	 * 获取通信商户
	 * @param tm
	 * @return
	 */
	public static String getTheMNC(TelephonyManager tm){
		/** 获得电话管理员 */
		//设置通信商户
		String operator = tm.getNetworkOperator();
		FyLog.d(TAG, "the operator is: " + operator);
		if(operator.length() < 1){
			return "未知";
		}else{
			int mnc = Integer.valueOf(operator.substring(3, 5));
			switch (mnc) {
			case 00:
				return "中国移动";
			case 01:
				return "中国联通";
			case 10:
				return "中国电信";
			default:
				return "未知";
			}
		}
	}
	
	/**         
	 * * 获取网络类型         
	 * *          
	 * * NETWORK_TYPE_CDMA 网络类型为CDMA         
	 * * NETWORK_TYPE_EDGE 网络类型为EDGE         
	 * * NETWORK_TYPE_EVDO_0 网络类型为EVDO0         
	 * * NETWORK_TYPE_EVDO_A 网络类型为EVDOA         
	 * * NETWORK_TYPE_GPRS 网络类型为GPRS         
	 * * NETWORK_TYPE_HSDPA 网络类型为HSDPA         
	 * * NETWORK_TYPE_HSPA 网络类型为HSPA         
	 * * NETWORK_TYPE_HSUPA 网络类型为HSUPA         
	 * * NETWORK_TYPE_UMTS 网络类型为UMTS         
	 * *          
	 * * 在中国，联通的3G为UMTS或HSDPA，
	 * 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA，
	 * 电信的3G为EVDO      
	 *    */
	public static String getNetworkType(TelephonyManager tm){
		switch (tm.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "2G";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "2G";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "3G";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "3G";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "2G";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "3G";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "3G";
		default:
			return "未知";
		}
	}
	
	/**SIM卡状态
	 * SIM_STATE_ABSENT SIM卡未找到
     * SIM_STATE_NETWORK_LOCKED SIM卡网络被锁定，需要Network PIN解锁
     * SIM_STATE_PIN_REQUIRED SIM卡PIN被锁定，需要User PIN解锁
     * SIM_STATE_PUK_REQUIRED SIM卡PUK被锁定，需要User PUK解锁
     * SIM_STATE_READY SIM卡可用
     * SIM_STATE_UNKNOWN SIM卡未知
	 * */
	public static String getSIMstatus(TelephonyManager tm){
		switch (tm.getSimState()) {
		case TelephonyManager.SIM_STATE_ABSENT:
			return "SIM卡未找到";
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return "SIM卡网络被锁定，需要Network PIN解锁";
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return "SIM卡PIN被锁定，需要User PIN解锁";
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return "SIM卡PUK被锁定，需要User PUK解锁";
		case TelephonyManager.SIM_STATE_READY:
			return "SIM卡可用";
		case TelephonyManager.SIM_STATE_UNKNOWN:
			return "SIM卡未知";
		default:
			return "未知";
		}
	}
	
	/**
	 * 漫游状态
	 * @return
	 */
	public static String getNetworkRemoting(TelephonyManager tm){
		if(tm.isNetworkRoaming())
			return "漫游";
		else
			return "非漫游";
	}
	
	/**
	 * 设置移动网络状态
	 * @return
	 */
	public static String getNetworkStatus(TelephonyManager tm){
		switch (tm.getDataState()) {
		case TelephonyManager.DATA_CONNECTED:
			return "已连接";
		case TelephonyManager.DATA_CONNECTING:
			return "正在连接";
		case TelephonyManager.DATA_DISCONNECTED:
			return "已断开连接";
		case TelephonyManager.DATA_SUSPENDED:
			return "暂停连接";
		default:
			return "未知";
		}
	}
	
	/**
	 * 设置本机电话号码
	 * @return
	 */
	public static String getLineNumber(TelephonyManager tm){
		if(tm.getLine1Number() == null 
				|| tm.getLine1Number().length() < 1)
			return "未知";
		else
			return tm.getLine1Number();
	}
	
	/**
	 * 设置IMEI
	 * @return
	 */
	public static String getIMEI(TelephonyManager tm){
		if(tm.getDeviceId() == null 
				|| tm.getDeviceId().length() < 1)
			return "未知";
		else
			return tm.getDeviceId();
		
	}
	/**
	 * 设置IMEI SV
	 * @param tm
	 * @return
	 */
	public static String getIMEI_SV(TelephonyManager tm){
		if(tm.getDeviceSoftwareVersion() == null 
				|| tm.getDeviceSoftwareVersion().length() < 1)
			return "未知";
		else
			return tm.getDeviceSoftwareVersion();
		
	}
}
