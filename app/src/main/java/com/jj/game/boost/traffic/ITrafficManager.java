package com.jj.game.boost.traffic;

import android.content.Context;

import java.io.Serializable;

/**
 * @author myx
 *         by 2017-06-05
 */
public interface ITrafficManager extends Serializable {

    /**
     * 获取指定uid接收的总数据,包括流量和wifi.
     *
     * @param context
     * @param uid
     * @return
     */
    @SuppressWarnings("JavaDoc")
    long getUidRxBytes(Context context, int uid);

    /**
     * 获取指定uid接收的流量,不包括wifi.
     *
     * @param uid
     * @return
     */
    @SuppressWarnings("JavaDoc")
    long getUidRxBytesMobile(int uid);

    /**
     * 获取指定uid接收的wifi,不包括流量.
     *
     * @param uid
     * @return
     */
    @SuppressWarnings("JavaDoc")
    long getUidRxBytesWifi(int uid);

    /**
     * 获取指定uid发送的总数据,包括流量和wifi.
     *
     * @param uid
     * @return
     */
    @SuppressWarnings("JavaDoc")
    long getUidTxBytes(Context context, int uid);

    long getUidTxBytesMobile(int uid);

    long getUidTxBytesWifi(int uid);

}
