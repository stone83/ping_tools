package com.jj.game.boost.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.ccmt.library.lru.LruMap;
import com.jj.game.boost.R;
import com.jj.game.boost.customview.CustomAlertDialog;
import com.jj.game.boost.view.ProgressbarActivity;

/**
 * 弹窗辅助类
 */
public class DialogFractory {

    public static Integer sType;
    private static Dialog sDialog;

    /**
     * 由于createFullScreenProgressDialog()方法返回的对话框对象有问题:
     * 1 当调用setOnKeyListener()方法设置按键监听器的时候,setCancelable(true),显示对话框时,按返回键也无法关闭对话框.
     * 2 当不调用setOnKeyListener()方法设置按键监听器的时候,setCancelable(false)且setCanceledOnTouchOutside(true),
     * 显示对话框时,点击返回键和空白区域都会关闭对话框,显然这不是想要的效果.
     * 3 setCancelable()方法和setCanceledOnTouchOutside()方法的调用顺序不同,效果也会不同.
     * 4 只能在Activity里显示.
     * 总结:
     * 由于以上4个原因,该方法可以灵活的满足各种不同情况的需求.
     *
     * @param context
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static void showFullScreenProgressDialog(Context context) {
        if (sDialog != null) {
            return;
        }
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_progressbar, null);
        MyAnimationDrawable myAnimationDrawable = MyAnimationDrawable.getInstance();
        CustomAlertDialog customAlertDialog = new CustomAlertDialog.Builder(context,
                R.style.custom_alter_dialog_style_fullscreen_style)
//                .setMessageView(view)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setOnDismissListener(dialog -> myAnimationDrawable.release())
                .setGlobalDialog(!(context instanceof Activity))
                .create();
        customAlertDialog.setOnShowListener(dialog -> myAnimationDrawable
                .animateRawManuallyFromXML(R.drawable.bees_loading_anim_list, ((ImageView) view.findViewById(R.id.iv_loading)),
                        null, null, true));
        sDialog = customAlertDialog;
        sDialog.show();
        customAlertDialog.setContentView(view);
    }

    public static void closeFullScreenProgressDialog() {
        if (sDialog == null) {
            return;
        }
        sDialog.dismiss();
        sDialog = null;
    }
    @SuppressWarnings("JavaDoc")
    public static Dialog createFullScreenProgressDialogNew(Context context) {
        if (context == null) {
            return null;
        }
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_progressbar, null);
        CustomAlertDialog customAlertDialog = new CustomAlertDialog.Builder(context, R.style.custom_alter_dialog_style_fullscreen_style)
//                .setMessageView(view)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setGlobalDialog(!(context instanceof Activity))
                .create();
        customAlertDialog.setView(view);
        return customAlertDialog;
    }
    /**
     * 显示通用转圈对话框
     *
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess"})
    public static void showProgressDialog(Context context, boolean isRotate, boolean isNotTouchModal) {
        LruMap lruMap = LruMap.getInstance();
        if (lruMap.get("isShow") != null) {
            return;
        }
        LogUtil.i("showProgressDialog()");
        LogUtil.i("可以显示通用转圈对话框");
        lruMap.put("isShow", true);
        if (isRotate) {
            lruMap.put("isRotate", true);
        }
        if (isNotTouchModal) {
            lruMap.put("isNotTouchModal", true);
        }

        Intent intent = new Intent(context, ProgressbarActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void showProgressDialog(Context context, boolean isRotate) {
        showProgressDialog(context, isRotate, false);
    }

    /**
     * 关闭通用转圈对话框
     */
    @SuppressWarnings("JavaDoc")
    public static void closeProgressDialog(Context context) {
        LruMap lruMap = LruMap.getInstance();
        if (lruMap.get("isShow") == null) {
            return;
        }
        LogUtil.i("closeProgressDialog()");
        LogUtil.i("可以关闭通用转圈对话框");
        lruMap.remove("isShow", false);
        lruMap.remove("isRotate", false);
        lruMap.remove("isNotTouchModal", false);

        Intent intent = new Intent(context, ProgressbarActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
