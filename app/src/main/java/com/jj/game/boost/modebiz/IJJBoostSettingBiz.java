package com.jj.game.boost.modebiz;

/**
 * Created by huzd on 2017/7/4.
 */

public interface IJJBoostSettingBiz {
    void setBoostStart(boolean ison);
    void setBoostFloatWindow(boolean ison);
    boolean getBoostStart();
    boolean getBoostFloatWindow();
}
