package com.witsi.setting.hardwaretest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import com.witsi.debug.FyLog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ClientThread implements Runnable {
	Timer timer = new Timer();
	public Socket s;
	// 定义向UI线程发送消息的Handler对象
	private Handler handler;
	private Handler timerHandler;
	// 定义接收UI线程的消息的Handler对象
	public Handler revHandler;
	// 该线程所处理的Socket所对应的输入流
	public BufferedReader br = null;
	public OutputStream os = null;
	int timerdata = 0;
	public boolean isNetworkOK = false,sendThread = true;

	public ClientThread(Handler handler, Handler timerHandler) {
		this.handler = handler;
		this.timerHandler = timerHandler;
	}

	@Override
	public void run() {
		FyLog.i("大线程", "open");
		try {
			/* 0.1s的时间精度计算与服务器建立的时间 */
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					timerdata++;
					if (timerdata >= 60) {
						timer.cancel();
						FyLog.i("ClientThread", "建立时间定时器关闭！！");
					}
				}
			}, 0, 100);
			FyLog.i("ClientThread", "建立时间定时器开启！！");
			try{
				s = new Socket("echo.u-blox.com", 7);
			}catch (SocketTimeoutException e1) {
				FyLog.w("ClientThread", "socket建立失败！！");
			}
		//	s = new Socket("echo.u-blox.com", 7);
			// timer.cancel();
			isNetworkOK = true;
			FyLog.i("ClientThread", "建立时间定时器关闭！！");
			/* 用于发送与服务器建立的时间 */
//			new Thread() {
//				@Override
//				public void run() {					
					FyLog.i("小线程发送建立时间", "open");
					Message msg = new Message();
					msg.what = 0x1111;
					msg.obj = timerdata;
					timerHandler.sendMessage(msg);
//				}
//			}.start();
			FyLog.i("数据接收发送缓存", "open");
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			os = s.getOutputStream();

			try {
				/* 发送数据到服务器上去 */
				FyLog.i("尝试发送数据", "open");
				os.write(("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890"
						+ "\r\n").getBytes("utf-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e1) {
			System.out.println("网络连接超时！！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		FyLog.i("数据发送", "成功");
		String content = null;
		// 不断读取Socket输入流中的内容。
		try {
			if (isNetworkOK == true) {
				FyLog.i("ClientThread", "尝试接收数据open");
				/* 从服务器接收数据发送到界面 */
				while ((content = br.readLine()) != null) {
					FyLog.i("ClientThread", "进入循环接收");
					// 每当读到来自服务器的数据之后，发送消息通知程序界面显示该数据
					if (content
							.equals("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890")) {
						FyLog.i("ClientThread", "尝试接收数据成功");
						Message msg = new Message();
						msg.what = 0x123;
						msg.obj = content;
						handler.sendMessage(msg);
						FyLog.i("ClientThread", "sock尝试关闭");
						s.close();
						br.close();
						os.close();
						FyLog.i("ClientThread", "socket已经关闭");
					}
				}
			} else {
				FyLog.i("ClientThread", "连接没有成功");
				try {
					FyLog.i("ClientThread", "sock尝试关闭");
					s.close();
					br.close();
					os.close();
					FyLog.i("ClientThread", "socket已经关闭");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		FyLog.i("socket线程末端", "到达");
	}
}
