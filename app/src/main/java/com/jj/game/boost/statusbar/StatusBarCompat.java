package com.jj.game.boost.statusbar;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author myx
 *         by 2017-06-08
 */
public class StatusBarCompat {

    private static final IStatusBar IMPL;

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            if ("huawei p7-l07".equals(Build.MODEL.toLowerCase())) {
                // 适配HUAWEI P7-L07手机的状态栏和导航栏
                IMPL = new StatusBarKitkatImpl();
            } else {
                IMPL = new StatusBarMImpl();
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            IMPL = new StatusBarKitkatImpl();
        } else {
            // 适配sdk小于19的手机也可能有虚拟按键的情况
//            IMPL = (window, color, lightStatusBar) -> {
//
//            };
            IMPL = new StatusBarKitkatImpl();
        }
    }

    public static void setStatusBarColor(Activity activity, int color, boolean lightStatusBar) {
        Window window = activity.getWindow();
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) > 0) {
            return;
        }
        IMPL.setStatusBarColor(window, color, lightStatusBar);
    }

    static void setFitsSystemWindows(Window window, boolean fitSystemWindows) {
        ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置ContentView的FitsSystemWindows,而是设置ContentView的第1个子View.预留出系统View的空间.
            mChildView.setFitsSystemWindows(fitSystemWindows);
        }
    }

}
