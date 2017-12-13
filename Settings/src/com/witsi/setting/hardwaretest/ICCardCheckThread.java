package com.witsi.setting.hardwaretest;

import java.util.Timer;
import java.util.TimerTask;

import com.witsi.debug.FyLog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.witsi.arq.ArqConverts;
import android.witsi.arq.ArqIcc;

public class ICCardCheckThread implements Runnable{

	private static String TAG = ICCardCheckThread.class.getSimpleName();
	public static final int CARD_EXIST = 0;
	public static final int CARD_NOT_EXIST = 1;
	public static final int READ_SUCCESS = 2;
	public static final int CARD_EXIST_POWER_ON = 3;
	public static final int CARD_ATR = 4;
	public static final int CARD_RANDOM = 5;
	public static final int CARD_POWER_DOWN = 6;
	private int slot = 0;
	private Handler handler;
	private boolean isThreadRunning = false;
	private boolean exitThread = false;
	private boolean isTimerOut = false;
	
	private Timer timer = null;
	private ArqIcc iccArq;
	private final byte[] GET_RANDOM_CMD = new byte[] { 0x00, (byte) 0x84, 0x00, 0x00, 0x08 };
	
	
	public ICCardCheckThread(Context context, Handler handler, int slot) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.slot = slot;
		// SysApplication.getInstance().addActivity(this);
		iccArq = new ArqIcc(context);
//		timer = new Timer();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		byte[] rbuf = new byte[1024];
		int ret = -1, flag = -1;
		while(!exitThread && isThreadRunning){
			//检测芯片卡是否存在
			ret = iccArq.iccPresent(rbuf);
			if (ret < 0) {
				FyLog.e(TAG, "icc present check failed ret = " + ret);
				continue;
			}else{
				break;
			}
		}
			handler.sendEmptyMessage(READ_SUCCESS);
			flag = (rbuf[slot] & 0x00ff);
			if (flag == 0x00) {
				/* 没有卡存在 */
				FyLog.d(TAG, "slot-" + slot + " has no card exist!");
				handler.sendEmptyMessage(CARD_NOT_EXIST);
			} else if (flag == 0x01) {
				/* 卡存在但是没上电 */
				if(slot != 1){
					handler.sendEmptyMessage(CARD_EXIST);
					FyLog.d(TAG, "slot-" + slot + " has card exist!");
				}
			} else if (flag == 0x03) {
				FyLog.d(TAG, "card exist and already power up!");
				handler.sendEmptyMessage(CARD_EXIST_POWER_ON);
			} else {
				FyLog.e(TAG, "Wrong return value: flag = " + flag);
			}
			
			//芯片卡存在，但是未上电
			if (flag != 0x03) {
				ret = iccArq.iccPowerUp(slot, rbuf);
				if (ret < 0) {
					FyLog.e(TAG, "icc power up failed.");
					if(slot == 1)
						handler.sendEmptyMessage(CARD_NOT_EXIST);
					return;
				}
			}
			
			//上电成功，开始读取IC卡数据
			/* get ATR value. */
			byte[] atr_buf = new byte[ret];
			for (int i = 0; i < ret; i++) {
				atr_buf[i] = rbuf[i];
			}
			Message msg = handler.obtainMessage();
			msg.what = CARD_ATR;
			if(ret > 0){
				msg.obj = ArqConverts.bytesToHexString(atr_buf, '-');
				if(slot == 1){
					handler.sendEmptyMessage(CARD_EXIST);
					FyLog.d(TAG, "slot-" + slot + " has card exist!");
				}
			}else {
				msg.obj = "无";
			}
			handler.sendMessage(msg);
			
			/* get random */
			ret = iccArq.iccApdu(slot, GET_RANDOM_CMD,
					GET_RANDOM_CMD.length, rbuf); // 3s
			if (ret < 0) {
				FyLog.e(TAG, "send apdu failed.");
				return;
			}
			byte[] rdm_buf = new byte[ret];
			for (int i = 0; i < ret; i++) {
				rdm_buf[i] = rbuf[i];
			}
			msg = handler.obtainMessage();
			msg.what = CARD_RANDOM;
			if(ret > 0)
				msg.obj = ArqConverts.bytesToHexString(rdm_buf, '-');
			else 
				msg.obj = "无";
			handler.sendMessage(msg);
			/* power down */
			ret = iccArq.iccPowerDown(slot);
			if (ret < 0) {
				FyLog.e(TAG, "icc power down failed.");
				if(slot == 1)
					handler.sendEmptyMessage(CARD_NOT_EXIST);
			} else {
				FyLog.i(TAG, "icc power down success.");
				handler.sendEmptyMessage(CARD_POWER_DOWN);
				if(slot == 1)
					handler.sendEmptyMessage(CARD_EXIST);
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void stopRunning(){
		isThreadRunning = false;
		timer.cancel();
	}
	public void startRunning(){
		if(!isThreadRunning){
			isThreadRunning = true;
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(isThreadRunning != false){
						/* power down */
						int ret = iccArq.iccPowerDown(slot);
						if (ret < 0) {
							FyLog.e(TAG, "time out and icc power down failed.");
						} else {
							FyLog.d(TAG, "time out and icc power down success.");
						}
						isThreadRunning = false;
						if(timer != null)
							timer.cancel();
					}
				}
			}, 5000, 1000);
		}
	}
	public void exitThread(){
		isThreadRunning = false;
		exitThread = true;
		if(timer != null){
			timer.cancel();
			timer = null;
		}
	}
}
