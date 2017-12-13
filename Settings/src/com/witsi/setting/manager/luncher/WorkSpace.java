package com.witsi.setting.manager.luncher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.setting.manager.luncher.LauncherSettings.Favorites;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;


public class WorkSpace {
    private static final String TAG = "WorkSpace";

    private static final String TAG_FAVORITES = "favorites";
    private static final String TAG_FAVORITE = "favorite";
    private static final String TAG_SCREEN = "screen";

	private Context mContext;
	
	
	private Map<Integer, ScreenInfo> map = new HashMap<Integer, ScreenInfo>();;
	
	public WorkSpace(Context context)
	{
		mContext = context;
		if(isFileExist(strPath + configFileName))
			copyBigDataToSD(strPath + configFileName);
	}
	
	/**
     *  函数名称 : loadFavorites
     *  功能描述 :  从XML中加载数据
     *  参数及返回值说明：
     *  	@param parser
     *  	@return
     */
	private int loadFavorites(XmlPullParser parser) {
		 
         Intent intent = new Intent(Intent.ACTION_MAIN, null);
         intent.addCategory(Intent.CATEGORY_LAUNCHER);
         ContentValues values = new ContentValues();
         
         PackageManager packageManager = mContext.getPackageManager();
         int i = 0;
         
         
         map.clear();
         try {
        	 
        	 
             AttributeSet attrs = Xml.asAttributeSet(parser);
                          
             beginDocument(parser, TAG_FAVORITES);
            
             final int depth = parser.getDepth();
           
             int type;

             while (((type = parser.next()) != XmlPullParser.END_TAG ||
                     parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                 if (type != XmlPullParser.START_TAG) {
                     continue;
                 }
                
                boolean added = false;
                final String name = parser.getName();

                if(TAG_SCREEN.equals(name)) {
                	
                	 FyLog.w(TAG, "into : " + name);
                	 
                	 int id = 0,row = 0,cloum = 0;
                	 ScreenInfo info = new ScreenInfo();
                	 
                	 for(i = 0; i < parser.getAttributeCount(); i++)
                	 {
                		 
                		 FyLog.w(TAG, "parser.getAttributeName(i) : " + parser.getAttributeName(i));
                		 FyLog.w(TAG, "parser.getAttributeValue(i) : " + parser.getAttributeValue(i));
                		 if("id".equals(parser.getAttributeName(i)) )
                		 {
                			 id = Integer.valueOf(parser.getAttributeValue(i));
                		 }else if("row_num".equals(parser.getAttributeName(i)) )
                		 {
                			 row = Integer.valueOf(parser.getAttributeValue(i));
                		 }else if("cloum_num".equals(parser.getAttributeName(i)) )
                		 {
                			 cloum = Integer.valueOf(parser.getAttributeValue(i));
                		 }
                	 }

                	 info.initScreen(id, true, row, cloum);
                	 while ( parser.next() != XmlPullParser.END_TAG) {  
                		 if(parser.getEventType() == XmlPullParser.TEXT)
                		 {
                			 int eventType = parser.next();
                			 if(eventType == XmlPullParser.END_TAG)
                				 break;
                		 }
                		 if(TAG_FAVORITE.equals(parser.getName()))
                		 {
        	                 String x = "0";
        	                 String y = "0";
        	                 String packageName = "";
        	                 String className = "";
        	                 String color = "0";
        	                 String weight = "0";
        	                 String icons = "/assets/ic_launcher.png";
        	                 String lablesize = "0";
                        	 for(i = 0; i < parser.getAttributeCount(); i++)
                        	 {
  	        	                FyLog.w(TAG, "parser.getAttributeName(i) : " + parser.getAttributeName(i));
  	        	                FyLog.w(TAG, "parser.getAttributeValue(i) : " + parser.getAttributeValue(i));

	                    		 if("packageName".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 packageName = parser.getAttributeValue(i);
	                    			 
	                    		 }else if("className".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 className = parser.getAttributeValue(i);
	                    			 
	                    		 }else if("cloum".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 x = parser.getAttributeValue(i);
	                    			 
	                    		 }else if("row".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 y = parser.getAttributeValue(i);
	                    			 
	                    		 }else if("color".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 color = parser.getAttributeValue(i);
	                    			 
	                    		 }else if("weight".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 weight = parser.getAttributeValue(i);
	                    			 
	                    		 }else if("icons".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 icons = parser.getAttributeValue(i);
	                    		 }else if("lablesize".equals(parser.getAttributeName(i)) )
	                    		 {
	                    			 lablesize = parser.getAttributeValue(i);
	                    		 }
                        	 }
		                     long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
		
		                     values.clear();
		                     values.put(LauncherSettings.Favorites.CONTAINER, container);
		                     values.put(LauncherSettings.Favorites.CELLX, x);
		                     values.put(LauncherSettings.Favorites.CELLY, y);
		                     values.put(LauncherSettings.Favorites.COLOR, color);
		                     values.put(LauncherSettings.Favorites.WEIGHT, weight);
		                     values.put(LauncherSettings.Favorites.ICONS, icons);
		                     values.put(LauncherSettings.Favorites.LABLESIZE, lablesize);
		                     
		                     long idVal = addAppShortcut(values, packageName, className, packageManager, intent);
		                     added = idVal >= 0;
		                     if (added) {
		                    	 FyLog.e(TAG, "the tag name is: " + values);   
		                      	 info.addAppInfoMap(addShortcutInfo( packageManager, values));
		                     }else{
		                    	 info.addAppInfoMap(setAppInfoConfig( new AppInfo(),  values));
		                      }
                		 }
          	             parser.next();
                	 }
            		 map.put( id, info);
                 }
//                 a.recycle();
             }
         } catch (XmlPullParserException e) {
             FyLog.w(TAG, "Got exception parsing favorites.", e);
         } catch (IOException e) {
             FyLog.w(TAG, "Got exception parsing favorites.", e);
         } catch (RuntimeException e) {
             FyLog.w(TAG, "Got exception parsing favorites.", e);
         }


         return i;
     }
	
    private static final void beginDocument(XmlPullParser parser, String firstElementName)
            throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
            ;
        }
        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }
        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    private AppInfo addShortcutInfo(PackageManager manager, ContentValues values)
	 {
		   AppInfo info = null;
		   String intentDescription;
		   Intent intent;
		   
		   intentDescription =  values.getAsString(Favorites.INTENT);
		   
		   int itemType = values.getAsInteger( Favorites.ITEM_TYPE);
		   
		   try {
              intent = Intent.parseUri(intentDescription, 0);
              ComponentName cn = intent.getComponent();
              if (cn != null && !isValidPackageComponent(manager, cn)) {
           	   
                 return null;
              }
          } catch (URISyntaxException e) {
              FyLog.i(TAG, "Invalid uri: " + intentDescription);
              return null;
          }
          if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
              info = getShortcutInfo(manager, intent, mContext);
          } 
          if (info != null) {
       	   
              
              setAppInfoConfig( info, values);

              info.intent = intent;
              
          }else
          {
       	   info = setAppInfoConfig(new AppInfo(), values);
          }
		   return info;
	 }
    
    private AppInfo setAppInfoConfig(AppInfo info, ContentValues values)
	 {
		 info.intent = null;
        
		 info.position_x = Integer.valueOf(values.getAsString(LauncherSettings.Favorites.CELLX));
        
        info.position_y = Integer.valueOf(values.getAsString(LauncherSettings.Favorites.CELLY));
        
        info.back_color = Color.parseColor(values.getAsString(LauncherSettings.Favorites.COLOR));;

        info.icons = values.getAsString(LauncherSettings.Favorites.ICONS);
        
        info.weight = (float) Double.parseDouble(values.getAsString(LauncherSettings.Favorites.WEIGHT));
        
        info.label_size = Integer.valueOf(values.getAsString(LauncherSettings.Favorites.LABLESIZE));
        
        if(!info.icons.equals("ic_launcher.png"))
        	info.bitIcon = getIcons(info.icons);
        return info;
	 }
    
    private long addAppShortcut( ContentValues values,String packageName,String className,
            PackageManager packageManager, Intent intent) {
        long id = -1;
        ActivityInfo info;

        
        try {
            ComponentName cn;
            try {
                cn = new ComponentName(packageName, className);
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                String[] packages = packageManager.currentToCanonicalPackageNames(
                    new String[] { packageName });
                cn = new ComponentName(packages[0], className);
                info = packageManager.getActivityInfo(cn, 0);
            }
            id = 1;
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);

            
        } catch (PackageManager.NameNotFoundException e) {
            FyLog.w(TAG, "Unable to add favorite: " + packageName +
                    "/" + className, e);
        }
        return id;
    }
    
    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    private AppInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        ComponentName componentName = intent.getComponent();
        final AppInfo info = new AppInfo();
        
        FyLog.d("LuncherActivity", "getPackInfo  for package " + componentName.getPackageName());
        if (componentName != null && !isValidPackageComponent(manager, componentName)) {
            FyLog.d(TAG, "Invalid package found in getShortcutInfo: " + componentName);
            return null;
        } else {
            try {
                PackageInfo pi = manager.getPackageInfo(componentName.getPackageName(), 0);
            } catch (NameNotFoundException e) {
                FyLog.d(TAG, "getPackInfo failed for package " +
                        componentName.getPackageName());
            }
        }

        Bitmap icon = null;
        ResolveInfo resolveInfo = null;
        ComponentName oldComponent = intent.getComponent();
        Intent newIntent = new Intent(intent.getAction(), null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setPackage(oldComponent.getPackageName());
        List<ResolveInfo> infos = manager.queryIntentActivities(newIntent, 0);
        for (ResolveInfo i : infos) {
            ComponentName cn = new ComponentName(i.activityInfo.packageName,
                    i.activityInfo.name);
            if (cn.equals(oldComponent)) {
                resolveInfo = i;
            }
        }
        if (resolveInfo == null) {
            resolveInfo = manager.resolveActivity(intent, 0);
        }
        if (resolveInfo != null) {
            icon = Utilities.createIconBitmap(resolveInfo.loadIcon(manager),
            		context);
        }
        // the fallback icon
        if (icon == null) {
//            icon = getFallbackIcon();
//            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);
        // from the resource
        if (resolveInfo != null) {
            ComponentName key = getComponentNameFromResolveInfo(resolveInfo);
            info.title = (String) resolveInfo.activityInfo.loadLabel(manager);
        }
        // fall back to the class name of the activity
        if (info.title == null) {
            info.title = componentName.getClassName();
        }
//        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }
    
    
    private boolean isValidPackageComponent(PackageManager pm, ComponentName cn) {
        if (cn == null) {
            return false;
        }
        try {
            // Skip if the application is disabled
            PackageInfo pi = pm.getPackageInfo(cn.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                return false;
            }
            // Check the activity
            return (pm.getActivityInfo(cn, 0) != null);
        } catch (NameNotFoundException e) {
            return false;
        }
    }
    
    public ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    /**
     * 
     *  函数名称 : saveFavorites
     *  功能描述 :  保存桌面配置到xml文件
     *  参数及返回值说明：
     *  	@param serial
     */
    private void saveFavorites(XmlSerializer serialiser, Map<Integer, ScreenInfo> map) {
		 // TODO Auto-generated method stub
    	try {
    		//父标签   命名空间
			serialiser.startTag(null, "favorites");
			serialiser.attribute(null, "xmlns:launcher", "http://schemas.android.com/apk/res-auto/com.android.launcher3");
	    	serialiser.attribute(null, "xmlns:witsi", "com.witsi.laucher");
	    	Set<Integer> s = map.keySet();
	    	for (Iterator iterator = s.iterator(); iterator.hasNext();) {
				Integer si = (Integer) iterator.next();
				//每个屏的配置  id row
				xmlSerial.startTag(null, "screen");
				xmlSerial.attribute(null, "launcher:id", 
						String.valueOf(map.get(si).getScreen_index()));
				xmlSerial.attribute(null, "launcher:row_num", 
						String.valueOf(map.get(si).getRow_num()));
				Set<Integer> r = map.get(si).getMap().keySet();
				for (Iterator iterator2 = r.iterator(); iterator2.hasNext();) {
					Integer ri = (Integer) iterator2.next();
					//每行
					Map<Integer, AppInfo> mapRow = map.get(si).getMap().get(ri);
					Set<Integer> c = mapRow.keySet();
					for (Iterator iterator3 = c.iterator(); iterator3.hasNext();) {
						Integer ci = (Integer) iterator3.next();
						//每列    每个应用的配置 
						AppInfo info = mapRow.get(ci);
						if(info.getIntent() != null){
							serialiser.startTag(null, "favorite");
							if(info.getIntent().getComponent() == null){
								serialiser.attribute(null, "launcher:packageName", 
										info.componentName.getPackageName());
								serialiser.attribute(null, "launcher:className", 
										info.componentName.getClassName());
							}else{
								serialiser.attribute(null, "launcher:packageName", 
										info.getIntent().getComponent().getPackageName());
								serialiser.attribute(null, "launcher:className", 
										info.getIntent().getComponent().getClassName());
							}
							serialiser.attribute(null, "launcher:row", String.valueOf(info.position_y));
							serialiser.attribute(null, "launcher:cloum", String.valueOf(info.position_x));
							serialiser.attribute(null, "launcher:color", getColor(info.back_color));
							FyLog.d(TAG, "the color is: " + getColor(info.back_color));
							serialiser.attribute(null, "launcher:weight", String.valueOf(info.weight));
							serialiser.attribute(null, "witsi:icons", info.icons);
							serialiser.attribute(null, "launcher:lablesize", String.valueOf(info.label_size));
							serialiser.endTag(null, "favorite");
						}
					}
				}
				serialiser.endTag(null, "screen");
			}
	    	xmlSerial.endTag(null, "favorites");
			xmlSerial.endDocument();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    private String getColor(int color){
		 int r = (color & 0xff0000) >> 16; 
	     int g = (color & 0x00ff00) >> 8; 
	     int b = (color & 0x0000ff); 
	     FyLog.d(TAG, "the r is: " + r + "the g is: " + g + "the b is: " + b);
	     String rr = Integer.toString(r, 16);
	     String gg = Integer.toString(g, 16);
	     String bb = Integer.toString(b, 16);
	     FyLog.d(TAG, "the r is: " + rr + "the g is: " + gg + "the b is: " + bb);
	     if(rr.length() == 1)
	    	 rr = "0"+ rr;
	     if(gg.length() == 1)
	    	 gg = "0"+ gg;
	     if(bb.length() == 1)
	    	 bb = "0"+ bb;
	     return "#"+rr+gg+bb;
	}
/** ========================================================================================================================*/	 
    public static final String strPath = "/mnt/sdcard/luncher/files/";
    private static final String configFileName = "default_workspace.xml";
	 private boolean isFileExist(String fileName){
		 FyLog.v(TAG, "isFileExist()");
		 File path = new File(strPath);
		 File file = new File(strPath + fileName);
		 if(!path.exists()){
			 return false;
		 }
		 if(!file.exists()){
			return false;
		 }
		 return true;
	 }
	 private void copyBigDataToSD(String strOutFileName){  
	        InputStream myInput;  
	        OutputStream myOutput;
			try {
				myOutput = new FileOutputStream(strOutFileName);
				myInput = mContext.getAssets().open("default_workspace.xml");  
		        byte[] buffer = new byte[1024];  
		        int length = myInput.read(buffer);
		        while(length > 0)
		        {
		            myOutput.write(buffer, 0, length); 
		            length = myInput.read(buffer);
		        }
		        myOutput.flush();  
		        myInput.close();  
		        myOutput.close();      
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	 /**
	  * 
	  *  函数名称 : xmlFileToXmlResourceParser
	  *  功能描述 :  加载XMLPullParser
	  *  参数及返回值说明：
	  *  	@return
	  */
    private XmlPullParser xmlReaderResourceParser()
	 {
		 FyLog.i("LuncherActivity", "xmlFileToXmlResourceParser");
		 XmlPullParser pullParser = null;
		 FileInputStream fin = null;
		 
		 if(isFileExist(configFileName)){
			 try {
					fin = new FileInputStream(strPath + configFileName);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				} 
			   try {
			    	 
				    InputStream xml = new BufferedInputStream(fin);
					pullParser = Xml.newPullParser();
					pullParser.setInput(xml, "UTF-8");
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			   return (XmlPullParser) pullParser;
		 }
		 return null;
	 }
	 /**
	  * 写配置，将用户界面管理的配置保存到config中。
	  */
	private XmlSerializer xmlSerial = null;
	private XmlSerializer xmlWriterResourceSerializer(){
		XmlPullParserFactory factory;
		FileOutputStream fio = null;
		try {
			factory = XmlPullParserFactory.newInstance();
			xmlSerial = factory.newSerializer();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if(isFileExist(configFileName)){
			FyLog.v("dfadf", "the file is exist");
			try {
				fio = new FileOutputStream(strPath + configFileName);
				OutputStream xml = new BufferedOutputStream(fio);
				xmlSerial = Xml.newSerializer();
				xmlSerial.setOutput(xml, "UTF-8");
				xmlSerial.startDocument("utf-8", true);
				return xmlSerial;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} 
		}
		return null;
	}
/** =============== 加载应用图标 和LOGO=================================*/ 
	 private FileInputStream loadImageResource(){
		 FyLog.i("LuncherActivity", "xmlFileToXmlResourceParser");
		 FileInputStream fin = null;
		 String fileName = "logo.png";
		 if(isFileExist(fileName)){
			 try {
					fin = new FileInputStream(strPath + fileName);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				} 
			    return fin;
		 }
		 return null;
	 }
	 private Bitmap getIcons(String path){
		InputStream is;
		Bitmap bit = null;
		try {
			is = mContext.getResources().getAssets().open(path);
			bit = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bit;
	}
	 
/** =========== 公有方法 ===============================================================================================*/	 
	 public void saveConfiguration(Map<Integer, ScreenInfo> map){
		 XmlSerializer serial = null;
		 serial = xmlWriterResourceSerializer();
		 
		 if(serial == null){
			 serial = (XmlSerializer) mContext.getResources().getXml(R.xml.default_workspace1);
		 }
		 
		 saveFavorites(serial, map);
	 }
	 
	public Map<Integer, ScreenInfo> loadWorkspace() {

		 XmlPullParser parser = null;
		 parser = xmlReaderResourceParser();
		 		 
		 if(parser == null)
		 {
			 parser = mContext.getResources().getXml(R.xml.default_workspace1);
		 }
		 
		 loadFavorites(parser);

		 return map;
	 }
	 
	 public Drawable getLogoDrawable(){
		 
		 Drawable d = null;
		 FileInputStream fin = loadImageResource();
		 if(fin == null){
			 d = mContext.getResources().getDrawable(R.drawable.add1);
		 }else{
			 d = Drawable.createFromPath(strPath + "logo.png");
		 }
		 
		 return d;
	 }
	 public Map<Integer, ScreenInfo> getWorkspaceApp(){
		 return map;
	 }
	 
	 
	 
	 
	 
	 
	 
	 
	 private int  BCDtoDec(byte bcd[],int offset, int length)
		{
		     int i, tmp;
		     int dec = 0;
		     for(i = 0 + offset; i < length + offset; i++)
		     {
		        tmp = (( bcd[i] >> 4 ) & 0x0F ) * 10 + ( bcd[i] & 0x0F );   
		        
		        dec += tmp * power(100, length + offset - 1 - i);          
		     }
		     return dec;
		}
		
	private long power(int x,int n)
		{
			int i =0;
			int tmp = 1;
			for(i = 0;i<n;i++){
				tmp = tmp*x;
			}
			return tmp;
	}
	
	private byte[] hexStr2HexByte(String hexStr) 
    {   
        String str = "0123456789ABCDEF"; 
        char[] hexs = hexStr.toUpperCase().toCharArray();   
        byte[] bytes = new byte[hexStr.length() / 2];   
        int n;   
 
        for (int i = 0; i < bytes.length; i++) 
        {   

            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);   
        }   

        return bytes;   
    } 
}
