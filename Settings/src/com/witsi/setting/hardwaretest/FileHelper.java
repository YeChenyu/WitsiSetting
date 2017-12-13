package com.witsi.setting.hardwaretest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.io.FileInputStream;

import java.io.FileNotFoundException;

import java.io.IOException;

import org.apache.http.util.EncodingUtils;

import android.content.Context;

import android.os.Environment;

public class FileHelper {
	private Context context;
	/** SD���Ƿ���� **/
	private boolean hasSD = false;
	/** SD����·�� **/
	private String SDPATH;
	/** ��ǰ�������·�� **/
	private String FILESPATH;

	public FileHelper(Context context) {
		this.context = context;
		hasSD = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
//		SDPATH = Environment.getExternalStorageDirectory().getPath();		
		SDPATH = "/mnt/external_sd";
		FILESPATH = this.context.getFilesDir().getPath();
	}

	/**
	 * �ڱ��ؿ��ϴ����ļ�
	 * 
	 * @throws IOException
	 */
	public File createNativeFile(String fileName) throws IOException {
		File file = new File(FILESPATH + "//" + fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	/**
	 * ɾ�����ؿ��ϵ��ļ�
	 * 
	 * @param fileName
	 */
	public boolean deleteNativeFile(String fileName) {
		File file = new File(FILESPATH + "//" + fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * д���ؿ��ϵ��ļ�
	 * 
	 * @param fileName
	 */
	public boolean witeNativeFile(String fileName, String writedata) {
		File file = new File(FILESPATH + "//" + fileName);
		if (file.exists()) {
			try {
				FileOutputStream fileOS = new FileOutputStream(file);
				fileOS.write(writedata.getBytes());
				fileOS.close();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						fileOS));
				bw.write(writedata, 0, writedata.length());
				bw.flush();
				bw.close();
				return true;
			} catch (Exception e) {

			}

		}
		return false;
	}

	/**
	 * ��ȡ���ؿ����ı��ļ�
	 * 
	 * @param fileName
	 *            * @return
	 */
	public String readNativeFile(String fileName) {
		//StringBuffer sb = new StringBuffer();
		String s = null ; 
		
		File file = new File(FILESPATH + "//" + fileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			
			int length;
	        try {
	            length = fis.available();
	            byte [] buffer = new byte[length];
	            fis.read(buffer);
	            s = EncodingUtils.getString(buffer, "UTF-8");	            			
			fis.close();
	        } catch (FileNotFoundException e) {
			e.printStackTrace();}
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return s;  
	}

	public String getFILESPATH() {
		return FILESPATH;
	}

	public String getSDPATH() {
		return SDPATH;
	}

	public boolean hasSD() {
		return hasSD;
	}
	/**
	 * �ڱ��ؿ��ϴ����ļ�
	 * 
	 * @throws IOException
	 */
	public File createSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + "//" + fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}

	/**
	 * ɾ�����ؿ��ϵ��ļ�
	 * 
	 * @param fileName
	 */
	public boolean deleteSDFile(String fileName) {
		File file = new File(SDPATH + "//" + fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * д���ؿ��ϵ��ļ�
	 * 
	 * @param fileName
	 */
	public boolean witeSDFile(String fileName, String writedata) {
		File file = new File(SDPATH + "//" + fileName);
		if (file.exists()) {
			try {
				FileOutputStream fileOS = new FileOutputStream(file);
				fileOS.write(writedata.getBytes());
				fileOS.close();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						fileOS));
				bw.write(writedata, 0, writedata.length());
				bw.flush();
				bw.close();
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * ��ȡ���ؿ����ı��ļ�
	 * 
	 * @param fileName
	 *            * @return
	 */
	public String readSDFile(String fileName) {
		StringBuffer sb = new StringBuffer();
		String s = null ; 
		File file = new File(SDPATH + "//" + fileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			
			int length;
	        try {
	            length = fis.available();
	            byte [] buffer = new byte[length];
	            fis.read(buffer);
	            s = EncodingUtils.getString(buffer, "UTF-8");	            			
			fis.close();
	        } catch (FileNotFoundException e) {
			e.printStackTrace();}
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return s;  
	}
}
