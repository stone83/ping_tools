package com.jj.game.boost.wifi4g;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.jj.game.boost.JJBoostApplication;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by huzd on 2017/7/6.
 */

public class WifiControl extends AbstractWIFI_DATA {
    private WifiManager wifi_service = null;
    private WifiInfo wifiInfo = null;

    public WifiControl(){
        wifi_service = (WifiManager)JJBoostApplication.application.getSystemService(WIFI_SERVICE);
        wifiInfo = wifi_service.getConnectionInfo();
    }
    @Override
    public boolean isEnable() {
        return Wifi_4G_Utils.isWifiEnabled(JJBoostApplication.application);
    }

    @Override
    public String getNetName() {
        return wifiInfo.getSSID();
    }

    @Override
    public int getNetLevel() {
        return wifiInfo.getRssi();
    }

    @Override
    public void setNetState(boolean state) {
       wifi_service.setWifiEnabled(state);
    }
}
