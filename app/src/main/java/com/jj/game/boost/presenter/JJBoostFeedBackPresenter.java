package com.jj.game.boost.presenter;

import com.jj.game.boost.modebiz.IJJBoostFeedBackBiz;
import com.jj.game.boost.modebiz.JJBoostFeedBackBiz;
import com.jj.game.boost.view.IJJBoostFeedBackView;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostFeedBackPresenter {
    private IJJBoostFeedBackView feedBackView;
    private IJJBoostFeedBackBiz feedBackBiz;
    public JJBoostFeedBackPresenter(IJJBoostFeedBackView view){
        feedBackView = view;
        feedBackBiz = new JJBoostFeedBackBiz();
    }
    public void submitInfo(){
        feedBackBiz.submitContactInfo(feedBackView.getFeedBack(), feedBackView.getContactInfo());
    }
}
