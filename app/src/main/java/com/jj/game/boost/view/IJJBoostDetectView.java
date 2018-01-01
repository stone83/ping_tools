package com.jj.game.boost.view;

/**
 * Created by huzd on 2017/7/5.
 */

public interface IJJBoostDetectView {
    void showCancle();
    void hideCancle();
    void updateCurNet(String test, String name, int level, boolean btn_enable);
    void updateCurRun(String test, String name, int level);
}
