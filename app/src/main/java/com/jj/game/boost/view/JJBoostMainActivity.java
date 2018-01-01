package com.jj.game.boost.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.customview.CustomAlertDialog;
import com.jj.game.boost.modebiz.JJBoostDetectBiz;
import com.jj.game.boost.presenter.JJBoostMainPresenter;
import com.jj.game.boost.traffic.service.TrafficService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class JJBoostMainActivity extends AbstractUserPermissionsCheckActivity implements IJJBoostMainView{
    public static final String[] CURRENT_PKG;
    private JJBoostMainActivity mContext;
    private JJBoostMainPresenter presenter = new JJBoostMainPresenter(this);
    private TextView mTvSpeed;
    private TextView mTvSpeedUnit;
    private CustomAlertDialog optionDialog;

    static {
        CURRENT_PKG = new String[]{
                JJBoostApplication.application.getPackageName()
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mTvSpeed = (TextView) findViewById(R.id.speed);
        mTvSpeedUnit = (TextView) findViewById(R.id.unit_speed);
    }
    private void initDialog(){
        optionDialog = new CustomAlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.no_game))
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //去下载，需要商店url等。后续添加，可能有多商店.目前先打开官网页面
                        Uri uri = Uri.parse("http://www.jj.cn/");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAccelerate();
                    }
                })
                .setOnDismissListener(dialog -> optionDialog = null)
                .create();
        optionDialog.show();
    }
    private void load(){
        presenter.updateSpeed();
        presenter.updateDelay();
        presenter.updateLost();
    }
    @Override
    public void toDetectView(){
        startActivity(JJBoostDetectActivity.class);
    }

    @Override
    public void showBoosting() {
        Button btn = (Button)findViewById(R.id.btn);
        btn.setText(getResources().getString(R.string.stop_acce));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.stopBoosting();
                initView();
            }
        });
        TextView txt = (TextView)findViewById(R.id.text);
        txt.setText(getResources().getString(R.string.jj_time));
        TextView time = (TextView)findViewById(R.id.time);
        time.setVisibility(View.VISIBLE);
    }

    private void startAccelerate(){
        presenter.updateUseTime();
        presenter.toDetectView();
//        if (presenter.isInstallApp() && presenter.getBoostStart()){
//            JJBoostApplication.application.mHandlerAccelerate.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Intent intent = new Intent(Intent.makeMainActivity(new ComponentName("cn.jj", "cn.jj.mobile.lobby.view.Main")));
//                    startActivity(intent);
//                }
//            }, 1500);
//        }
    }

    @Override
    public void initView(){
        Button btn = (Button)findViewById(R.id.btn);
        btn.setText(getResources().getString(R.string.start_accelerate));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(presenter.hasWifiOrData()){
                    if(presenter.isInstallApp()){
                        startAccelerate();
                    } else {
                        if(presenter.getBoostStart()){
                            initDialog();
                        } else {
                            startAccelerate();
                        }
                    }
                } else {
                    startActivity(JJBoostErroActivity.class);
                }
            }
        });
        TextView txt = (TextView)findViewById(R.id.text);
        txt.setText(getResources().getString(R.string.jj_game));
        TextView time = (TextView)findViewById(R.id.time);
        time.setVisibility(View.INVISIBLE);
    }

    @Override
    public void update_time(String time) {
        TextView txt = (TextView)findViewById(R.id.time);
        txt.setText(time);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void update_cur_speed(String speed) {
        if ("-1".equals(speed)) {
            mTvSpeed.setText("--");
            mTvSpeedUnit.setText("B/s");
        } else if ("0".equals(speed)) {
            mTvSpeed.setText("0");
            mTvSpeedUnit.setText("B/s");
        } else {
            long speedValue = Long.parseLong(speed);
            speed = Formatter.formatShortFileSize(mContext, speedValue);
            String[] strs = speed.split(" ");
            mTvSpeed.setText(strs[0]);
            mTvSpeedUnit.setText(strs[1] + "/s");
        }
        TrafficService.sTvSpeed = mTvSpeed;
        TrafficService.sTvSpeedUnit = mTvSpeedUnit;
    }

    @Override
    public void update_cur_delay(String delay) {
        TextView txt = (TextView)findViewById(R.id.delay);
        txt.setText(delay);
    }

    @Override
    public void update_cur_lost(String lost) {
        TextView txt = (TextView)findViewById(R.id.lose);
        txt.setText(lost);
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.jjboost_main_activity;
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
        return false;
    }

    @Override
    protected boolean getActivityHasSetting() {
        return true;
    }

    /**
     * 申请动态权限成功后调用该方法,该方法会在子线程运行.
     */
    @Override
    protected void loadData() {
        super.loadData();
        presenter.loadDataAfterRequestDynamicPermissions(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
        stopService(new Intent(mContext, TrafficService.class));
    }

    @Override
    protected void doOnResume() {
        presenter.onResume();
        load();
    }

    @Override
    protected void initRequestDynamicPermissionsFinishView() {

    }

    @Override
    protected boolean isShowDialog() {
        return false;
    }

    @Override
    protected boolean isDoOnResumeAfterRequestDynamicPermissions() {
        return false;
    }

    @Override
    protected String[] initDynamicPermissionses() {
        return new String[]{Manifest.permission.READ_PHONE_STATE};
    }

    @Override
    protected View initHasPerssmissionsShowView() {
        return null;
    }

    @Override
    protected View initHasPerssmissionHideView() {
        return null;
    }

    @Override
    protected View initHasNoPerssmissionShowView() {
        return null;
    }

    @Override
    protected View initHasNoPerssmissionHideView() {
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.releaseTimer();
        presenter.onDestory();
        JJBoostDetectBiz.sList = null;
    }
}
