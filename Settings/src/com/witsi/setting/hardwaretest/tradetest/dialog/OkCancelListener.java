package com.witsi.setting.hardwaretest.tradetest.dialog;



public abstract interface OkCancelListener
{
	public abstract void onOk(String text);
	public abstract void onCancel();
	public abstract void onTimeOut();
}