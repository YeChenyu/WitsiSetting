package com.witsi.activitys;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetWorkUtil {  
    private static String LOG_TAG = "fdfd";
    public static boolean isNetworkAvailable(Context context) {  
        ConnectivityManager connectivity = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
  
        if (connectivity == null) {  
            Log.w(LOG_TAG, "无法获得ConnectivityManager");  
        } else {  
            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
            if (info != null) {  
                for (int i = 0; i < info.length; i++) {  
                    if (info[i].isAvailable()) {  
                        return true;  
                    }  
                }  
            }  
        }  
        return false;  
    }  
      
    public static boolean checkNetState(Context context){  
                boolean netstate = false;  
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        if(connectivity != null)  
        {  
            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
            if (info != null) {  
                for (int i = 0; i < info.length; i++)  
                {  
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)   
                    {  
                        netstate = true;  
                        break;  
                    }  
                }  
            }  
        }  
        return netstate;  
        }  
      
    public static boolean isNetworkRoaming(Context context) {  
        ConnectivityManager connectivity = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
        if (connectivity == null) {  
            Log.w(LOG_TAG, "couldn't get connectivity manager");  
        } else {  
            NetworkInfo info = connectivity.getActiveNetworkInfo();  
            if (info != null  
                    && info.getType() == ConnectivityManager.TYPE_MOBILE) {  
                TelephonyManager tm = (TelephonyManager) context  
                        .getSystemService(Context.TELEPHONY_SERVICE);  
                if (tm != null && tm.isNetworkRoaming()) {  
                    return true;  
                } else {  
                }  
            } else {  
            }  
        }  
        return false;  
    }  
  
    public static boolean isMobileDataEnable(Context context) throws Exception {  
        ConnectivityManager connectivityManager = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
        boolean isMobileDataEnable = false;  
  
        isMobileDataEnable = connectivityManager.getNetworkInfo(  
                ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();  
  
        return isMobileDataEnable;  
    }  
  
    public static boolean isWifiDataEnable(Context context) throws Exception {  
        ConnectivityManager connectivityManager = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
        boolean isWifiDataEnable = false;  
        isWifiDataEnable = connectivityManager.getNetworkInfo(  
                ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();  
        return isWifiDataEnable;  
    }  
}