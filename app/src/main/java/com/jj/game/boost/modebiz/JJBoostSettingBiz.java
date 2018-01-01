package com.jj.game.boost.modebiz;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.PreferenceUtils;

import static com.jj.game.boost.utils.PreferenceUtils.getPrefBoolean;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostSettingBiz implements IJJBoostSettingBiz{
    public static final String KEY_STARTGAME = "startgame";
    public static final String KEY_STARTWINDOW = "startwindow";

    @Override
    public void setBoostStart(boolean ison) {
        PreferenceUtils.setPrefBoolean(JJBoostApplication.application, KEY_STARTGAME, ison);
        LogUtil.e("huzedong", " set startgame : " + ison);
    }

    @Override
    public void setBoostFloatWindow(boolean ison) {
        PreferenceUtils.setPrefBoolean(JJBoostApplication.application, KEY_STARTWINDOW, ison);
        LogUtil.e("huzedong", " set floatwindow : " + ison);
    }

    @Override
    public boolean getBoostStart() {
        boolean startgame = PreferenceUtils.getPrefBoolean(JJBoostApplication.application, KEY_STARTGAME, false);
        LogUtil.e("huzedong", " get startgame : " + startgame);
        return startgame;
    }

    @Override
    public boolean getBoostFloatWindow() {
        boolean floatwindow = getPrefBoolean(JJBoostApplication.application, KEY_STARTWINDOW, false);
        LogUtil.e("huzedong", " get floatwindow : " + floatwindow);
        return floatwindow;
    }
}
