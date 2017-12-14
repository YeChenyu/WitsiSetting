package com.witsi.setting.hardwaretest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.witsi.debug.FyLog;

import android.R.string;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PingThread implements Runnable {
	String TAG = "PingThread";
	String addr = "192.168.2.1";
	public static boolean isPingOK = false;
	Message msg = null;
	String returndata = "";
	int pingnum = 4;
	Handler handler;
	boolean runable = true;
	int k = 0;
	int msgnum = 0;
	boolean pagflage = false;
	ThreadListener mListener;
	public abstract interface ThreadListener
	{
		public abstract void onComplete();
	}
	public PingThread(String addr, int pingnum, Handler handler, ThreadListener listener) {
		this.handler = handler;
		this.pingnum = pingnum;
		this.addr = addr;
		mListener = listener;
	}
	
	public PingThread(String addr, int pingnum, Handler handler) {
		this.handler = handler;
		this.pingnum = pingnum;
		this.addr = addr;
	}
	
	public PingThread(int pingnum, Handler handler) {
		this.handler = handler;
		this.pingnum = pingnum;
	}

	public PingThread(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		while (runable == true) {
			Process p = null;
			try {
				p = Runtime.getRuntime().exec(
						"/system/bin/ping -c " + pingnum + " " + addr);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			int status = 0;
			try {
				status = p.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (status == 0) {
				isPingOK = true;
				FyLog.i(TAG, "success");
			} else {
				isPingOK = false;
				FyLog.i(TAG, "false");
			}
			String lost = new String();
			String delay = new String();
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String str = new String();

			// 读出所有信息并显示
			try {
				while ((str = buf.readLine()) != null) {
					msgnum++;
					str = str + "\r\n";
					returndata = str;
					msg = new Message();
					msg.what = 0x123;
					msg.obj = pingDecode(returndata);
					FyLog.i(TAG, str);
					if (pingnum > 5) {
						if (msgnum < 8) {
							handler.sendMessage(msg);
						} else if (msgnum == 8) {
							msg.obj = "……";
							handler.sendMessage(msg);
						} else if (pagflage == true) {
							handler.sendMessage(msg);
						}
					} else {
						handler.sendMessage(msg);
					}
				}
				if(mListener!=null)
				{
					mListener.onComplete();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				buf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			p.destroy();
			runable = false;
		}
		FyLog.i(TAG, "线程结束！！！！！");
	}

	public String pingDecode(String data) {
		String decodedata = "";
		char[] mchar;
		int j = 0;
		String min = "";
		String max = "";
		String ave = "";
		int dataflag = 0;
		mchar = new char[data.length()];
		data.getChars(0, data.length(), mchar, 0);

		FyLog.i(TAG, data);
		if (mchar[0] == 'P' && mchar[1] == 'I') {
			decodedata = decodedata + data + "\r";
			return decodedata;
		}
		// 时间获取
		if (mchar[0] == '6' && mchar[1] == '4') {
			k++;
			decodedata = decodedata + "包" + k + ": ";
			for (int i = 0; i < data.length(); i++) {
				if (mchar[i] == 'e') {
					if (mchar[i + 1] == '=') {
						j = i + 2;
						while (mchar[j] != ' ') {
							decodedata = decodedata + mchar[j];
							j++;
						}
						decodedata = decodedata + " " + "ms" + "\r";
						FyLog.i(TAG, decodedata);
						FyLog.i(TAG, "***************************");
						return decodedata;
					}
				}
			}
		}
		// 获取丢包率
		if (data.length() > 10) {
			int w=0;
			while(mchar[w]!=' '){
				w++;
			}			
//			FyLog.i(TAG, "w:"+w+"check:"+mchar[2+w]+","+mchar[3+w]+","+mchar[4+w]);
			if (mchar[1+w] == 'p' && mchar[2+w] == 'a' && mchar[3+w] == 'c') {
				for (int i = 0; i < data.length(); i++) {
					if (mchar[i] == 'r' && mchar[i + 1] == 'e'
							&& mchar[i + 2] == 'c') {
						pagflage = true;
						decodedata = decodedata + "丢包率:";
						j = i + 9;
						while (mchar[j] != '%') {
							decodedata = decodedata + mchar[j];
							j++;
						}
						decodedata = decodedata + '%' + "\r";
						FyLog.i(TAG, decodedata);
						FyLog.i(TAG, "***************************");
						return decodedata;
					}
				}
			}

		}
		FyLog.i(TAG, decodedata);
		FyLog.i(TAG, "***************************");
		return "";
	}
}
