package com.jj.game.boost.statusbar;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.jj.game.boost.R;

/**
 * @author myx
 *         by 2017-06-08
 */
class StatusBarMImpl implements IStatusBar {

    @SuppressWarnings("deprecation")
    public void setStatusBarColor(Window window, int color, boolean lightStatusBar) {
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(color);
        //设置导航栏颜色
        window.setNavigationBarColor(window.getContext().getResources().getColor(R.color.navigation_bar_color));

        // 设置浅色状态栏时的界面显示
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (lightStatusBar) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);

        // 去掉系统状态栏下的windowContentOverlay
        View v = window.findViewById(Window.ID_ANDROID_CONTENT);
        if (v != null) {
            try {
                v.setForeground(null);
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

}
