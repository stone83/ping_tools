package com.jj.game.boost.presenter;

import com.jj.game.boost.modebiz.IJJBoostSettingBiz;
import com.jj.game.boost.modebiz.JJBoostSettingBiz;
import com.jj.game.boost.view.IJJBoostSettingView;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostSettingPresenter {
    private IJJBoostSettingView settingView;
    private IJJBoostSettingBiz settingBiz;
    public JJBoostSettingPresenter(IJJBoostSettingView view){
        settingView = view;
        settingBiz = new JJBoostSettingBiz();
    }
    public void setBoostStart(boolean ison){
        settingBiz.setBoostStart(ison);
    }
    public void setBoostFloatWindow(boolean ison){
        settingBiz.setBoostFloatWindow(ison);
    }
    public boolean getBoostStart(){
        return settingBiz.getBoostStart();
    }
    public boolean getBoostFloatWindow(){
        return settingBiz.getBoostFloatWindow();
    }
    public void toFeedBackView(){
        settingView.toFeedBackView();
    }
}
