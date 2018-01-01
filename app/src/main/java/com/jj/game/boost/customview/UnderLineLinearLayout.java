package com.jj.game.boost.customview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccmt.library.util.ViewUtil;
import com.jj.game.boost.R;
import com.jj.game.boost.utils.MyAnimationDrawable;
import com.jj.game.boost.wifi4g.AbstractWIFI_DATA;
import com.jj.game.boost.wifi4g.Const;

public class UnderLineLinearLayout extends LinearLayout {
    private Context mContext;
    private LinearLayout layout_right, layout_left;
    private int i = 0;
    private int i_text = 1;
    private int j_text = 1;
    private int anima_id = 0;
    private String Net = getResources().getString(R.string.net);
    private String Net_Erro = getResources().getString(R.string.net_erro);
    private String Net_WifiName = "";
    private String Run = getResources().getString(R.string.run);
    private String Smart = getResources().getString(R.string.smart);
    private String Smart_Pre = getResources().getString(R.string.smart_pre);
    private String Finish = getResources().getString(R.string.finish);
    private String Run_Clear = getResources().getString(R.string.run_clean);
    private int wifi_level = 0;
    private boolean mIsShowCurRun;
    private int mSize;
    public static final int TIME = 50;
    private boolean isRecycler = false;
    private Handler mHandler = null;

    public void setHandler(Handler handler){
        mHandler = handler;
    }

    public void setHasRecycler(boolean ishas){
        isRecycler = ishas;
    }
    public void setWifi_level(int level){
        wifi_level = level;
    }

    public void setNet_wifiName(String name){
        Net_WifiName = name;
    }
    public UnderLineLinearLayout(Context context) {
        this(context, null);
    }

    public UnderLineLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnderLineLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        Resources resources = context.getResources();
        Run = resources.getString(R.string.activity_detect_check_run);
        Run_Clear = resources.getString(R.string.activity_detect_clear_background_app);

        setWillNotDraw(false);
        initView(context);

        layout_left = new LinearLayout(getContext());
        layout_left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_left.setLayoutParams(lp);
        layout_right = new LinearLayout(getContext());
        LinearLayout.LayoutParams lp_right = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_right.setLayoutParams(lp_right);
        layout_right.setOrientation(LinearLayout.VERTICAL);
        addView(layout_left);
        addView(layout_right);
        View v_line1 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_timeline, this, false);
        View v_line2 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_timeline, this, false);
        View v_line3 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_timeline, this, false);
        View v_line4 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_timeline, this, false);

        addChildView_CustomeLeft(v_line1);
        addChildView_CustomeLeft(v_line2);
        addChildView_CustomeLeft(v_line3);
        addChildView_CustomeLeft(v_line4);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_net, this, false);
        addChildView_Custome(v);
        View v2 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_run, this, false);
        addChildView_Custome(v2);
        View v3 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_smart, this, false);
        addChildView_Custome(v3);
        View v4 = LayoutInflater.from(getContext()).inflate(R.layout.item_vertical_finish, this, false);
        addChildView_Custome(v4);
        initRight(View.INVISIBLE);
    }

    private void initRight(int visible){
        for(int i = 0; i < layout_right.getChildCount(); i++){
            layout_right.getChildAt(i).setVisibility(visible);
        }
    }

    private void showRight(int index){
        layout_right.getChildAt(index).setVisibility(View.VISIBLE);
    }

    public void addChildView_Custome(View view){
        layout_right.addView(view);
    }
    public void addChildView_CustomeLeft(View view){
        layout_left.addView(view);
    }
    private void initView(Context context) {
    }

    private void startLine2Animation(int index, int Res){
        Handler handler = new Handler();
        ImageView imageView = (ImageView) layout_left.getChildAt(index).findViewById(Res);
        ImageView imageIcon = (ImageView) layout_left.getChildAt(index).findViewById(R.id.img_icon);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                i = i + 20;
                params.height = i ;
                imageView.setLayoutParams(params);
                int pos = 0;
                if(isRecycler){
                    if(index == 0){
                        pos = layout_right.getChildAt(index).getPaddingBottom()
                                + layout_right.getChildAt(index + 1).getPaddingTop()
                                - ViewUtil.obtainViewPx(getContext(), 20, true);
                    }
                    if(index == 1){
                        pos = layout_right.getChildAt(index).getPaddingBottom()
                                + layout_right.getChildAt(index + 1).getPaddingTop()
                                + ViewUtil.obtainViewPx(getContext(), 410, true);
                    }
                    if(index == 2){
                        pos = layout_right.getChildAt(index).getPaddingBottom()
                                + layout_right.getChildAt(index + 1).getPaddingTop()
                                + ViewUtil.obtainViewPx(getContext(), 20, true);
                    }
                } else {
                    pos = (layout_right.getChildAt(index).getHeight()/2 - imageIcon.getHeight()/2)
                            + (layout_right.getChildAt(index + 1).getHeight()/2 - imageIcon.getHeight()/2);
                }
                if(i < pos){
                    handler.postDelayed(this, 1);
                } else {
                    handler.removeCallbacks(this);
                    //线条动画执行完毕
                    lineAnimationFinish(index);
                }
            }
        };
        handler.postDelayed(runnable, 1);
    }

    public void lineAnimationFinish(int index){
        j_text = 1;
        i_text = 1;
        i = 0;
        if(0 == index){
            startLineAnimation(1, R.id.img_icon, mIsShowCurRun, mSize);
        } else if(1 == index){
            startLineAnimation(2, R.id.img_icon, mIsShowCurRun, mSize);
        } else if(2 == index){
            startLineAnimation(3, R.id.img_icon, mIsShowCurRun, mSize);
        } else if(3 == index){

        } else {

        }
    }

    @SuppressLint("StringFormatMatches")
    public void showTextView(int index, int Res){
        TextView tile = (TextView)layout_right.getChildAt(index).findViewById(Res);
        Handler handler = new Handler();
        if(index == 0){
            if(wifi_level == 3){
                Net = Net_Erro;
            }
        } else if(index == 1){
            if (mIsShowCurRun) {
                Net = Run;
            } else {
                Net = String.format(getContext().getResources().getString(R.string.activity_detect_network_app), mSize);
            }
        } else if(index == 2){
            Net = Smart;
        } else if(index == 3){
            Net = Finish;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String kk = Net.substring(0, i_text++);
                tile.setText(kk);
                handler.postDelayed(this, TIME);
                if(Net.length() == i_text - 1){
                    handler.removeCallbacks(this);
                    if (index == 1) {
                        if (mIsShowCurRun) {
                            showTextView2(index, R.id.lable_name);
                        } else {
                            // TODO 暂时保留,这里播放RecyclerView动画.
                            startLine2Animation(index, R.id.img_line);
                        }
                    } else {
                        showTextView2(index, R.id.lable_name);
                    }
                }
            }
        };
        handler.postDelayed(runnable, TIME);
    }

    public void showTextView2(int index, int Res){
        TextView name = (TextView)layout_right.getChildAt(index).findViewById(Res);
        Handler handler = new Handler();
        int length = Net_WifiName.length();

        if(AbstractWIFI_DATA.getNetType().equals(Const.WIFI)){
            Net_WifiName = Net_WifiName.substring(1, length - 1);
        }

        if(index == 1){
            Net_WifiName = Run_Clear;
        } else if(index == 2){
            Net_WifiName = Smart_Pre;
        } else if(index == 3){
            Net_WifiName = Finish;
            ImageView dot = (ImageView)layout_right.getChildAt(index).findViewById(R.id.img_dot);
            dot.setVisibility(View.VISIBLE);
            showDotAnimation(index, R.id.img_dot);
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String kk2 = Net_WifiName.substring(0, j_text++); Log.e("huzedong", " wifi name : " + Net_WifiName);
                name.setText(kk2);
                handler.postDelayed(this, TIME);
                if(Net_WifiName.length() == j_text - 1){
                    handler.removeCallbacks(this);
                    if(wifi_level == 3 && index == 0){
                        i_text = 1;
                        j_text = 1;
                        Button shutdown = (Button)layout_right.getChildAt(index).findViewById(R.id.lable_shutdown);
                        if(null != shutdown){
                            shutdown.setVisibility(View.VISIBLE);
                        }
                        startLineAnimation(1, R.id.img_icon, mIsShowCurRun, mSize);
                        return;
                    } else {
                        ImageView dot = (ImageView)layout_right.getChildAt(index).findViewById(R.id.img_dot);
                        dot.setVisibility(View.VISIBLE);
                        showDotAnimation(index, R.id.img_dot);
                    }
                }
            }
        };
        handler.postDelayed(runnable, TIME);
    }

    public void showDotAnimation(int index, int Res){
        Handler handler = new Handler();
        ImageView dot = (ImageView)layout_right.getChildAt(index).findViewById(Res);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyAnimationDrawable.getInstance().animateRawManuallyFromXML(R.drawable.line_dot_anim, dot, new Runnable(){
                    @Override
                    public void run() {

                    }
                }, new Runnable(){
                    @Override
                    public void run() {
                        ImageView scalebtn = (ImageView)layout_right.getChildAt(index).findViewById(R.id.img_btn_ok);
                        scalebtn.setVisibility(View.VISIBLE);
                        showScaleBtnAnimation(index, R.id.img_btn_ok);
                    }
                });
            }
        }, 100);
    }
    public void showScaleBtnAnimation(int index, int Res){
        ImageView scalebtn = (ImageView)layout_right.getChildAt(index).findViewById(Res);
        ObjectAnimator anim = ObjectAnimator.ofFloat(scalebtn, "scaleY", 1f, 2f, 1f);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(scalebtn, "scaleX", 1f, 2f, 1f);
        anim.setDuration(500);
        anim1.setDuration(500);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
            }
        });
        anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
            }
        });
        anim1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(index != 3){
                    startLine2Animation(index, R.id.img_line);
                } else {
                    if(isRecycler || wifi_level == 3){
                        Message msg = mHandler.obtainMessage();
                        msg.arg1 = 1;
                        msg.sendToTarget();
                    } else {
                        Message msg = mHandler.obtainMessage();
                        msg.arg1 = 2;
                        msg.sendToTarget();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        // 正式开始启动执行动画
        anim.start();
        anim1.start();
    }
    public void startLineAnimation(int index, int Res,boolean isShowCurRun, int size){
        this.mIsShowCurRun = isShowCurRun;
        this.mSize = size;
        if(0 == index){
            if(AbstractWIFI_DATA.getNetType().equals(Const.WIFI)){
                anima_id = R.drawable.line_anim_list;
            } else {
                anima_id = R.drawable.line_anim_data_list;
            }
        } else if(1 == index){
            anima_id = R.drawable.line_anim_list_run;
        } else if(2 == index){
            anima_id = R.drawable.line_anim_list_smart;
        } else if(3 == index){
            anima_id = R.drawable.line_anim_list_finish;
        } else {
            anima_id = R.drawable.line_anim_list;
        }
        Handler handler = new Handler();
        ImageView view = (ImageView) layout_left.getChildAt(index).findViewById(Res);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MyAnimationDrawable.getInstance().animateRawManuallyFromXML(anima_id, view, new Runnable(){
                    @Override
                    public void run() {

                    }
                }, new Runnable(){
                    @Override
                    public void run() {
                        if(0 == index){
                            if(AbstractWIFI_DATA.getNetType().equals(Const.WIFI)){
                                view.setImageResource(R.drawable.h);
                            } else {
                                view.setImageResource(R.drawable.data8);
                            }
                        } else if(1 == index){
                            view.setImageResource(R.drawable.run9);
                        } else if(2 == index){
                            view.setImageResource(R.drawable.smart10);
                        } else if(3 == index){
                            view.setImageResource(R.drawable.finish6);
                        } else {
                            view.setImageResource(R.drawable.h);
                        }
                        //启动文字动画等
                        showRight(index);
                        showTextView(index, R.id.lable_test);
                    }
                });
            }
        }, 100);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        for(int i = 0; i < layout_left.getChildCount(); i++){
            ImageView imageView = (ImageView) layout_left.getChildAt(i).findViewById(R.id.img_icon);
            LinearLayout.LayoutParams params_icon = (LinearLayout.LayoutParams)imageView.getLayoutParams();
            params_icon.topMargin = 0;
//            if (i == 0) {
                layout_left.getChildAt(i).setPadding(ViewUtil.obtainViewPx(getContext(), 40, true),
                        0, 0, 0);
//            } else {
//                layout_left.getChildAt(i).setPadding(ViewUtil.obtainViewPx(getContext(), 40, true), 0, 0, 0);
//            }
            imageView.setLayoutParams(params_icon);
        }
    }
}
