package com.jj.game.boost.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.jj.game.boost.R;
import com.jj.game.boost.presenter.JJBoostSettingPresenter;
import com.zhy.android.percent.support.PercentLinearLayout;

public class JJBoostSettingActivity extends AbstractActivity implements IJJBoostSettingView{
    private JJBoostSettingActivity mContext;
    private JJBoostSettingPresenter presenter = new JJBoostSettingPresenter(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        load();
        initView();
    }
    private void load(){

    }
    private void initView(){
        PercentLinearLayout layout = (PercentLinearLayout)findViewById(R.id.feedback_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.toFeedBackView();
            }
        });
        Switch switch_start = (Switch)findViewById(R.id.start_swtich);
        switch_start.setChecked(presenter.getBoostStart());
        switch_start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.setBoostStart(isChecked);
            }
        });
        Switch switch_window = (Switch)findViewById(R.id.float_swtich);
        switch_window.setChecked(presenter.getBoostFloatWindow());
        switch_window.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.setBoostFloatWindow(isChecked);
            }
        });
        PercentLinearLayout site = (PercentLinearLayout)findViewById(R.id.site_layout);
        site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.jj.cn/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

    }
    @Override
    protected int obtainLayoutResID() {
        return R.layout.jjboost_setting_activity;
    }

    @Override
    protected String getActivityTitle() {
        return getResources().getString(R.string.about);
    }

    @Override
    protected boolean getActivityHasBack() {
        return true;
    }

    @Override
    protected boolean getActivityHasSetting() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void loadData() {
        super.loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void toFeedBackView() {
       startActivity(JJBoostFeedBackActivity.class);
    }
}
