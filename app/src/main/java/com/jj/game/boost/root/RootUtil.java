package com.jj.game.boost.root;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.view.View;

import com.jj.game.boost.utils.DialogFractory;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ThreadManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RootUtil {

    @SuppressWarnings("WeakerAccess")
    public interface IProcessResult {
        void processSuccess(Context context, View view, StringBuilder sb);

        void processError(Context context, View view, StringBuilder sb);
    }

    @SuppressWarnings("WeakerAccess")
    public static void sendShell(final Context context, final View view,
                                 IProcessResult processResult, final String[] commonds,
                                 boolean isDialogShow) {
        Dialog progressDialog = null;
//        if (isDialogShow) {
//            progressDialog = DialogFractory.createFullScreenProgressDialogNew(context);
//            progressDialog.show();
//        } else {
//            DialogFractory.showProgressDialog(context, true);
//        }
        if (isDialogShow) {
            progressDialog = DialogFractory.createFullScreenProgressDialogNew(context);
        }

        List<String> list = null;
        if (commonds != null && commonds.length > 0) {
            list = new ArrayList<>();
            Collections.addAll(list, commonds);
        }
        Dialog progressDialogTemp = progressDialog;
        new RootShell.RootCommand().setCallback(new RootShell.RootCommand.Callback() {
            @Override
            public void cbFunc(RootShell.RootCommand state) {
                LogUtil.i("cbFunc()");
                LogUtil.i("Thread.currentThread().getName() -> " + Thread.currentThread().getName());
                LogUtil.i("state.exitCode -> " + state.exitCode);
                LogUtil.i("state.res -> " + state.res);
                LogUtil.i("RootShell.rootState -> " + RootShell.rootState);

                boolean onMainThread = RootUtil.isOnMainThread();
                if (state.exitCode == 0) {
                    // root执行成功
                    LogUtil.i("root执行成功");

                    if (onMainThread) {
                        // 主线程
                        if (state.commandIndex < state.script.size()) {
                            // 不是RootCommand对象的最后1条命令
                            LogUtil.i("不是RootCommand对象的最后1条命令");

                            return;
                        }

                        processResult.processSuccess(context, view, state.res);
                        endRoot(context, view, progressDialogTemp);
                    } else {
                        // 子线程
                        ThreadManager.post(() -> {
                            if (state.commandIndex < state.script.size()) {
                                // 不是RootCommand对象的最后1条命令
                                LogUtil.i("不是RootCommand对象的最后1条命令");

                                return;
                            }

                            processResult.processSuccess(context, view, state.res);
                            endRoot(context, view, progressDialogTemp);
                        });
                    }
                } else {
                    // root执行失败
                    LogUtil.i("root执行失败");

                    if (onMainThread) {
                        // 主线程
                        if (state.exitCode == RootShell.EXIT_NO_ROOT_PHONE) {
                            // 手机没有root
                            LogUtil.i("手机没有root");

                            processResult.processError(context, view, state.res);
                            endRoot(context, view, progressDialogTemp);

                            return;
                        }

                        // 手机有root
                        if (state.exitCode == RootShell.EXIT_NO_ROOT_ACCESS) {
                            // 用户拒绝授权给应用
                            LogUtil.i("用户拒绝授权给应用");

                            processResult.processError(context, view, state.res);
                            endRoot(context, view, progressDialogTemp);

                            return;
                        }

                        if (state.exitCode == RootShell.EXIT_TIMEOUT) {
                            // 超时
                            LogUtil.i("超时");

                            processResult.processError(context, view, state.res);
                            endRoot(context, view, progressDialogTemp);

                            return;
                        }

                        if (state.exitCode > 0) {
                            // 命令执行失败
                            LogUtil.i("命令执行失败");

                            processResult.processError(context, view, state.res);
                            endRoot(context, view, progressDialogTemp);

                            return;
                        }

                        if (state.exitCode == RootShell.EXIT_NO_COMMAND) {
                            // 没有要执行的root命令
                            LogUtil.i("没有要执行的root命令");
                        }

                        processResult.processSuccess(context, view, state.res);
                        endRoot(context, view, progressDialogTemp);
                    } else {
                        // 子线程
                        ThreadManager.post(() -> {
                            if (state.exitCode == RootShell.EXIT_NO_ROOT_PHONE) {
                                // 手机没有root
                                LogUtil.i("手机没有root");

                                processResult.processError(context, view, state.res);
                                endRoot(context, view, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode == RootShell.EXIT_NO_ROOT_ACCESS) {
                                // 用户拒绝授权给应用
                                LogUtil.i("用户拒绝授权给应用");

                                processResult.processError(context, view, state.res);
                                endRoot(context, view, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode == RootShell.EXIT_TIMEOUT) {
                                // 超时
                                LogUtil.i("超时");

                                processResult.processError(context, view, state.res);
                                endRoot(context, view, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode > 0) {
                                // 命令执行失败
                                LogUtil.i("命令执行失败");

                                processResult.processError(context, view, state.res);
                                endRoot(context, view, progressDialogTemp);

                                return;
                            }

                            if (state.exitCode == RootShell.EXIT_NO_COMMAND) {
                                // 没有要执行的root命令
                                LogUtil.i("没有要执行的root命令");
                            }

                            processResult.processSuccess(context, view, state.res);
                            endRoot(context, view, progressDialogTemp);
                        });
                    }
                }
            }
        }).setReopenShell(true).setRes(new StringBuilder()).run(context, false, list, progressDialogTemp);
    }

    public static void sendShell(final Context context, IProcessResult processResult,
                                 final String[] commonds) {
        sendShell(context, null, processResult, commonds, true);
    }

    private static void endRoot(Context context, View view, Dialog progressDialog) {
        if (view != null) {
            view.setEnabled(true);
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        } else {
            DialogFractory.closeProgressDialog(context);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isOnMainThread() {
        return ((Looper.myLooper() != null) && (Looper.myLooper() == Looper.getMainLooper()));
    }

}
