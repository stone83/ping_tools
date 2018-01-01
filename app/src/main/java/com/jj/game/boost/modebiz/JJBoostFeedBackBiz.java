package com.jj.game.boost.modebiz;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.utils.LogUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostFeedBackBiz implements IJJBoostFeedBackBiz{

    @Override
    public void submitContactInfo(String feedback, String contactinfo) {
        LogUtil.e("huzedong", " feedback : " + feedback);
        LogUtil.e("huzedong", " contactinfo : " + contactinfo);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("info", feedback + " / " + contactinfo);
        MobclickAgent.onEvent(JJBoostApplication.application, "jj_01", map);
    }
}
