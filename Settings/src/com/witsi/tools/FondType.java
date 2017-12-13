package com.witsi.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.witsi.debug.FyLog;

import android.content.res.Configuration;

public class FondType {

	private static String TAG = FondType.class.getSimpleName();
	
	public static String getFontSzie(){
		try {  
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");  
            try {  
                Object am = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);  
                Object config = am.getClass().getMethod("getConfiguration").invoke(am);  
                Configuration configs = (Configuration)config;  
                FyLog.d(TAG, "FONT SCALE IS: " + configs.fontScale);
                if(configs.fontScale < 1.0f){
                	return "С";
                }else if(configs.fontScale == 1.0f){
                	return "��ͨ";
                }else if(configs.fontScale > 1.0f 
                		&& configs.fontScale < 1.3f){
                	return "��";
                }else if(configs.fontScale >= 1.3f){
                	return "����";
                }else{
                	return "δ֪";
                }
            } catch (IllegalArgumentException e) {  
                // TODO Auto-generated catch block   
                e.printStackTrace();  
            } catch (SecurityException e) {  
                // TODO Auto-generated catch block   
                e.printStackTrace();  
            } catch (IllegalAccessException e) {  
                // TODO Auto-generated catch block   
                e.printStackTrace();  
            } catch (InvocationTargetException e) {  
                // TODO Auto-generated catch block   
                e.printStackTrace();  
            } catch (NoSuchMethodException e) {  
                // TODO Auto-generated catch block   
                e.printStackTrace();  
            }  
              
        } catch (ClassNotFoundException e) {  
            // TODO Auto-generated catch block   
            e.printStackTrace();  
        }  
		return null;
	}
	public static Configuration setFontSize(){
		
		Configuration mconfig = new Configuration();
    	Method method;
		try {
			Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
		    try {
				Object am = activityManagerNative.getMethod("getDefault").invoke(activityManagerNative);
				method = am.getClass().getMethod("updateConfiguration", Configuration.class);
				method.invoke(am, mconfig);//���������С�ķ�������updateConfiguration(Configuration confit);
				return mconfig;
		    } catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
}
