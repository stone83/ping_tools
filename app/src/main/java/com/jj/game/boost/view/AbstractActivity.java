package com.jj.game.boost.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.logtools.LogSettingActivity;
import com.jj.game.boost.statusbar.StatusBarCompat;
import com.jj.game.boost.utils.CommonUtil;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ObjectUtil;
import com.jj.game.boost.customview.TitleView;
import com.umeng.analytics.MobclickAgent;

public abstract class AbstractActivity extends FragmentActivity implements TitleView.OnTitleClickListener {

    protected FragmentManager mFragmentManager;
    protected Resources mResources;
    public TitleView mTitleLayout;
    protected boolean mIsLoadData;

    @SuppressWarnings({"deprecation", "StatementWithEmptyBody"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.i(getClass().getName() + " onCreate()");

        MobclickAgent.setDebugMode(true);
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.setCatchUncaughtExceptions(true);

        mResources = getResources();

        // 完全透明的Activity,可以点击后面的Activity的控件.必须在setContentView()方法之前调用,
        // 否则沉浸式状态栏不生效.也可以在setTheme()方法之后调用.
        int customStyleResourceId = getCustomStyleResourceId();
        if (this instanceof ProgressbarActivity) {
            Window window = getWindow();
            if (customStyleResourceId == R.style.custom_not_touch_modal_activity) {
                WindowManager.LayoutParams lp = window.getAttributes();

                // 可以点击后面的窗体,点击返回键和home键不生效.
//                lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

                // 可以点击后面的窗体,点击返回键和home键生效.
                lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                lp.type = WindowManager.LayoutParams.TYPE_PHONE;

//                        lp.format = PixelFormat.TRANSPARENT;
//                        lp.alpha = 0.6F;

                window.setAttributes(lp);
            } else if (customStyleResourceId == R.style.custom_progressbar_activity) {
                // 转圈对话框界面全屏
//                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }

        if (customStyleResourceId > 0) {
            setTheme(customStyleResourceId);
        }

        int layoutResID = obtainLayoutResID();
        if (layoutResID > 0) {
            setContentView(layoutResID);
            StatusBarCompat.setStatusBarColor(this, mResources.getColor(R.color.title_bg), true);
        }

//        JJBoostApplication.application.addActivity(this);

        mFragmentManager = getSupportFragmentManager();

//        initTitle();
    }

    protected abstract int obtainLayoutResID();

    protected abstract String getActivityTitle();

    protected abstract boolean getActivityHasBack();

    protected abstract boolean getActivityHasSetting();

    protected boolean isShowTitle() {
        return true;
    }

    protected int getCustomStyleResourceId() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        JJBoostApplication.application.removeActivity(this);

        if (Build.VERSION.SDK_INT >= 16) {
            JJBoostApplication.getRefWatcher().watch(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ObjectUtil.obtainDynamicPermissionManager().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    public void initTitle() {
        LogUtil.i("initTitle()");
        if (!isShowTitle()) {
            return;
        }
//        if (mTitleLayout == null) {
//            mTitleLayout = (TitleView) findViewById(R.id.title_root_layout);
//        }
        if (mTitleLayout == null) {
            return;
        }
        mTitleLayout.setOnTitleClickListener(this);
        mTitleLayout.setTitle(getActivityTitle());
        if (!getActivityHasBack()) {
            mTitleLayout.setBackBtnVisibility(View.GONE);
        }
        mTitleLayout.setSettingBtnResource(R.drawable.settings);
        if (!getActivityHasSetting()) {
            mTitleLayout.setSettingBtnVisibility(View.GONE);
        }
    }

    @SuppressWarnings("unused")
    public void setActivityTitle(final String title) {
        if (mTitleLayout != null) {
            mTitleLayout.setTitle(title);
        }
    }

    @Override
    public void onTitleBack() {
        if (getActivityHasBack()) {
            onBackPressed();
        }
    }

    @Override
    public void onTitleSetting() {
       startActivity(LogSettingActivity.class);
    }

    /**
     * 向服务端调接口或本地操作获取数据
     */
    @SuppressWarnings("unused")
    protected void loadData() {
        if (CommonUtil.isOnMainThread()) {
            mIsLoadData = true;
        } else {
            runOnUiThread(() -> mIsLoadData = true);
        }
    }

    @SuppressWarnings("unused")
    protected void setSettingBtnResource(int resid) {
        mTitleLayout.setSettingBtnResource(resid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
        MobclickAgent.onPause(this);
    }

    @SuppressWarnings("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
        MobclickAgent.onResume(this);
        LogUtil.i(getClass().getName() + " onResume()");
        LogUtil.i("mIsLoadData -> " + mIsLoadData);

        // 如果是以Dialog形式,就不能有这个判断语句.
//        if (mIsLoadData) {
//            // 加载数据时弹出转圈对话框,数据加载完,对话框关闭,会再调用onResume()方法,如果是这种情况就直接返回.
//            mIsLoadData = false;
//            return;
//        }
    }

    protected void onStop() {
        super.onStop();
    }

    protected void startActivity(Class<? extends Activity> cla) {
        Intent intent = new Intent();
        intent.setClass(this, cla);
        startActivity(intent);
    }

}
