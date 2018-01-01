package com.jj.game.boost.logtools;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.view.AbstractActivity;

public class LogSettingActivity extends AbstractActivity{
    private LogSettingActivity mContext;
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
        EditText editText1_inter = (EditText)findViewById(R.id.contact);
        EditText editText2_ip = (EditText)findViewById(R.id.contact1);
        EditText editText3_port = (EditText)findViewById(R.id.contact2);
        Button btn2 = (Button)findViewById(R.id.btn2);

        try {
            String ip = JJBoostApplication.application.getmBindService2().getIP();
            String inter = JJBoostApplication.application.getmBindService2().getTime();
            String port = JJBoostApplication.application.getmBindService2().getPort();
            if(ip != null && !ip.equals("")){
                editText2_ip.setText(ip);
            }
            if(inter != null && !inter.equals("")){
                editText1_inter.setText(inter);
            }
            if(port != null && !port.equals("")){
                editText3_port.setText(port);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(editText2_ip.getText().toString() != null && !editText2_ip.getText().toString().equals("")){
                        JJBoostApplication.application.getmBindService2().saveIP(editText2_ip.getText().toString());
                    }
                    if(editText1_inter.getText().toString() != null && !editText1_inter.getText().toString().equals("")){
                        JJBoostApplication.application.getmBindService2().saveTime(editText1_inter.getText().toString());
                    }
                    if(editText3_port.getText().toString() != null && !editText3_port.getText().toString().equals("")){
                        JJBoostApplication.application.getmBindService2().savePort(editText3_port.getText().toString());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
}
