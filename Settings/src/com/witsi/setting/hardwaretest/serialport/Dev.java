package com.witsi.setting.hardwaretest.serialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.util.Log;

public class Dev {

	private static final String TAG = "Dev";
	private SerialPort mSerialPort;	
	private InputStream mInputStream;
	private OutputStream mOutputStream;

	/*		
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	public boolean openPort(String portName,int bandrate) throws SecurityException, IOException
	{
		boolean bret = false;
		try
		{//"/dev/ttyS2"
			 mSerialPort = new SerialPort(new File("/dev/" + portName), bandrate);		
			 mOutputStream = mSerialPort.getOutputStream();
			 mInputStream = mSerialPort.getInputStream();	
			 bret = true;
		} catch (SecurityException e) {
//			DisplayError("R.string.error_security");
		} catch (IOException e) {
//			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
//			DisplayError(R.string.error_configuration);
		}
		return bret;
	}
	
	public void closePort()
	{
		

		if(mSerialPort !=null)
		{
			try {
				mInputStream.skip(1024);
				mOutputStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSerialPort.close();
			mSerialPort = null;			
		}	
	}
	
	public void WritePort(byte [] byOrder)
	{
		try {
			mOutputStream.write(byOrder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	public void WritePort(byte [] byOrder,int offset, int len)
	{
		try {
			mOutputStream.write(byOrder, offset ,len);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	public int ReadPort(byte[] buff)
	{
		int nRet = 0;
		try {
			nRet = mInputStream.read(buff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nRet;
	}
}
