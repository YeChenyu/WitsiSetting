package com.witsi.setting.hardwaretest.tradetest.base;

import com.witsi.setting.hardwaretest.tradetest.base.TransComm.COMM_STATUS;

public abstract interface TransCommListener
{

  public abstract void onTransCommComplete(COMM_STATUS state, byte[] recv);
  
  public abstract void transCommUI(String paramString);
}