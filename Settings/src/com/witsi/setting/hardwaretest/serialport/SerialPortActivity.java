/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.witsi.setting.hardwaretest.serialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

public abstract class SerialPortActivity extends Activity {

	protected SerialPort mSerialPort;	
	protected ReadThread mReadThread;
	private boolean m_bAsync = false;

	public class ReadThread extends Thread {
		protected InputStream mInputStream;
		protected OutputStream mOutputStream;
		private boolean mbStopRun = false;
		
		public ReadThread(SerialPort port) {			
		      mOutputStream = port.getOutputStream();
		      mInputStream = port.getInputStream();	
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
		// for sync read
		public int ReadPort(byte [] byBuf)
		{
			int nRet = 0;
			try {
				nRet = mInputStream.read(byBuf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return nRet;
		}
		
		public void ClearPort()
		{			
			try {
				mInputStream.skip(500);
				mOutputStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Í£Ö¹ÔËÐÐ
		public void StopRunSign()
		{
			mbStopRun = true;
		}
		public boolean GetRunState()
		{
			return mbStopRun;
		}
		
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {	
				if(mbStopRun)
				{
					mbStopRun = false;
					break;
				}
				int size = 0;
				
//			    try {
//		              Thread.sleep(50);
//		        } catch (InterruptedException e) {
//		              e.printStackTrace();
//		        }

				try {
					byte[] buffer = new byte[1024];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					//Log.v("serialportactivity","rev size="+size); 
					if (size > 0) {
						onDataReceived(buffer, size);						
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	protected boolean openPort(String strCom,int bandrate,boolean bAsync) throws SecurityException, IOException
	{
		boolean bret = false;
		try
		{
			mSerialPort = new SerialPort(new File(strCom), bandrate);		
	
			/* Create a receiving thread */
			mReadThread = new ReadThread(mSerialPort);
			//mReadThread.setDaemon(true);
			m_bAsync = bAsync;
			if(bAsync)
			{
				mReadThread.start();
			}
			bret = true;
		
		} catch (SecurityException e) {
//			DisplayError(R.string.error_security);
		} catch (IOException e) {
//			DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
//			DisplayError(R.string.error_configuration);
		}
		return bret;
	}
	
	protected void closePort()
	{
		if (mReadThread != null
				&& m_bAsync)
		{
			mReadThread.interrupt();			 
			mReadThread.StopRunSign();	
			mReadThread =null;
			m_bAsync = false;
		}
		if(mSerialPort !=null)
		{
			mSerialPort.close();
			mSerialPort = null;			
		}	
	}
	
	protected void WritePort(byte [] byOrder)
	{
		mReadThread.WritePort(byOrder);	
	}
	
	protected abstract void onDataReceived(final byte[] buffer, final int size);

	@Override
	protected void onDestroy() {		
		closePort();
		super.onDestroy();
	}
}
