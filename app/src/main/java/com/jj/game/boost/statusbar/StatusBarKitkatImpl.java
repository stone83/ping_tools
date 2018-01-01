package com.jj.game.boost.statusbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.jj.game.boost.R;
import com.jj.game.boost.utils.DimenUtils;

class StatusBarKitkatImpl implements IStatusBar {

    public void setStatusBarColor(Window window, int color, boolean lightStatusBar) {
        int flags = 0;
        flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        window.addFlags(flags);

        ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
        View statusBarView = new View(window.getContext());
        int statusBarHeight = getStatusBarHeight(window.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
        params.gravity = Gravity.TOP;
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(color);
        decorViewGroup.addView(statusBarView);
        StatusBarCompat.setFitsSystemWindows(window, true);

        StatusBarCompatFlavorRom.setLightStatusBar(window, lightStatusBar);

        setForNavigationBar((Activity) window.getContext());
    }

    /**
     * 适配虚拟按键
     *
     * @param context
     * @return
     */
    @SuppressWarnings("JavaDoc")
    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    private static void setForNavigationBar(Activity activity) {
        int navHeight = DimenUtils.getNavigationHeight(activity);
        Log.i("MyLog", "navHeight -> " + navHeight);
        if (navHeight > 0) {
            FrameLayout content = ((FrameLayout) activity.findViewById(android.R.id.content));
//            int childCount = content.getChildCount();
//            FrameLayout.LayoutParams layoutParams;
//            for (int i = 0; i < childCount; i++) {
//                View childView = content.getChildAt(i);
//
////                childView.setPadding(0, 0, 0, navHeight);
//                layoutParams = (FrameLayout.LayoutParams) childView.getLayoutParams();
//                layoutParams.bottomMargin = navHeight;
//            }

            FrameLayout.LayoutParams navBarLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    navHeight, Gravity.BOTTOM);
            View navBar = new View(activity);
            navBar.setBackgroundColor(activity.getResources().getColor(R.color.navigation_bar_color));
            content.addView(navBar, navBarLayoutParams);
        }
    }

}
