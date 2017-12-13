package com.witsi.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import com.witsi.debug.FyLog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class AndroidDev {

	private static String TAG = AndroidDev.class.getSimpleName();
	/**
	 * ��ȡϵͳ�ں˰汾�����ߣ�����
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
				version = arrayOfString[2] + "\n"
						+ arrayOfString[3] + arrayOfString[11] ;
			}else{
				version = arrayOfString[2] + "\n"
						+ arrayOfString[3] + arrayOfString[11] + "\n"
						+ arrayOfString[14] + " " + arrayOfString[15] + " "
						+ arrayOfString[16] + " " + arrayOfString[17] + " "
						+ arrayOfString[18] + " " + arrayOfString[19]; //KernelVersion
			}
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return version;
	}
	
	/**
	 * ��ȡ�����汾
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
	 * ��ȡ��ǰip��ַ
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
			return " ��ȡIP������!!!!�뱣֤��WIFI,���������´�����!\n" + ex.getMessage();
		}
	}
	
	/**
	 * ��ip��������ʽת����ip��ʽ
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
	 * ��������Ƿ����
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

	

}
