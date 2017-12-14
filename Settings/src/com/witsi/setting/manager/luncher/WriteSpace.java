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
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.witsi.debug.FyLog;

import android.content.Context;
import android.util.Xml;

public class WriteSpace {

	private Context context;
	private XmlSerializer xmlSerial = null;
	private StringWriter writer = new StringWriter();
	private XmlPullParserFactory factory ;
	
	public static String strPath = "/mnt/sdcard/luncher/files/";
	private String fileName = "default_workspace.xml";
			
	public WriteSpace(Context context) throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		this.context = context;
		
		try {
			factory = XmlPullParserFactory.newInstance();
			xmlSerial = factory.newSerializer();
			FileOutputStream fio = null;
			if(isFileExist(fileName)){
				FyLog.v("dfadf", "the file is exist");
				 try {
					 fio = new FileOutputStream(strPath + fileName);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
				   OutputStream xml = new BufferedOutputStream(fio);
				xmlSerial = Xml.newSerializer();
				xmlSerial.setOutput(xml, "UTF-8");
				xmlSerial.startDocument("utf-8", true);
				
				xmlSerial.startTag(null, "favorites");
				xmlSerial.attribute(null, "xmlns:launcher", "http://schemas.android.com/apk/res-auto/com.android.launcher3");
				xmlSerial.attribute(null, "xmlns:witsi", "com.witsi.laucher");
				for (int i = 0; i < 2; i++) {
					//ÆÁ
					xmlSerial.startTag(null, "screen");
					xmlSerial.attribute(null, "launcher:id", String.valueOf(i));
					xmlSerial.attribute(null, "launcher:row_num", "3");
					//´ÅÌù
					xmlSerial.startTag(null, "favorite");
					xmlSerial.attribute(null, "launcher:packageName", "com.android.settings");
					xmlSerial.attribute(null, "launcher:className", "com.android.settings.Settings");
					xmlSerial.attribute(null, "launcher:row", "0");
					xmlSerial.attribute(null, "launcher:cloum", "0");
					xmlSerial.attribute(null, "launcher:color", "#ED643B");
					xmlSerial.attribute(null, "launcher:weight", "1");
					xmlSerial.attribute(null, "witsi:icons", "childish_gears.png");
					xmlSerial.attribute(null, "launcher:lablesize", "35");
					xmlSerial.endTag(null, "favorite");
					
					xmlSerial.startTag(null, "favorite");
					xmlSerial.attribute(null, "launcher:packageName", "com.example.android_equipment_text1");
					xmlSerial.attribute(null, "launcher:className", "com.example.android_equipment_test.EntryActivity");
					xmlSerial.attribute(null, "launcher:row", "1");
					xmlSerial.attribute(null, "launcher:cloum", "0");
					xmlSerial.attribute(null, "launcher:color", "#ED643B");
					xmlSerial.attribute(null, "launcher:weight", "0.5");
					xmlSerial.attribute(null, "witsi:icons", "childish_gears.png");
					xmlSerial.attribute(null, "launcher:lablesize", "35");
					xmlSerial.endTag(null, "favorite");
					
					xmlSerial.endTag(null, "screen");
				}
				xmlSerial.endTag(null, "favorites");
				xmlSerial.endDocument();
			}
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	private boolean isFileExist(String fileName){
		 FyLog.v("dfadf", "isFileExist()");
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
}
