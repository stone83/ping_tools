package com.jj.game.boost.view;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ccmt.library.util.ViewUtil;
import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.adapter.ProcessColumAdapter;
import com.jj.game.boost.customview.RecycleViewDivider;
import com.jj.game.boost.customview.UnderLineLinearLayout;
import com.jj.game.boost.domain.ProcessInfo;
import com.jj.game.boost.presenter.JJBoostDetectPresenter;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ThreadManager;
import com.zhy.android.percent.support.PercentLinearLayout;

import java.util.List;

public class JJBoostDetectActivity extends AbstractActivity implements IJJBoostDetectView{
    private JJBoostDetectActivity mContext;
    private UnderLineLinearLayout mUnderLineLinearLayout;
    private JJBoostDetectPresenter presenter = new JJBoostDetectPresenter(this);
    public RecyclerView mRecyclerView;
    private boolean isonPause = false;
    private boolean mIsShouldStartLineAnimation;
    private ProcessColumAdapter mAdapter;
    private Intent mIntent;
    private String mPackageName;
    private int mPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        // 加载布局在onCreate()方法里调用,不需要每次界面切换后又重新加载布局,提升性能.
        initView();
        presenter.release();
        presenter.updateCurNet();
        presenter.updateCurRun();
    }

    @Override
    protected int obtainLayoutResID() {
        return R.layout.jjboost_detect_activity2;
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.activity_detect_name);
    }

    @Override
    protected boolean getActivityHasBack() {
        return false;
    }

    @Override
    protected boolean getActivityHasSetting() {
        return false;
    }

    @Override
    protected void loadData() {
        super.loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isonPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isonPause){
            loadViewData();
        }
        isonPause = false;
        if (mPosition != -1) {
            ThreadManager.executeAsyncTask(() -> {
                boolean checkPackageNameExists = isPackageNameRunning(mPackageName);
                LogUtil.i("checkPackageNameExists -> " + checkPackageNameExists);
                runOnUiThread(() -> {
                    if (!checkPackageNameExists) {
                        mAdapter.remove(mPosition);
                        mAdapter.notifyItemRemoved(mPosition);
                    }
                    mPackageName = null;
                    mPosition = -1;
                });
            });
        }
    }

    private void initView(){
        mUnderLineLinearLayout = (UnderLineLinearLayout)
                findViewById(R.id.underline_layout);
        mUnderLineLinearLayout.setHandler(myHandler);
        TextView btn = (TextView) findViewById(R.id.cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(JJBoostMainActivity.class);
            }
        });
    }
    private void startAccelerate(){
        if (presenter.isInstallApp() && presenter.getBoostStart()){
            JJBoostApplication.application.mHandlerAccelerate.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.makeMainActivity(new ComponentName("cn.jj", "cn.jj.mobile.lobby.view.Main")));
                    startActivity(intent);
                }
            }, 1000);
        }
    }
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int witch = msg.arg1;
            if(witch == 1){
                presenter.showCancle();
            } else if(witch == 2){
                presenter.hideCancle();
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       startActivity(JJBoostMainActivity.class);
                    }
                }, 1000);
            }
            if(presenter.getBoostStart()){
                startAccelerate();
            }
        }
    };

    public void initRecycler(List<ProcessInfo> list) {
        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.VERTICAL,
                    R.drawable.recyclerview_divider));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        ViewUtil.setVisibility(mRecyclerView, View.VISIBLE);

        if (mAdapter == null) {
            mAdapter = new ProcessColumAdapter(mContext, list);
            setOnItemClickListener(list);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setList(list);
            setOnItemClickListener(list);
            mAdapter.notifyDataSetChanged();
        }

//        TrafficService.sAdapter = mAdapter;
    }

    private void setOnItemClickListener(List<ProcessInfo> list) {
        if (mIntent == null) {
            mIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            mIntent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.applications.InstalledAppDetails"));
        }
        mAdapter.setOnItemClickListener((view, pos) -> {
            if (mPosition != -1) {
                LogUtil.i("上次被停止的应用还没有从列表中删除");
                return;
            }
            mPackageName = list.get(pos).getPackageName();
            mPosition = pos;
            mIntent.setData(Uri.parse("package:" + mPackageName));
            startActivity(mIntent);
        });
    }

    private void loadViewData(){
        presenter.updateCurNet();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeMessages(1);
        myHandler.removeMessages(2);
        myHandler = null;
    }

    @Override
    public void showCancle() {
        PercentLinearLayout layout = (PercentLinearLayout)findViewById(R.id.btn_cancel);
        layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideCancle() {
        PercentLinearLayout layout = (PercentLinearLayout)findViewById(R.id.btn_cancel);
        layout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void updateCurNet(String test, String name, int level, boolean btn_enable) {
        TextView name_test = (TextView)findViewById(R.id.lable_test);
        name_test.setText(test);
        ImageView name_level = (ImageView)findViewById(R.id.img_level);
        name_level.setImageResource(level);
        Button btn = (Button)findViewById(R.id.lable_shutdown);
//        btn.setVisibility(btn_enable ? View.VISIBLE:View.INVISIBLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onClick_wifi();
            }
        });
        mUnderLineLinearLayout.setWifi_level(level);
        mUnderLineLinearLayout.setNet_wifiName(name);
        JJBoostApplication.application.mHandlerAccelerate.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsShouldStartLineAnimation) {
                    mIsShouldStartLineAnimation = false;
                    List<ProcessInfo> excessProcessInfos = presenter.detectBiz.mExcessProcessInfos;
                    mUnderLineLinearLayout.startLineAnimation(0, R.id.img_icon, presenter.detectBiz.mIsShowCurRun,
                            excessProcessInfos == null ? 0 : excessProcessInfos.size());
                } else {
                    JJBoostApplication.application.mHandlerAccelerate.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void updateCurRun(String test, String name, int level) {
        if (!presenter.detectBiz.mIsShowCurRun) {
            ViewUtil.setVisibility(mUnderLineLinearLayout.findViewById(R.id.mVgText), View.GONE);
            TextView tvNetworkApp = (TextView) mUnderLineLinearLayout.findViewById(R.id.mTvNetworkApp);
            ViewUtil.setVisibility(tvNetworkApp, View.VISIBLE);
            ViewUtil.setVisibility(mUnderLineLinearLayout.findViewById(R.id.mRightView), View.GONE);
            tvNetworkApp.setText(String.format(getResources()
                    .getString(R.string.activity_detect_network_app), presenter.detectBiz.mExcessProcessInfos.size()));
            initRecycler(presenter.detectBiz.mExcessProcessInfos);
            mUnderLineLinearLayout.setHasRecycler(true);
        } else {
            ViewUtil.setVisibility(mUnderLineLinearLayout.findViewById(R.id.mTvNetworkApp), View.GONE);
            ViewUtil.setVisibility(mUnderLineLinearLayout.findViewById(R.id.mVgText), View.VISIBLE);
            if (mRecyclerView != null) {
                ViewUtil.setVisibility(mRecyclerView, View.GONE);
            } else {
                ViewUtil.setVisibility(mUnderLineLinearLayout.findViewById(R.id.recycler), View.GONE);
            }
        }
        mIsShouldStartLineAnimation = true;
    }

    public boolean isPackageNameRunning(String packageName) {
        return presenter.isPackageNameRunning(this, packageName);
    }
}
