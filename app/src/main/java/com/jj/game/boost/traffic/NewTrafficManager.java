package com.jj.game.boost.traffic;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.utils.SystemUtil;

/**
 * @author myx
 *         by 2017-06-05
 */
class NewTrafficManager extends AbstractTrafficManager {

    private static final long serialVersionUID = 7333650008593662594L;
    @SuppressWarnings("unused")
    public static final long TRAFFIC_REQUEST_GET_USAGE_STATS = -2;
    @SuppressWarnings("unused")
    public static final long TRAFFIC_REQUEST_READ_PHONE_STATE = -3;

    NewTrafficManager() {

    }

    @RequiresApi(api = 23)
    @Override
    public long getUidRxBytes(Context context, int uid) {
//        LogUtil.i("NewTrafficManager getUidRxBytes()");
        long uidRxBytesMobile = getUidRxBytesMobile(uid);
        long uidRxBytesWifi = getUidRxBytesWifi(uid);
//        LogUtil.i("uidRxBytesMobile -> " + uidRxBytesMobile);
//        LogUtil.i("uidRxBytesWifi -> " + uidRxBytesWifi);
        if (uidRxBytesMobile == TrafficStats.UNSUPPORTED) {
            uidRxBytesMobile = 0;
            if (uidRxBytesWifi == TrafficStats.UNSUPPORTED) {
                return 0;
            }
        } else {
            if (uidRxBytesWifi == TrafficStats.UNSUPPORTED) {
                uidRxBytesWifi = 0;
            }
        }
        return uidRxBytesMobile + uidRxBytesWifi;
    }

    @RequiresApi(api = 23)
    @Override
    public long getUidRxBytesMobile(int uid) {
        return getUidBytes(uid, ConnectivityManager.TYPE_MOBILE)[0];
    }

    @RequiresApi(api = 23)
    @Override
    public long getUidRxBytesWifi(int uid) {
        return getUidBytes(uid, ConnectivityManager.TYPE_WIFI)[0];
    }

    @RequiresApi(api = 23)
    @Override
    public long getUidTxBytes(Context context, int uid) {
//        LogUtil.i("NewTrafficManager getUidTxBytes()");
        long uidTxBytesMobile = getUidTxBytesMobile(uid);
        long uidTxBytesWifi = getUidTxBytesWifi(uid);
//        LogUtil.i("uidTxBytesMobile -> " + uidTxBytesMobile);
//        LogUtil.i("uidTxBytesWifi -> " + uidTxBytesWifi);
        if (uidTxBytesMobile == TrafficStats.UNSUPPORTED) {
            uidTxBytesMobile = 0;
            if (uidTxBytesWifi == TrafficStats.UNSUPPORTED) {
                return 0;
            }
        } else {
            if (uidTxBytesWifi == TrafficStats.UNSUPPORTED) {
                uidTxBytesWifi = 0;
            }
        }
        return uidTxBytesMobile + uidTxBytesWifi;
    }

    @RequiresApi(api = 23)
    @Override
    public long getUidTxBytesMobile(int uid) {
        return getUidBytes(uid, ConnectivityManager.TYPE_MOBILE)[1];
    }

    @RequiresApi(api = 23)
    @Override
    public long getUidTxBytesWifi(int uid) {
        return getUidBytes(uid, ConnectivityManager.TYPE_WIFI)[1];
    }

    @RequiresApi(api = 23)
    private long[] getUidBytes(int uid, int networkType) {
        NetworkStats summaryStats;
        long summaryRx = 0;
        long summaryTx = 0;
        NetworkStats.Bucket summaryBucket = new NetworkStats.Bucket();
//        long summaryTotal = 0;
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) JJBoostApplication.application
                .getSystemService(Context.NETWORK_STATS_SERVICE);
        long currentTimeMillis;
        try {
            currentTimeMillis = System.currentTimeMillis();
            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                summaryStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE,
                        SystemUtil.getSubscriberId(), currentTimeMillis - SystemClock.elapsedRealtime(),
                        currentTimeMillis);
            } else {
                summaryStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI,
                        "", currentTimeMillis - SystemClock.elapsedRealtime(),
                        currentTimeMillis);
            }
            if (summaryStats != null) {
                while (summaryStats.hasNextBucket()) {
                    summaryStats.getNextBucket(summaryBucket);

                    int summaryUid = summaryBucket.getUid();
                    if (uid == summaryUid) {
                        summaryRx += summaryBucket.getRxBytes();
                        summaryTx += summaryBucket.getTxBytes();
                    }
//                    summaryRx += summaryBucket.getRxBytes();
//                    summaryTx += summaryBucket.getTxBytes();

//                    summaryTotal += summaryRx + summaryTx;
                }
            }
            long[] arr = new long[2];
            arr[0] = summaryRx;
            arr[1] = summaryTx;
            return arr;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        long[] arr = new long[2];
        arr[0] = TrafficStats.UNSUPPORTED;
        arr[1] = TrafficStats.UNSUPPORTED;
        return arr;
    }

}
