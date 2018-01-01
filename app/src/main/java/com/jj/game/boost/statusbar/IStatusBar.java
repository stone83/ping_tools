package com.jj.game.boost.statusbar;

import android.view.Window;

/**
 * @author myx
 *         by 2017-06-08
 */
interface IStatusBar {

    /**
     * 适配沉浸式状态栏
     *
     * @param window
     * @param color
     * @param lightStatusBar
     */
    @SuppressWarnings("JavaDoc")
    void setStatusBarColor(Window window, int color, boolean lightStatusBar);

}
