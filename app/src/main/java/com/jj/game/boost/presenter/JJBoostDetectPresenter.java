package com.jj.game.boost.presenter;

import android.content.Context;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.domain.ProcessInfo;
import com.jj.game.boost.modebiz.JJBoostDetectBiz;
import com.jj.game.boost.utils.CommonUtil;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ThreadManager;
import com.jj.game.boost.view.IJJBoostDetectView;
import com.jj.game.boost.view.JJBoostDetectActivity;
import com.jj.game.boost.view.JJBoostMainActivity;
import com.jj.game.boost.wifi4g.WIFI_DATA_Manager;

import java.util.List;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostDetectPresenter {
    private IJJBoostDetectView detectView;
    public JJBoostDetectBiz detectBiz;
    public JJBoostDetectPresenter(IJJBoostDetectView view){
        detectView = view;
        detectBiz = new JJBoostDetectBiz();
    }
    public boolean getBoostStart(){
        return detectBiz.getBoostStart();
    }
    public void showCancle() {
        detectView.showCancle();
    }

    public void hideCancle() {
        detectView.hideCancle();
    }
    public void updateCurNet() {
        String name = WIFI_DATA_Manager.getInstance() == null ? "已关闭" : WIFI_DATA_Manager.getInstance().getNetName();
        int level = WIFI_DATA_Manager.getInstance() == null ? 0 : WIFI_DATA_Manager.getInstance().getNetLevel();
        int ResId = 0;
        String qulality_string = "";
        boolean btn_enable = false;
        LogUtil.e("huzedong", " wifi name : " + name);
        LogUtil.e("huzedong", " wifi level : " + level);
        if(level >= -60 && level <= 0){
            ResId = 1;  //优
            qulality_string = "";
            btn_enable = false;
        } else if(level >= -90 && level < -60){
            ResId = 2; //良
            qulality_string = "";
            btn_enable = false;
        } else if(level >= -120 && level < -90){
            ResId = 3;  //差
            qulality_string = "";
            btn_enable = true;
        } else {
            ResId = 1;
            qulality_string = "";
            btn_enable = false;
        }
        detectView.updateCurNet(qulality_string, name, ResId, btn_enable);
    }

    @SuppressWarnings("unchecked")
    public void updateCurRun() {
        //        boolean isLoad = false;
        final List[] list = new List[]{JJBoostDetectBiz.sList};
        if (list[0] == null) {
            if (!JJBoostApplication.application.mIsLoaded) {
                // 主界面申请动态权限失败
                detectBiz.mIsShowCurRun = true;
                detectView.updateCurRun(null, null, 0);
                return;
            }
//            if (!isLoad) {
//                isLoad = true;
            ThreadManager.executeAsyncTask(() -> {
                JJBoostDetectActivity boostDetectActivity = (JJBoostDetectActivity) detectView;
                list[0] = CommonUtil.obtainCurrentProcessInfo(boostDetectActivity, JJBoostMainActivity.CURRENT_PKG);
                if (list[0] == null) {
                    boostDetectActivity.runOnUiThread(() -> {
                        detectBiz.mIsShowCurRun = true;
                        detectView.updateCurRun(null, null, 0);
                    });
                    return;
                }
                List<ProcessInfo> excessProcessInfos = (List<ProcessInfo>) list[0].get(2);
                if (excessProcessInfos != null && excessProcessInfos.size() > 0) {
                    boostDetectActivity.runOnUiThread(() -> {
                        detectBiz.mIsShowCurRun = false;
                        detectBiz.mExcessProcessInfos = excessProcessInfos;
                        detectView.updateCurRun(null, null, 0);
                    });
                } else {
                    boostDetectActivity.runOnUiThread(() -> {
                        detectBiz.mIsShowCurRun = true;
                        detectView.updateCurRun(null, null, 0);
                    });
                }
            });
//            }
        } else {
            List<ProcessInfo> excessProcessInfos = (List<ProcessInfo>) list[0].get(2);
            if (excessProcessInfos != null && excessProcessInfos.size() > 0) {
                detectBiz.mIsShowCurRun = false;
                detectBiz.mExcessProcessInfos = excessProcessInfos;
            } else {
                detectBiz.mIsShowCurRun = true;
            }
            detectView.updateCurRun(null, null, 0);
        }
    }
    public boolean isInstallApp(){
        return CommonUtil.isInstallApp(JJBoostApplication.application, "cn.jj");
    }
    public void onClick_wifi(){
        WIFI_DATA_Manager.getInstance().setNetState(false);
    }

    public boolean isPackageNameRunning(Context context, String packageName) {
        return detectBiz.isPackageNameRunning(context, packageName);
    }
    public void release(){
        WIFI_DATA_Manager.release();
    }
}
