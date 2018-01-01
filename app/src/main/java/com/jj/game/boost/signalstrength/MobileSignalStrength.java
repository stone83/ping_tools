package com.jj.game.boost.signalstrength;

import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ReflectUtils;

import java.util.List;

/**
 * @author myx
 *         by 2017-07-07
 */
public class MobileSignalStrength extends AbstractSignalStrength {

    private static final int WCDMA_SIGNAL_STRENGTH_MODERATE = 5;
    private static final int WCDMA_SIGNAL_STRENGTH_GOOD = 8;
    private static final int WCDMA_SIGNAL_STRENGTH_GREAT = 12;

    @Override
    public int getLevel(Context context, SignalStrength signalStrength) {
        if (signalStrength == null) {
            // 新api,获取CellInfo对象的方式.
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            List<CellInfo> cellInfos;
            if (Build.VERSION.SDK_INT < 17) {
                return SIGNAL_STRENGTH_NO_HAVE;
            }
            cellInfos = telephonyManager.getAllCellInfo();
            if (cellInfos == null) {
                return SIGNAL_STRENGTH_NO_HAVE;
            }
            int size = cellInfos.size();
            if (size == 0) {
                return SIGNAL_STRENGTH_NO_HAVE;
            }
            CellInfo cellInfo;
            CellSignalStrengthWcdma cellSignalStrengthWcdma;
            for (int i = 0; i < size; i++) {
                LogUtil.i("------");
                cellInfo = cellInfos.get(i);
                LogUtil.i("cellInfo -> " + cellInfo);
                if (cellInfo.isRegistered()) {
                    if (cellInfo instanceof CellInfoGsm) {
                        LogUtil.i("gsm");
                        return ((CellInfoGsm) cellInfo).getCellSignalStrength().getLevel();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        LogUtil.i("cdma");
                        return ((CellInfoCdma) cellInfo).getCellSignalStrength().getLevel();
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        LogUtil.i("wcdma");
                        if (Build.VERSION.SDK_INT >= 18) {
                            LogUtil.i("api大于等于18");
                            return ((CellInfoWcdma) cellInfo).getCellSignalStrength().getLevel();
                        }
                        LogUtil.i("api小于18");
//                                int asu = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getAsuLevel();
                        cellSignalStrengthWcdma = (CellSignalStrengthWcdma) ReflectUtils
                                .obtainNonStaticFieldValue(cellInfo, "mCellSignalStrengthWcdma");
                        if (cellSignalStrengthWcdma == null) {
                            return SIGNAL_STRENGTH_NO_HAVE;
                        }
                        Integer asu = (Integer) ReflectUtils
                                .obtainNonStaticFieldValue(cellSignalStrengthWcdma, "mSignalStrength");
                        if (asu == null) {
                            return SIGNAL_STRENGTH_NO_HAVE;
                        }
                        if (asu <= 2 || asu == 99) {
                            return SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
                        } else if (asu >= WCDMA_SIGNAL_STRENGTH_GREAT) {
                            return SIGNAL_STRENGTH_GREAT;
                        } else if (asu >= WCDMA_SIGNAL_STRENGTH_GOOD) {
                            return SIGNAL_STRENGTH_GOOD;
                        } else if (asu >= WCDMA_SIGNAL_STRENGTH_MODERATE) {
                            return SIGNAL_STRENGTH_MODERATE;
                        } else {
                            return SIGNAL_STRENGTH_POOR;
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        LogUtil.i("lte");
                        return ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel();
                    }
                    break;
                }
            }
        } else {
            // 0到4,小米手机出现过最高为5的情况.
            Integer level;
            if (Build.VERSION.SDK_INT >= 23) {
                level = signalStrength.getLevel();
            } else {
                level = (Integer) ReflectUtils.invokeNonStaticMethod(signalStrength, "getLevel", null);
            }
            LogUtil.i("level -> " + level);
            if (level == null) {
                return SIGNAL_STRENGTH_NO_HAVE;
            }
            return level;
        }
        return 0;
    }

}
