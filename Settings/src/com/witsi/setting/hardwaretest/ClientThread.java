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
	// ������UI�̷߳�����Ϣ��Handler����
	private Handler handler;
	private Handler timerHandler;
	// �������UI�̵߳���Ϣ��Handler����
	public Handler revHandler;
	// ���߳��������Socket����Ӧ��������
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
		FyLog.i("���߳�", "open");
		try {
			/* 0.1s��ʱ�侫�ȼ����������������ʱ�� */
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					timerdata++;
					if (timerdata >= 60) {
						timer.cancel();
						FyLog.i("ClientThread", "����ʱ�䶨ʱ���رգ���");
					}
				}
			}, 0, 100);
			FyLog.i("ClientThread", "����ʱ�䶨ʱ����������");
			try{
				s = new Socket("echo.u-blox.com", 7);
			}catch (SocketTimeoutException e1) {
				FyLog.w("ClientThread", "socket����ʧ�ܣ���");
			}
		//	s = new Socket("echo.u-blox.com", 7);
			// timer.cancel();
			isNetworkOK = true;
			FyLog.i("ClientThread", "����ʱ�䶨ʱ���رգ���");
			/* ���ڷ����������������ʱ�� */
//			new Thread() {
//				@Override
//				public void run() {					
					FyLog.i("С�̷߳��ͽ���ʱ��", "open");
					Message msg = new Message();
					msg.what = 0x1111;
					msg.obj = timerdata;
					timerHandler.sendMessage(msg);
//				}
//			}.start();
			FyLog.i("���ݽ��շ��ͻ���", "open");
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			os = s.getOutputStream();

			try {
				/* �������ݵ���������ȥ */
				FyLog.i("���Է�������", "open");
				os.write(("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890"
						+ "\r\n").getBytes("utf-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e1) {
			System.out.println("�������ӳ�ʱ����");
		} catch (Exception e) {
			e.printStackTrace();
		}
		FyLog.i("���ݷ���", "�ɹ�");
		String content = null;
		// ���϶�ȡSocket�������е����ݡ�
		try {
			if (isNetworkOK == true) {
				FyLog.i("ClientThread", "���Խ�������open");
				/* �ӷ������������ݷ��͵����� */
				while ((content = br.readLine()) != null) {
					FyLog.i("ClientThread", "����ѭ������");
					// ÿ���������Է�����������֮�󣬷�����Ϣ֪ͨ���������ʾ������
					if (content
							.equals("abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890")) {
						FyLog.i("ClientThread", "���Խ������ݳɹ�");
						Message msg = new Message();
						msg.what = 0x123;
						msg.obj = content;
						handler.sendMessage(msg);
						FyLog.i("ClientThread", "sock���Թر�");
						s.close();
						br.close();
						os.close();
						FyLog.i("ClientThread", "socket�Ѿ��ر�");
					}
				}
			} else {
				FyLog.i("ClientThread", "����û�гɹ�");
				try {
					FyLog.i("ClientThread", "sock���Թر�");
					s.close();
					br.close();
					os.close();
					FyLog.i("ClientThread", "socket�Ѿ��ر�");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		FyLog.i("socket�߳�ĩ��", "����");
	}
}
