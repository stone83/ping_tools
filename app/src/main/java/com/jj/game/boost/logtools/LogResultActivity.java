package com.jj.game.boost.logtools;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.utils.PreferenceUtils;
import com.jj.game.boost.view.AbstractActivity;

public class LogResultActivity extends AbstractActivity {
//    private LogService logService = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
        initView();
    }
    private boolean isStart = true;
    private void initView(){
        gotoAnotherChart();
        Button bind = (Button)findViewById(R.id.bind);
        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chartFragmentMoblie.refreshChart();
            }
        });

        Button unbind = (Button)findViewById(R.id.unbind);
        isStart = PreferenceUtils.getPrefBoolean(getApplicationContext(), "isStart", true);
        if(isStart){
            unbind.setText("停止记录PING值");
        } else {
            unbind.setText("开始记录PING值");
        }
        unbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = PreferenceUtils.getPrefBoolean(getApplicationContext(), "isStart", true);
                if(isStart){
                    JJBoostApplication.application.excuteUnbindService();
                    unbind.setText("开始记录PING值");
                    PreferenceUtils.setPrefBoolean(getApplicationContext(), "isStart", false);
                } else {
                    JJBoostApplication.application.excutebindService();
                    unbind.setText("停止记录PING值");
                    PreferenceUtils.setPrefBoolean(getApplicationContext(), "isStart", true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.activity_logresult;
    }

    @Override
    protected String getActivityTitle() {
        return "ping Log 记录";
    }

    @Override
    protected boolean getActivityHasBack() {
        return true;
    }

    @Override
    protected boolean getActivityHasSetting() {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private ChartFragmentMoblie chartFragmentMoblie = new ChartFragmentMoblie();
    private void gotoAnotherChart(){
        mFragmentManager.beginTransaction().replace(R.id.container, chartFragmentMoblie).commit();
    }

    @Override
    public void onTitleBack() {
        if (getActivityHasBack()) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().removeAllStickyEvents();
//        EventBus.getDefault().unregister(this);
    }
}
