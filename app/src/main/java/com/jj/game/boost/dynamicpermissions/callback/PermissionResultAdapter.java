package com.jj.game.boost.dynamicpermissions.callback;

import com.jj.game.boost.dynamicpermissions.DynamicPermissionManager;
import com.jj.game.boost.utils.LogUtil;

import java.util.Arrays;

/**
 * 支持任意重写方法,而无需重写所有的方法
 */
public abstract class PermissionResultAdapter implements PermissionResultCallBack {

    @Override
    public void onPermissionDialogShow() {
        LogUtil.i("onPermissionDialogShow()");
    }

    @Override
    public void onHasPermissionDenied() {
        LogUtil.i("onHasPermissionDenied()");
    }

    @Override
    public void onPermissionGranted() {
        LogUtil.i("onPermissionGranted()");
        if (DynamicPermissionManager.sIsHasPermissionsDenyedAtCheck != null
                && DynamicPermissionManager.sIsHasPermissionsDenyedAtCheck) {
            DynamicPermissionManager.sIsHasPermissionsDenyedAtCheck = null;
            DynamicPermissionManager.sIsShouldGoToAppSetting = null;
        }
    }

    @Override
    public void onPermissionGranted(String... permissions) {
        LogUtil.i("onPermissionGranted()");
        LogUtil.i("Arrays.toString(permissions) -> " + Arrays.toString(permissions));
    }

    @Override
    public void onPermissionDenied(String... permissions) {
        LogUtil.i("onPermissionDenied()");
        LogUtil.i("Arrays.toString(permissions) -> " + Arrays.toString(permissions));
        DynamicPermissionManager.sIsShouldGoToAppSetting = null;
    }

    @Override
    public void onRationalShow(String... permissions) {
        LogUtil.i("onRationalShow()");
        LogUtil.i("Arrays.toString(permissions) -> " + Arrays.toString(permissions));
//        if (DynamicPermissionManager.sType != null
//                && DynamicPermissionManager.sType == DynamicPermissionManager.TYPE_NOT_ACTIVITY) {
//            DynamicPermissionManager.sIsShouldGoToAppSetting = true;
//        } else {
//            DynamicPermissionManager.sIsShouldGoToAppSetting = null;
//        }
        DynamicPermissionManager.sIsShouldGoToAppSetting = null;
    }

}
