package com.jj.game.boost.traffic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.jj.game.boost.utils.LogUtil;

/**
 * @author myx
 *         by 2017-06-12
 */
public class NetworkStatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("intent.getAction() -> " + intent.getAction());
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
            LogUtil.i("bssid -> " + bssid);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            LogUtil.i("networkInfo -> " + networkInfo);
            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
            LogUtil.i("wifiInfo -> " + wifiInfo);
            if (wifiInfo == null || bssid == null) {//如果关闭
                // 结余本次wifi过程中uid应用的流量
                LogUtil.i("结余本次wifi过程中uid应用的流量");
            } else {
                // 记录当前uid应用的流量
                LogUtil.i("记录当前uid应用的流量");
            }
        }
    }

}
