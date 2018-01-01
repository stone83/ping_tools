package com.jj.game.boost.signalstrength;

import android.content.Context;
import android.telephony.SignalStrength;

/**
 * @author myx
 *         by 2017-07-07
 */
public interface ISignalStrength {

    int SIGNAL_STRENGTH_NO_HAVE = -1;
    int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    int SIGNAL_STRENGTH_POOR = 1;
    int SIGNAL_STRENGTH_MODERATE = 2;
    int SIGNAL_STRENGTH_GOOD = 3;
    int SIGNAL_STRENGTH_GREAT = 4;

    /**
     * 获取信号级别
     *
     * @param contex         上下文
     * @param signalStrength 如果不为空,采用api17以前的方式.否则采用api17或以后的方式,也就是通过获取CellInfo对象的方式.
     * @return 信号级别
     */
    int getLevel(Context contex, SignalStrength signalStrength);

}
