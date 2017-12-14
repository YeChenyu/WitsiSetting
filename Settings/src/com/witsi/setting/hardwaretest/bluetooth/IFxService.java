package com.witsi.setting.hardwaretest.bluetooth;

import android.view.View;

public interface IFxService
{
  public abstract View getView();
  public abstract void addOnClink(FxServiceOnClink onClink);

}