package com.jj.game.boost.modebiz;

import android.content.Context;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.utils.CommonUtil;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.view.JJBoostMainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.jj.game.boost.modebiz.JJBoostSettingBiz.KEY_STARTGAME;
import static com.jj.game.boost.modebiz.JJBoostSettingBiz.KEY_STARTWINDOW;
import static com.jj.game.boost.utils.PreferenceUtils.getPrefBoolean;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostMainBiz implements IJJBoostMainBiz {
    public static final String PING = "www.baidu.com";
    public static final String EXE = "ping -c 1 " + PING;
    public long mTotalNetworkSpeed = -1;

    @Override
    public String getCurSpeed() {
        return mTotalNetworkSpeed + "";
    }

    @Override
    public String getCurDelay() {
        String delay = "";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(EXE);
            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str;
            while((str=buf.readLine())!=null){
                if(str.contains("avg")){
                    int i=str.indexOf("/", 20);
                    int j=str.indexOf(".", i);
                    delay =str.substring(i+1, j);
                    delay = delay+"ms";
                }
                LogUtil.e("huzedong", " delay : " + delay);
            }
        } catch (IOException e) {
            LogUtil.e("huzedong", " delay exception");
            e.printStackTrace();
        }
        if(null != delay && !delay.equals("")){
            delay = delay.replace("ms", "");
        }
        return delay;
    }

    @Override
    public String getCurLost() {
        String lost = "";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(EXE);
            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str;
            while((str=buf.readLine())!=null){
                if(str.contains("packet loss")){
                    int i= str.indexOf("received");
                    int j= str.indexOf("%");
                    lost = str.substring(i+10, j+1);
                }
                LogUtil.e("huzedong", " lost : " + lost);
            }
        } catch (IOException e) {
            LogUtil.e("huzedong", " lost exception");
            e.printStackTrace();
        }
        if(null != lost && !lost.equals("")){
            lost = lost.replace("%", "");
        }
        return lost;
    }

    /**
     * 该方法会在子线程运行
     */
    public void loadDataAfterRequestDynamicPermissions(Context context) {
        if (!JJBoostApplication.application.mIsLoaded) {
            JJBoostApplication.application.mIsLoaded = true;
            CommonUtil.obtainCurrentProcessInfo(context, JJBoostMainActivity.CURRENT_PKG);
            List<Object> list = CommonUtil.obtainCurrentProcessInfo(context, JJBoostMainActivity.CURRENT_PKG);
            if (list != null) {
                JJBoostDetectBiz.sList = list;
                mTotalNetworkSpeed = (long) list.get(1);
            }
        } else {
            List<Object> list = CommonUtil.obtainCurrentProcessInfo(context, JJBoostMainActivity.CURRENT_PKG);
            if (list != null) {
                JJBoostDetectBiz.sList = list;
                mTotalNetworkSpeed = (long) list.get(1);
            }
        }
    }

    @Override
    public boolean getBoostStart() {
        boolean startgame = getPrefBoolean(JJBoostApplication.application, KEY_STARTGAME, false);
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
