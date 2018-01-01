package com.jj.game.boost.view;

import android.os.Bundle;
import android.widget.TextView;

import com.jj.game.boost.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class JJBoostErroActivity extends AbstractActivity{
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         initView();
    }
    private void initView(){
        TextView txt = (TextView)findViewById(R.id.erro);
    }
    @Override
    protected int obtainLayoutResID() {
        return R.layout.jjboost_erro_activity;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getSpeedsValue(Double valuetemp){

    }

    @Override
    protected String getActivityTitle() {
        return getResources().getString(R.string.jj_accelera);
    }

    @Override
    protected boolean getActivityHasBack() {
        return true;
    }

    @Override
    protected boolean getActivityHasSetting() {
        return false;
    }

    /**
     * 申请动态权限成功后调用该方法,该方法会在子线程运行.
     */
    @Override
    protected void loadData() {
        super.loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
