package com.jj.game.boost.wifi4g;

import com.ccmt.library.util.NetUtil;
import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.signalstrength.SignalStrengthFactory;
import com.jj.game.boost.utils.TelephonyUtil;

/**
 * Created by huzd on 2017/7/6.
 */

public class DataControl extends AbstractWIFI_DATA {
    @Override
    public boolean isEnable() {
        return NetUtil.isMobileConnected(JJBoostApplication.application);
    }

    @Override
    public String getNetName() {
        return TelephonyUtil.getSimOperator(JJBoostApplication.application);
    }

    @Override
    public int getNetLevel() {
        return SignalStrengthFactory.createSignalStrength().getLevel(JJBoostApplication.application, null);
    }

    @Override
    public void setNetState(boolean state) {

    }
}
