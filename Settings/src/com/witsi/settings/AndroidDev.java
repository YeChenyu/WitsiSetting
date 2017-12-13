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
	 * ��ȡ���к�
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
				return "δ֪";
			else{
				byte[] tmp = new byte[ret];
				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = serialnumber[i];
				}
				FyLog.d(TAG, "serialnumber="+ ArqConverts.bytesToHexString(tmp));
				return ArqConverts.bytesToHexString(tmp);
			}
		}else{
			return "δ֪";
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

	/**
	 * ��ȡͨ���̻�
	 * @param tm
	 * @return
	 */
	public static String getTheMNC(TelephonyManager tm){
		/** ��õ绰����Ա */
		//����ͨ���̻�
		String operator = tm.getNetworkOperator();
		FyLog.d(TAG, "the operator is: " + operator);
		if(operator.length() < 1){
			return "δ֪";
		}else{
			int mnc = Integer.valueOf(operator.substring(3, 5));
			switch (mnc) {
			case 00:
				return "�й��ƶ�";
			case 01:
				return "�й���ͨ";
			case 10:
				return "�й�����";
			default:
				return "δ֪";
			}
		}
	}
	
	/**         
	 * * ��ȡ��������         
	 * *          
	 * * NETWORK_TYPE_CDMA ��������ΪCDMA         
	 * * NETWORK_TYPE_EDGE ��������ΪEDGE         
	 * * NETWORK_TYPE_EVDO_0 ��������ΪEVDO0         
	 * * NETWORK_TYPE_EVDO_A ��������ΪEVDOA         
	 * * NETWORK_TYPE_GPRS ��������ΪGPRS         
	 * * NETWORK_TYPE_HSDPA ��������ΪHSDPA         
	 * * NETWORK_TYPE_HSPA ��������ΪHSPA         
	 * * NETWORK_TYPE_HSUPA ��������ΪHSUPA         
	 * * NETWORK_TYPE_UMTS ��������ΪUMTS         
	 * *          
	 * * ���й�����ͨ��3GΪUMTS��HSDPA��
	 * �ƶ�����ͨ��2GΪGPRS��EGDE�����ŵ�2GΪCDMA��
	 * ���ŵ�3GΪEVDO      
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
			return "δ֪";
		}
	}
	
	/**SIM��״̬
	 * SIM_STATE_ABSENT SIM��δ�ҵ�
     * SIM_STATE_NETWORK_LOCKED SIM�����类��������ҪNetwork PIN����
     * SIM_STATE_PIN_REQUIRED SIM��PIN����������ҪUser PIN����
     * SIM_STATE_PUK_REQUIRED SIM��PUK����������ҪUser PUK����
     * SIM_STATE_READY SIM������
     * SIM_STATE_UNKNOWN SIM��δ֪
	 * */
	public static String getSIMstatus(TelephonyManager tm){
		switch (tm.getSimState()) {
		case TelephonyManager.SIM_STATE_ABSENT:
			return "SIM��δ�ҵ�";
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return "SIM�����类��������ҪNetwork PIN����";
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return "SIM��PIN����������ҪUser PIN����";
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return "SIM��PUK����������ҪUser PUK����";
		case TelephonyManager.SIM_STATE_READY:
			return "SIM������";
		case TelephonyManager.SIM_STATE_UNKNOWN:
			return "SIM��δ֪";
		default:
			return "δ֪";
		}
	}
	
	/**
	 * ����״̬
	 * @return
	 */
	public static String getNetworkRemoting(TelephonyManager tm){
		if(tm.isNetworkRoaming())
			return "����";
		else
			return "������";
	}
	
	/**
	 * �����ƶ�����״̬
	 * @return
	 */
	public static String getNetworkStatus(TelephonyManager tm){
		switch (tm.getDataState()) {
		case TelephonyManager.DATA_CONNECTED:
			return "������";
		case TelephonyManager.DATA_CONNECTING:
			return "��������";
		case TelephonyManager.DATA_DISCONNECTED:
			return "�ѶϿ�����";
		case TelephonyManager.DATA_SUSPENDED:
			return "��ͣ����";
		default:
			return "δ֪";
		}
	}
	
	/**
	 * ���ñ����绰����
	 * @return
	 */
	public static String getLineNumber(TelephonyManager tm){
		if(tm.getLine1Number() == null 
				|| tm.getLine1Number().length() < 1)
			return "δ֪";
		else
			return tm.getLine1Number();
	}
	
	/**
	 * ����IMEI
	 * @return
	 */
	public static String getIMEI(TelephonyManager tm){
		if(tm.getDeviceId() == null 
				|| tm.getDeviceId().length() < 1)
			return "δ֪";
		else
			return tm.getDeviceId();
		
	}
	/**
	 * ����IMEI SV
	 * @param tm
	 * @return
	 */
	public static String getIMEI_SV(TelephonyManager tm){
		if(tm.getDeviceSoftwareVersion() == null 
				|| tm.getDeviceSoftwareVersion().length() < 1)
			return "δ֪";
		else
			return tm.getDeviceSoftwareVersion();
		
	}
}
