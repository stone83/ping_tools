package com.jj.game.boost.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jj.game.boost.R;
import com.jj.game.boost.presenter.JJBoostFeedBackPresenter;

public class JJBoostFeedBackActivity extends AbstractActivity implements IJJBoostFeedBackView{
    private JJBoostFeedBackActivity mContext;
    private JJBoostFeedBackPresenter presenter = new JJBoostFeedBackPresenter(this);
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
        Button btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.submitInfo();
            }
        });
    }
    @Override
    protected int obtainLayoutResID() {
        return R.layout.jjboost_feedback_activity;
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
    public String getFeedBack() {
//        EditText editText = (EditText)findViewById(R.id.edit);
        return null;
    }

    @Override
    public String getContactInfo() {
        EditText editText = (EditText)findViewById(R.id.contact);
        return editText.getText().toString();
    }

}
