package com.ccmt.library.util;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetUtil {

    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_NO_NET = -1;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_WIFI = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_NET_2_G = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_NET_3_G = 3;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_NET_4_G = 4;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_CMWAP = 5;
    @SuppressWarnings("WeakerAccess")
    public static final int TYPE_UNKNOWN = 6;
    private static Uri PREFERRED_APN_URI = Uri
            .parse("content://telephony/carriers/preferapn");
    @SuppressWarnings("WeakerAccess")
    public static String proxyIp;
    @SuppressWarnings("WeakerAccess")
    public static int proxyPort;

    /**
     * 检查网络,如果是移动网络会设置wap代理ip和端口.
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static boolean checkNet(Context context) {
        // 检查是否存在可以利用的网络
        // WIFI、手机接入点（APN）
        boolean wifiConnected = isWIFIConnected(context);
        boolean mobileConnected = isMobileConnected(context);
        // 不可以——提示工作
        if (!wifiConnected && !mobileConnected) {
            return false;
        }
        // 可以
        // 明确到底是哪个渠道可以使用
        if (mobileConnected) {
            // 如果当前的是wap方式通信：代理信息——没有固定
            // 信息变动
            // IP是10.0.0.172 端口是80 ip：010.000.000.172 80
            // 读取：数据库
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            String extraInfo = activeNetInfo.getExtraInfo();
            if (extraInfo != null && extraInfo.toLowerCase(Locale.getDefault()).equals("cmwap")) {
                readAPN(context);
            }
        }
        return true;
    }

    /**
     * 读取apn信息针对于Wap方式
     *
     * @param context
     */
    @SuppressWarnings("JavaDoc")
    private static void readAPN(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        // 获取当前处于活动状态的APN的信息
        Cursor query = null;
        try {
            query = contentResolver.query(PREFERRED_APN_URI, null, null,
                    null, null);
            if (proxyIp == null || proxyPort == 0) {
                if (query != null && query.moveToNext()) {
                    proxyIp = query.getString(query.getColumnIndex("proxy"));
                    proxyPort = query.getInt(query.getColumnIndex("port"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (query != null) {
                query.close();
            }
        }
    }

    /**
     * 判断wifi是否可以连接
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "deprecation", "WeakerAccess"})
    public static boolean isWIFIConnected(Context context) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        return activeNetInfo != null && activeNetInfo.isConnected();
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks == null || networks.length == 0) {
                return false;
            }
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo != null && networkInfo.isConnected()) {
                    int type = networkInfo.getType();
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        return true;
                    }
                }
            }
        } else {
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if (networkInfos == null || networkInfos.length == 0) {
                return false;
            }
            for (NetworkInfo networkInfo : networkInfos) {
                if (networkInfo != null && networkInfo.isConnected()) {
                    int type = networkInfo.getType();
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断手机接入点是否可以连接
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "deprecation", "WeakerAccess"})
    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            Network[] networks = connectivityManager.getAllNetworks();
            if (networks == null || networks.length == 0) {
                return false;
            }
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = connectivityManager.getNetworkInfo(network);
                if (networkInfo != null && networkInfo.isConnected()) {
                    int type = networkInfo.getType();
                    if (type == ConnectivityManager.TYPE_MOBILE
                            || type == ConnectivityManager.TYPE_MOBILE_DUN
                            || type == ConnectivityManager.TYPE_MOBILE_HIPRI
                            || type == ConnectivityManager.TYPE_MOBILE_MMS
                            || type == ConnectivityManager.TYPE_MOBILE_SUPL) {
                        return true;
                    }
                }
            }
        } else {
            NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
            if (networkInfos == null || networkInfos.length == 0) {
                return false;
            }
            for (NetworkInfo networkInfo : networkInfos) {
                if (networkInfo != null && networkInfo.isConnected()) {
                    int type = networkInfo.getType();
                    if (type == ConnectivityManager.TYPE_MOBILE
                            || type == ConnectivityManager.TYPE_MOBILE_DUN
                            || type == ConnectivityManager.TYPE_MOBILE_HIPRI
                            || type == ConnectivityManager.TYPE_MOBILE_MMS
                            || type == ConnectivityManager.TYPE_MOBILE_SUPL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    /**
//     * 网络是否可用
//     *
//     * @param context
//     * @return
//     */
//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager connectivity = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity == null) {
//            return false;
//        }
//        NetworkInfo[] info = connectivity.getAllNetworkInfo();
//        if (info != null) {
//            for (int i = 0; i < info.length; i++) {
//                if (info[i].isConnected()) {
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true表示开启
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static void openGPS(Context context) {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * wifi是否打开
//     */
//    @SuppressWarnings("unused")
//    public static boolean isWifiEnabled(Context context) {
//        ConnectivityManager mgrConn = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = mgrConn.getActiveNetworkInfo();
//        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED
//                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
//    }

//    /**
//     * 判断当前网络是否是wifi网络
//     * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //判断3G网
//     *
//     * @param context
//     * @return boolean
//     */
//    @SuppressWarnings({"JavaDoc", "unused"})
//    public static boolean isWifi(Context context) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetInfo != null && activeNetInfo.isConnected()
//                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI;
//    }

//    /**
//     * 判断当前网络是否是3G网络
//     *
//     * @param context
//     * @return boolean
//     */
//    @SuppressWarnings({"JavaDoc", "unused"})
//    public static boolean is3G(Context context) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetInfo != null && activeNetInfo.isConnected()
//                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE;
//    }

    /**
     * 判断当前是否有网络
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "unused"})
    public static boolean checkNetwork(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * GPRS    2G(2.5) General Packet Radia Service 114kbps
     * EDGE    2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
     * UMTS    3G WCDMA 联通3G Universal MOBILE Telecommunication System 完整的3G移动通信技术标准
     * CDMA    2G 电信 Code Division Multiple Access 码分多址
     * EVDO_0  3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
     * EVDO_A  3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
     * 1xRTT   2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,
     * HSDPA   3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps
     * HSUPA   3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
     * HSPA    3G (分HSDPA,HSUPA) High Speed Packet Access
     * IDEN    2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
     * EVDO_B  3G EV-DO Rev.B 14.7Mbps 下行 3.5G
     * LTE     4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
     * EHRPD   3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
     * HSPAP   3G HSPAP 比 HSDPA 快些
     * <p>
     * 获取当前的网络状态,但是不设置wap代理ip和端口,-1代表没有网络,1代表WIFI网络,2代表2G网络,
     * 3代表3G网络,4代表4G网络,5代表wap网络,6代表未知网络.
     *
     * @param context
     * @return
     */
    @SuppressWarnings({"JavaDoc", "deprecation", "unused"})
    public static int getNetworkType(Context context) {
        int result = TYPE_NO_NET;
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String extraInfo = networkInfo.getExtraInfo();
            Log.i("MyLog", "extraInfo -> " + extraInfo);
            int networkInfoType = networkInfo.getType();
            Log.i("MyLog", "networkInfoType -> " + networkInfoType);
            if (networkInfoType == ConnectivityManager.TYPE_WIFI) {
                // LogUtils.i("wifi");
                result = TYPE_WIFI;
            } else if (networkInfoType == ConnectivityManager.TYPE_MOBILE
                    || networkInfoType == ConnectivityManager.TYPE_MOBILE_DUN
                    || networkInfoType == ConnectivityManager.TYPE_MOBILE_HIPRI
                    || networkInfoType == ConnectivityManager.TYPE_MOBILE_MMS
                    || networkInfoType == ConnectivityManager.TYPE_MOBILE_SUPL) {
                if (!extraInfo.toLowerCase(Locale.getDefault()).equals("cmwap")) {
                    int networkType = ((TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType();
                    Log.i("MyLog", "networkType -> " + networkType);
                    switch (networkType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            result = TYPE_NET_2_G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                        case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                            result = TYPE_NET_3_G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                        case TelephonyManager.NETWORK_TYPE_IWLAN:
                            result = TYPE_NET_4_G;
                            break;
                        default:
                            String subtypeName = networkInfo.getSubtypeName();
                            if (subtypeName.equalsIgnoreCase("WCDMA")
                                    || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                result = TYPE_NET_3_G;
                            } else {
                                result = TYPE_UNKNOWN;
                            }
                            break;
                    }
                } else {
                    result = TYPE_CMWAP;
                }
            } else {
                result = TYPE_UNKNOWN;
            }
        }
        return result;
    }

    /**
     * 判断移动数据是否打开
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    @SuppressWarnings({"TryWithIdenticalCatches", "unused"})
    public static boolean isDataEnabled(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled");
            if (getMobileDataEnabledMethod != null) {
                getMobileDataEnabledMethod.setAccessible(true);
                return (boolean) getMobileDataEnabledMethod.invoke(tm);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Build.VERSION.SDK_INT >= 24) {
                Method getMobileDataEnabledMethod;
                try {
                    getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled", int.class);
                    if (getMobileDataEnabledMethod != null) {
                        getMobileDataEnabledMethod.setAccessible(true);
                        return (boolean) getMobileDataEnabledMethod.invoke(tm, SubscriptionManager.getDefaultDataSubscriptionId());
                    }
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 打开或关闭移动数据,需要提升为系统应用才有效.
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    @SuppressWarnings({"TryWithIdenticalCatches", "unused"})
    public static void setDataEnabled(Context context, boolean enabled) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (setMobileDataEnabledMethod != null) {
                setMobileDataEnabledMethod.setAccessible(true);
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("MyLog", "e -> " + e);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Method setMobileDataEnabledMethod;
                try {
                    setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", int.class, boolean.class);
                    if (setMobileDataEnabledMethod != null) {
                        setMobileDataEnabledMethod.setAccessible(true);
                        setMobileDataEnabledMethod.invoke(tm, SubscriptionManager.getDefaultDataSubscriptionId(), enabled);
                    }
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                    Log.i("MyLog", "e2 -> " + e2);
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                    Log.i("MyLog", "e2 -> " + e2);
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                    Log.i("MyLog", "e2 -> " + e2);
                }
            }
        }
    }

    /**
     * 获取IP地址
     *
     * @param useIPv4 是否用IPv4
     * @return IP地址
     */
    @SuppressWarnings("unused")
    public static String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                NetworkInterface ni = nis.nextElement();
                // 防止小米手机返回10.0.2.15
                if (!ni.isUp()) {
                    continue;
                }
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return hostAddress;
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过域名获取ip地址
     *
     * @param domain 域名 只能是www.baidu.com的形式
     * @return ip地址
     */
    @SuppressWarnings("unused")
    public static String getDomainAddress(final String domain) {
        try {
            ExecutorService exec = Executors.newCachedThreadPool();
            Future<String> fs = exec.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    InetAddress inetAddress;
                    try {
                        inetAddress = InetAddress.getByName(domain);
                        return inetAddress.getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            return fs.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}