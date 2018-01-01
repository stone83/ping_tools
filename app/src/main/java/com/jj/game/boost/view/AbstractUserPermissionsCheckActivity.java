package com.jj.game.boost.view;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.ccmt.library.lru.LruMap;
import com.ccmt.library.util.ViewUtil;
import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.R;
import com.jj.game.boost.customview.CustomAlertDialog;
import com.jj.game.boost.dynamicpermissions.DynamicPermissionManager;
import com.jj.game.boost.dynamicpermissions.callback.PermissionResultAdapter;
import com.jj.game.boost.traffic.service.TrafficService;
import com.jj.game.boost.utils.DialogFractory;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ObjectUtil;
import com.jj.game.boost.utils.ThreadManager;

import java.util.Arrays;

public abstract class AbstractUserPermissionsCheckActivity extends AbstractActivity {

    /**
     * 是否让用户授权android.permission.PACKAGE_USAGE_STATS权限
     */
    public static Boolean sIsAuthorization;

    private static Boolean sIsShowPackageUsageStatsDialoged;

    protected View mShowView;
    protected View mHideView;
    public boolean mIsClickButton;
    private boolean mIsShowHasNoPackageUsageStatsPermissionsContented;
    private boolean mIsShowHasNoDynamicPermissionsContented;
    private boolean mIsOnPermissionDeniedInvoke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsAuthorization = null;
    }

    @SuppressWarnings("NewApi")
    @Override
    protected void onResume() {
        super.onResume();

//        if (this instanceof ProgressbarActivity) {
////            if (DynamicPermissionManager.sType != null
////                    && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//            doOnResume();
//            return;
////            }
//        }

        LogUtil.i("sIsAuthorization -> " + sIsAuthorization);
//        LogUtil.i("sIsShowDynamicPermissionDialoged -> " + sIsShowDynamicPermissionDialoged);
        LogUtil.i("DynamicPermissionManager.sIsShouldGoToAppSetting -> " + DynamicPermissionManager.sIsShouldGoToAppSetting);
        if (sIsAuthorization != null) {
            // 如果用户没有授权android.permission.PACKAGE_USAGE_STATS权限,也能返回到主界面.
            boolean hasPermissionToReadNetworkStats = hasPermissionToReadNetworkStats(this);
            if (!hasPermissionToReadNetworkStats) {
                showHasNoPackageUsageStatsPermissionsContent();
                doOnResume();
                return;
            }
            sIsAuthorization = null;
        }
        if (mIsShowHasNoPackageUsageStatsPermissionsContented) {
            mIsShowHasNoPackageUsageStatsPermissionsContented = false;
            mShowView = initHasPerssmissionsShowView();
            mHideView = initHasPerssmissionHideView();
            showContent();
        }

        // 重构后不需要以下代码
//        if (sIsShowDynamicPermissionDialoged != null) {
//            // 弹出授权对话框后,用户允许或拒绝都会再调用onResume()方法,如果是这种情况就直接返回.
//            sIsShowDynamicPermissionDialoged = null;
//            return;
//        }

        if (DynamicPermissionManager.sIsShouldGoToAppSetting == null) {
            // 用户拒绝授权动态权限,同时点击了不再提示,会再调用onResume()方法,就会弹系统动态权限列表界面,
            // 再点返回键到应用的当前界面时,会再弹系统动态权限列表界面,如果是这种情况就直接返回.
            DynamicPermissionManager.sIsShouldGoToAppSetting = true;
            showHasNoDynamicPermissionsContent();
            return;
        }
        LruMap lruMap = LruMap.getInstance();
        Boolean isGoToAppSetting = (Boolean) lruMap.get("isGoToAppSetting");
        if (isGoToAppSetting != null) {
            lruMap.remove("isGoToAppSetting", false);
            if (!checkPermissions(this)) {
                return;
            }
        }
        if (mIsShowHasNoDynamicPermissionsContented) {
            mIsShowHasNoDynamicPermissionsContented = false;
            mShowView = initHasPerssmissionsShowView();
            mHideView = initHasPerssmissionHideView();
            showContent();
        }

        // 动态权限用的是非Activity方式
//        if (DynamicPermissionManager.sType != null
//                && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//            DialogFractory.closeProgressDialog(this);
//        }

        // 检测android.permission.PACKAGE_USAGE_STATS权限
        if (!hasPermissionToReadNetworkStats(this)) {
//            mShowView = initHasNoPerssmissionShowView();
//            mHideView = initHasNoPerssmissionHideView();
//            showContent();

//            LogUtil.i("让用户去授权");
//            sIsAuthorization = true;
//            requestReadNetworkStats(this);

            showPackageUsageStatsDialog(this);
            return;
        }

        // 检测动态权限,暂时保留.
//        String[] dynamicPermissionses = initDynamicPermissionses();
//        if (dynamicPermissionses != null && dynamicPermissionses.length > 0) {
//            if (Build.VERSION.SDK_INT >= 23) {
//                boolean hasDynamicPermission = true;
//
//                for (String dynamicPermissionse : dynamicPermissionses) {
//                    hasDynamicPermission = ContextCompat.checkSelfPermission(this,
//                            dynamicPermissionse) == PackageManager.PERMISSION_GRANTED;
//                    if (!hasDynamicPermission) {
//                        hasDynamicPermission = false;
//                        break;
//                    }
//                }
//                LogUtil.i("hasDynamicPermission -> " + hasDynamicPermission);
////                hasDynamicPermission = NewTrafficManager.hasPermission(this, AppOpsManager.OPSTR_GET_USAGE_STATS);
////                LogUtil.i("hasDynamicPermission -> " + hasDynamicPermission);
//
//                if (!hasDynamicPermission) {
//                    mShowView = initHasNoPerssmissionShowView();
//                    mHideView = initHasNoPerssmissionHideView();
//                    showContent();
//                    return;
//                }
//            }
//        }

        // Activity方式
        requestDynamicPermissions(false);
    }

    @SuppressWarnings("NewApi")
    public static void showPackageUsageStatsDialog(AbstractUserPermissionsCheckActivity userPermissionsCheckActivity) {
        if (sIsShowPackageUsageStatsDialoged != null) {
            return;
        }
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                LogUtil.i("让用户去授权");
                sIsAuthorization = true;
                requestReadNetworkStats(userPermissionsCheckActivity);
            } else {
                userPermissionsCheckActivity.showHasNoPackageUsageStatsPermissionsContent();
                userPermissionsCheckActivity.doOnResume();
            }
        };
        try {
            PackageManager packageManager = userPermissionsCheckActivity.getPackageManager();
            CharSequence label = packageManager.getApplicationInfo(userPermissionsCheckActivity.getPackageName(),
                    0).loadLabel(packageManager);
            CustomAlertDialog dialog = new CustomAlertDialog.Builder(userPermissionsCheckActivity)
                    .setTitle(userPermissionsCheckActivity.getString(R.string.traffic_single_package_usage_stats_title))
                    .setMessage("“" + label.toString() + "”" + userPermissionsCheckActivity.getString(R.string
                            .traffic_single_package_usage_stats_message))
                    .setPositiveButton(R.string.traffic_single_allow, onClickListener)
                    .setNegativeButton(R.string.traffic_single_deny, onClickListener)
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false)
                    .setOnDismissListener(dialog1 -> sIsShowPackageUsageStatsDialoged = null)
                    .create();
            dialog.setOnShowListener(dialog12 -> sIsShowPackageUsageStatsDialoged = true);
            dialog.show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasPermission(Context context, String perssmissionsName) {
        if (Build.VERSION.SDK_INT < 19) {
            return true;
        }
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(perssmissionsName,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @RequiresApi(api = 21)
    public static boolean hasPermissionToReadNetworkStats(Context context) {
        return hasPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS);
    }

    /**
     * 打开有权查看使用情况的应用页面
     */
    @RequiresApi(api = 21)
    public static void requestReadNetworkStats(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    protected void requestDynamicPermissions(boolean isClickButton) {
//        ITrafficManager trafficManager = TrafficManagerFactory.createTrafficManager(this);
//        LogUtil.i("mTrafficManager -> " + mTrafficManager);
//        if (mTrafficManager == null) {
//            return;
//        }

        mIsClickButton = isClickButton;

        boolean showDialog = isShowDialog();
        boolean doOnResumeAfterRequestDynamicPermissions = isDoOnResumeAfterRequestDynamicPermissions();

        // Activity方式
        String[] dynamicPermissionses = initDynamicPermissionses();
        if (Build.VERSION.SDK_INT >= 23) {
            ObjectUtil.obtainDynamicPermissionManager().request(this,
                    dynamicPermissionses,
                    new PermissionResultAdapter() {
                        @Override
                        public void onPermissionGranted(String... permissions) {
                            if (Arrays.asList(dynamicPermissionses).equals(Arrays.asList(permissions))) {
                                if (showDialog) {
//                                    DialogFractory.showProgressDialog(mContext, true);
                                    DialogFractory.showFullScreenProgressDialog(AbstractUserPermissionsCheckActivity.this);
                                }
                                ThreadManager.executeAsyncTask(() -> {
                                    loadData();
                                    JJBoostApplication.application.mHandlerAccelerate.post(() -> {
                                        initRequestDynamicPermissionsFinishView();
                                        if (showDialog) {
//                                            DialogFractory.closeProgressDialog(mContext);
                                            DialogFractory.closeFullScreenProgressDialog();
                                        }
                                        if (doOnResumeAfterRequestDynamicPermissions) {
                                            doOnResume();
                                            startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                                        }
                                    });
                                });
                                if (!doOnResumeAfterRequestDynamicPermissions) {
                                    doOnResume();
                                    startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                                }
                            }
                        }

                        @Override
                        public void onPermissionDenied(String... permissions) {
                            super.onPermissionDenied(permissions);
                            mIsOnPermissionDeniedInvoke = true;
                            doOnResume();
                        }

                        @Override
                        public void onRationalShow(String... permissions) {
                            super.onRationalShow(permissions);
                            if (mIsOnPermissionDeniedInvoke) {
                                mIsOnPermissionDeniedInvoke = false;
                            } else {
                                doOnResume();
                            }
                        }
                    });
        } else {
            if (showDialog) {
//                DialogFractory.showProgressDialog(mContext, true);
                DialogFractory.showFullScreenProgressDialog(this);
            }
            ThreadManager.executeAsyncTask(() -> {
                loadData();
                JJBoostApplication.application.mHandlerAccelerate.post(() -> {
                    initRequestDynamicPermissionsFinishView();
                    if (showDialog) {
//                        DialogFractory.closeProgressDialog(mContext);
                        DialogFractory.closeFullScreenProgressDialog();
                    }
                    if (doOnResumeAfterRequestDynamicPermissions) {
                        doOnResume();
                        startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
                    }
                });
            });
            if (!doOnResumeAfterRequestDynamicPermissions) {
                doOnResume();
                startService(new Intent(AbstractUserPermissionsCheckActivity.this, TrafficService.class));
            }
        }
    }

    protected abstract void initRequestDynamicPermissionsFinishView();

    /**
     * 申请动态权限成功后,加载数据的过程中是否弹出转圈对话框.
     *
     * @return 为true代表要弹出转圈对话框.否则不弹出转圈对话框.
     */
    protected abstract boolean isShowDialog();

    /**
     * 子类是否在申请动态权限成功且在子线程运行完loadData()方法后,然后在主线程调用doOnResume()方法.
     *
     * @return 为true代表子类在申请动态权限成功且在子线程运行完loadData()方法后,
     * 然后在主线程调用doOnResume()方法.否则代表子类在申请动态权限成功后,不用等子线程运行完loadData()方法,
     * 直接在主线程调用doOnResume()方法.
     * 如果申请动态权限失败,不会在子线程调用loadData()方法,直接在主线程调用doOnResume()方法.
     */
    protected abstract boolean isDoOnResumeAfterRequestDynamicPermissions();

    protected void showContent() {
        if (mShowView != null && mHideView != null) {
            ViewUtil.setVisibility(mShowView, View.VISIBLE);
            ViewUtil.setVisibility(mHideView, View.GONE);
        }
    }

    public void showHasNoPackageUsageStatsPermissionsContent() {
        mIsShowHasNoPackageUsageStatsPermissionsContented = true;
        mShowView = initHasNoPerssmissionShowView();
        mHideView = initHasNoPerssmissionHideView();
        showContent();
    }

    public void showHasNoDynamicPermissionsContent() {
        mIsShowHasNoDynamicPermissionsContented = true;
        mShowView = initHasNoPerssmissionShowView();
        mHideView = initHasNoPerssmissionHideView();
        showContent();
    }

    protected boolean checkPermissions(Context context) {
        String[] dynamicPermissionses = initDynamicPermissionses();
        if (dynamicPermissionses == null || dynamicPermissionses.length == 0) {
            throw new RuntimeException("子类重写initDynamicPermissionses()方法返回的权限不能为空且数量必须大于0");
        }
        boolean hasDynamicPermissions = true;
        for (String dynamicPermissionse : dynamicPermissionses) {
            hasDynamicPermissions = ContextCompat.checkSelfPermission(context,
                    dynamicPermissionse) == PackageManager.PERMISSION_GRANTED;
            if (!hasDynamicPermissions) {
                break;
            }
        }
        return hasDynamicPermissions;
    }

    /**
     * 如果子类每次onResume()方法被调用时需要申请的动态权限请在子类重写该方法
     */
    protected abstract String[] initDynamicPermissionses();

    @SuppressWarnings("unused")
    protected abstract View initHasPerssmissionsShowView();

    @SuppressWarnings("unused")
    protected abstract View initHasPerssmissionHideView();

    protected abstract View initHasNoPerssmissionShowView();

    protected abstract View initHasNoPerssmissionHideView();

    protected abstract void doOnResume();

}
