package com.jj.game.boost.modebiz;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.domain.ProcessInfo;
import com.jj.game.boost.utils.LogUtil;
import android.content.Context;

import com.jj.game.boost.utils.CommonUtil;
import com.jj.game.boost.view.JJBoostMainActivity;

import java.util.List;

import static com.jj.game.boost.modebiz.JJBoostSettingBiz.KEY_STARTGAME;
import static com.jj.game.boost.utils.PreferenceUtils.getPrefBoolean;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostDetectBiz implements IJJBoostDetectBiz {

    public static List<Object> sList;
    public boolean mIsShowCurRun = true;
    public List<ProcessInfo> mExcessProcessInfos;

    @Override
    public String getCurrentNetType() {
        return null;
    }

    @Override
    public String getCurrentNetName() {
        return null;
    }

    @Override
    public String getCurrentNetLevel() {
        return null;
    }

    @Override
    public boolean getBoostStart() {
        boolean startgame = getPrefBoolean(JJBoostApplication.application, KEY_STARTGAME, false);
        LogUtil.e("huzedong", " get startgame : " + startgame);
        return startgame;
    }

//    @Override
//    public List<ProcessInfo> getCurrentProcessInfo() {
//        return null;
//    }
    public boolean isPackageNameRunning(Context context,String packageName) {
        return CommonUtil.isRunningApp(context,packageName, JJBoostMainActivity.CURRENT_PKG);
    }
}
