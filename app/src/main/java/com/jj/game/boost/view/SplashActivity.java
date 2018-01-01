package com.jj.game.boost.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.jj.game.boost.logtools.LogResultActivity;
import com.jj.game.boost.utils.MyAnimationDrawable;
import com.jj.game.boost.utils.ThreadManager;
import com.jj.game.boost.R;
import java.lang.ref.WeakReference;

public class SplashActivity extends AbstractActivity {
    private final String mPageName = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        overridePendingTransition(R.anim.stand,R.anim.splash);
        startAnimation();

    }
    private void startAnimation(){
        Handler handler = new Handler();
        ImageView layout = (ImageView)findViewById(R.id.img_bg);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyAnimationDrawable.getInstance().animateRawManuallyFromXML(R.drawable.bees_anim_list, layout, new Runnable(){
                    @Override
                    public void run() {

                    }
                }, new Runnable(){
                    @Override
                    public void run() {
                        ThreadManager.postDelayed(ThreadManager.THREAD_UI, new InnerRunnable(SplashActivity.this), 200);
                    }
                });
            }
        }, 500);
    }
    private static class InnerRunnable implements Runnable {
        private final WeakReference<SplashActivity> mActivity;

        public InnerRunnable(SplashActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            SplashActivity activity = mActivity.get();
            if (activity != null) {
                Intent intent = new Intent(activity, LogResultActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                activity.finish();
            }
        }
    }
    @Override
    protected int obtainLayoutResID() {
        return 0;
    }

    @Override
    protected String getActivityTitle() {
        return null;
    }

    @Override
    protected boolean getActivityHasBack() {
        return false;
    }
    @Override
    protected boolean getActivityHasSetting() {
        return false;
    }
    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        //启动界面，屏蔽Back键
        //super.onBackPressed();
    }
}
