package com.jj.game.boost.presenter;

import android.content.Context;

import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.modebiz.IJJBoostMainBiz;
import com.jj.game.boost.modebiz.JJBoostMainBiz;
import com.jj.game.boost.utils.CommonUtil;
import com.jj.game.boost.utils.ThreadManager;
import com.jj.game.boost.view.IJJBoostMainView;
import com.jj.game.boost.wifi4g.WIFI_DATA_Manager;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by huzd on 2017/7/4.
 */

public class JJBoostMainPresenter {
    private IJJBoostMainView mainView;
    private IJJBoostMainBiz mainBiz;
    private boolean isBoosting = false;
    private int time = 0;
    private List<SoftReference<Disposable>> sr_delay_lost = new ArrayList<SoftReference<Disposable>>();
    private List<SoftReference<Disposable>> sr_time = new ArrayList<SoftReference<Disposable>>();

    public JJBoostMainPresenter(IJJBoostMainView view){
        mainView = view;
        mainBiz = new JJBoostMainBiz();
    }

    public IJJBoostMainBiz getMainBiz() {
        return mainBiz;
    }

    public void stopBoosting(){
        setIsBoosting(false);
        releaseTimer();
    }

    public void updateSpeed() {
//        ThreadManager.executeAsyncTask(new Runnable() {
//            @Override
//            public void run() {
//                mainBiz.getCurSpeed();
//                //biz获取之后更新速度
//                ThreadManager.post(ThreadManager.THREAD_UI, new Runnable(){
//                    @Override
//                    public void run() {
//                        mainView.update_cur_speed("0");
//                    }
//                });
//            }
//        });
        if(null != mainView){
            mainView.update_cur_speed(mainBiz.getCurSpeed());
        }
    }

    public void updateDelay(){
        Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                sr_delay_lost.add(new SoftReference<Disposable>(d));
            }

            @Override
            public void onNext(Long value) {
                String delay = mainBiz.getCurDelay();
                //biz获取之后更新延迟
                ThreadManager.post(ThreadManager.THREAD_UI, new Runnable(){
                    @Override
                    public void run() {
                        if(null != mainView){
                            mainView.update_cur_delay(delay);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void updateLost(){
        Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                sr_delay_lost.add(new SoftReference<Disposable>(d));
            }

            @Override
            public void onNext(Long value) {
                String lost = mainBiz.getCurLost();
                //biz获取之后更新界面
                ThreadManager.post(ThreadManager.THREAD_UI, new Runnable(){
                    @Override
                    public void run() {
                        if(null != mainView){
                            mainView.update_cur_lost(lost);
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void updateUseTime(){
        Observable.interval(0, 1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        sr_time.add(new SoftReference<Disposable>(d));
                    }

                    @Override
                    public void onNext(Long value) {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(time);
                        time = time + 1000;
                        //每隔1秒钟更新界面计时
                        mainView.update_time(sdf.format(calendar.getTime()));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void releaseTimer(){
        time = 0;
        for(int i = 0; i < sr_time.size(); i++){
            SoftReference<Disposable> soft = sr_time.get(i);
            if(null != soft && null != soft.get() && !soft.get().isDisposed()){
                soft.get().dispose();
            }
        }
        sr_time.clear();
    }
    public void releaseAll(){
        for(int i = 0; i < sr_delay_lost.size(); i++){
            SoftReference<Disposable> soft = sr_delay_lost.get(i);
            if(null != soft && null != soft.get() && !soft.get().isDisposed()){
                soft.get().dispose();
            }
        }
        sr_delay_lost.clear();
    }

    public void toDetectView(){
        isBoosting = true;
        mainView.toDetectView();
    }

    public void setIsBoosting(boolean boosting){
        isBoosting = boosting;
    }

    public boolean getIsBoosting(){
        return isBoosting;
    }
    public void onResume(){
        if(getIsBoosting()){
            mainView.showBoosting();
        } else {
            mainView.initView();
        }
    }
    public boolean getBoostStart(){
        return mainBiz.getBoostStart();
    }
    public boolean getBoostFloatWindow(){
        return mainBiz.getBoostFloatWindow();
    }

    public void onPause(){
        releaseAll();
    }
    public void onDestory(){
        mainView = null;
    }

    /**
     * 该方法会在子线程运行
     */
    public void loadDataAfterRequestDynamicPermissions(Context context) {
        ((JJBoostMainBiz) mainBiz).loadDataAfterRequestDynamicPermissions(context);
    }

    public boolean isInstallApp(){
        return CommonUtil.isInstallApp(JJBoostApplication.application, "cn.jj");
    }

    public boolean hasWifiOrData(){
        return WIFI_DATA_Manager.getInstance() == null ? false:true;
    }
}
