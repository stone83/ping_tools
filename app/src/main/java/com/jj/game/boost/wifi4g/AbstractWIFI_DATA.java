package com.jj.game.boost.wifi4g;

import com.jj.game.boost.JJBoostApplication;

/**
 * Created by huzd on 2017/7/6.
 */

public abstract class AbstractWIFI_DATA {
    public AbstractWIFI_DATA(){

    }

    public static String getNetType(){
        if(Wifi_4G_Utils.isWifi(JJBoostApplication.application)){
            return Const.WIFI;
        } else if(Wifi_4G_Utils.is3rd(JJBoostApplication.application)){
            return Const.DATA;
        } else {
            return "";
        }
    }
    public abstract boolean isEnable();
    public abstract String getNetName();
    public abstract int getNetLevel();
    public abstract void setNetState(boolean state);
}
