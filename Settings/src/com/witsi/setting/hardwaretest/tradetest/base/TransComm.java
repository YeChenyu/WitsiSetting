
package com.witsi.setting.hardwaretest.tradetest.base;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.witsi.tools.TOOL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class TransComm{
	
	private static final String TAG = "TransComm";
	private static final boolean D = true;
	private Context mContext = null;
	private byte[] dataBuffer = new byte[1024];
	private int dataLen;
	private String mDstName;
	private int mDstPort;
	
	private TransCommListener mTransCommListener = null;
	public TransComm( Context context,TransCommListener Listener, String dstName, int dstPort)
	{
		mContext = context;
		mTransCommListener = Listener;	
		mDstName = dstName;
		mDstPort = dstPort;
	}
	
	private void ShowStatus(String paramString)
	{
	   if (this.mTransCommListener != null)
	     this.mTransCommListener.transCommUI(paramString);
	}
	
	private void onTransCommComplete(COMM_STATUS result, byte[] recv)
	{
	   if (this.mTransCommListener != null)
	     this.mTransCommListener.onTransCommComplete( result, recv);
	}
	
	private TransCommTask mTransCommTask;
	public void CommitTrans(byte[] data, int len,TransCommListener Listener)
	{
		TOOL.ProgramSleep(300);
		if(Listener!=null)
		{
			mTransCommListener = Listener;	
		}
		if(data == null||data.length < len)
			return;
		byte[] buf = new byte[len];
		System.arraycopy(data, 0, buf, 0, len);
		
		mTransCommTask = new TransCommTask(buf);
	    this.mTransCommTask.execute(new String[0]);
	}
	
	public enum COMM_STATUS
	{
		COMM_STATUS_CONNECT_ERR,	
		COMM_STATUS_WRITE_ERR,		
		COMM_STATUS_READ_ERR,          
		COMM_STATUS_OTHER_ERR,    
		COMM_STATUS_SUCC
	};
	private class TransCommTask extends AsyncTask<String, Integer, byte[]>
	{
		private Socket conn = null;
		private InputStream is = null;
		private OutputStream os = null;
		private boolean flag = true;
		private byte[] sendData;
		private COMM_STATUS state;
		
	    public TransCommTask(byte[] data)
	    {
	    	sendData = data;	

	try {
		if(D)Log.i(TAG,"sendData : " + new String(sendData,"GBK"));
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	    	
	    }
	    
	    protected byte[] doInBackground(String[] param)
	    {
if(D)Log.i(TAG,"+++++++++++++++doInBackground++++++++++++++++++");	    	
			int len = 0;
			boolean flag = true;
			byte [] readBuffer = new byte[2048];
			byte[] packt = sendData;
			
			
			if(!connect())
				return null;
			
			
			try {
				os.write(packt, 0, packt.length);
				os.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				state = COMM_STATUS.COMM_STATUS_WRITE_ERR;
				return null;
			}
			
			try {	
				is = new BufferedInputStream(conn.getInputStream());	
				
				int count = 0;
				dataLen = 0;
				conn.setSoTimeout(10*1000);
	
				len = is.read(readBuffer);	
				

			}catch (ConnectException e)
			{
				e.printStackTrace();
				state = COMM_STATUS.COMM_STATUS_CONNECT_ERR;
				return null;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				state = COMM_STATUS.COMM_STATUS_OTHER_ERR;
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block		
				e.printStackTrace();
				state = COMM_STATUS.COMM_STATUS_READ_ERR;
				return null;
			}finally{
				
				SafeClose();
			}

			if(len >0)
			{		

				byte out[] = new byte[len];
				System.arraycopy(readBuffer, 0, out, 0, len);
				state = COMM_STATUS.COMM_STATUS_SUCC;
				return out;
				
			}
		  state = COMM_STATUS.COMM_STATUS_OTHER_ERR;
	      return null;
	    }

	    protected void onPostExecute(byte[] param)
	    {
	    	
if(D)Log.i(TAG,"+++++++++++++++onPostExecute++++++++++++++++++");	    	
	    	if(state == COMM_STATUS.COMM_STATUS_CONNECT_ERR)
	    	{
	    		ShowStatus("连接失败!");
	    		onTransCommComplete( state, null);
	    	}else if(state == COMM_STATUS.COMM_STATUS_WRITE_ERR)
	    	{
	    		ShowStatus("发送数据异常!");
	    		onTransCommComplete( state, null);
	    	}else if(state == COMM_STATUS.COMM_STATUS_READ_ERR)
	    	{	    		
	    		ShowStatus("接收数据异常!");
	    		onTransCommComplete( state, null);
	    	}else if(state == COMM_STATUS.COMM_STATUS_OTHER_ERR)
	    	{
	    		ShowStatus("未知错误!");
	    		onTransCommComplete( state, null);
	    	}else 
	    	{
	    		ShowStatus("接收完成!");
	    		if(D)
	    			try {
	    				Log.i(TAG,"recvData : " + new String(param,"UTF-8"));
	    			} catch (UnsupportedEncodingException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}	    		
	    		onTransCommComplete( state, param);
	    	}
	    	this.cancel(true);
	    }

	    protected void onPreExecute()
	    {
if(D)Log.i(TAG,"+++++++++++++++onPreExecute++++++++++++++++++");	 	    	
	      super.onPreExecute();
	    }

	    protected void onProgressUpdate(Integer[] paramArrayOfInteger)
	    {
	      super.onProgressUpdate(paramArrayOfInteger);
if(D)Log.i(TAG,"+++++++++++++++onProgressUpdate++++++++++++++++++");		      
	    }
	    
		private boolean ConnectServer()
		{
		    try
		    {
		      this.conn = new Socket(mDstName, mDstPort);
		      this.os = this.conn.getOutputStream();
		      this.is = this.conn.getInputStream();
		     
		    }
		    catch (IOException localIOException)
		    {
		      localIOException.printStackTrace();
		      return false;
		    }
		    catch (Exception localException)
		    {
		      localException.printStackTrace();
		      return false;
		    }
		    return true;
		}
		  
		private void SafeClose()
		{
			    
			try
			{
			  if (this.is != null);
			  {
			      this.is.close();
			      this.is = null;
			  }
			  if (this.os == null);
			  {
				  this.os.close();
				  this.os = null;
			  }
			  if (this.conn == null)
			  {
			      this.conn.close();
			      this.conn = null;
			      return;
			  }
			  
			}
			catch (IOException localIOException)
			{
			   localIOException.printStackTrace();
			}
			
		}
		
		
		private boolean connect()
		{
//		    ShowStatus("正在连接中心...");
		    if (!ConnectServer())
		    {
		      System.out.println("PreConnect fail");
//		      ShowStatus( "连接失败!");
		      state = COMM_STATUS.COMM_STATUS_CONNECT_ERR;
		      return false;
		    }
//		    ShowStatus("成功连接中心\r\n正在接收数据...");
		    return true;
		}
	  }
	
}