package com.jj.game.boost.view;

/**
 * Created by huzd on 2017/7/4.
 */

public interface IJJBoostMainView {
    void update_cur_speed(String speed);
    void update_cur_delay(String delay);
    void update_cur_lost(String lost);
    void toDetectView();
    void showBoosting();
    void initView();
    void update_time(String time);
}
