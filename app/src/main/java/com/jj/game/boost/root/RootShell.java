package com.jj.game.boost.root;

import android.app.Dialog;
import android.content.Context;

import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ThreadManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

class RootShell {

    /* write command completion times to logcat */
    private static final boolean enableProfiling = false;

    private static Shell.Interactive rootSession;

    private enum ShellState {
        INIT,
        READY,
        BUSY,
        FAIL
    }

    static ShellState rootState = ShellState.INIT;

    private final static int MAX_RETRIES = 5;

    private static LinkedList<RootCommand> waitQueue = new LinkedList<>();

    //private SynchronousQueue workingCommand = new SynchronousQueue();

    @SuppressWarnings("unused")
    final static int EXIT_SUCCESS = Shell.OnCommandResultListener.SHELL_RUNNING;
    final static int EXIT_TIMEOUT = Shell.OnCommandResultListener.WATCHDOG_EXIT;
    final static int EXIT_NO_ROOT_ACCESS = Shell.OnCommandResultListener.SHELL_DIED;
    final static int EXIT_NO_ROOT_PHONE = Shell.OnCommandResultListener.SHELL_EXEC_FAILED;
    final static int EXIT_NO_COMMAND = -5;

    static class RootCommand {
        List<String> script;
        private Callback cb = null;
        private boolean reopenShell = false;
        private int retryExitCode = -1;
        int commandIndex;
        private boolean ignoreExitCode;
        @SuppressWarnings("unused")
        private Date startTime;
        private int retryCount;
        public StringBuilder res;
        @SuppressWarnings("WeakerAccess")
        public String lastCommand;
        @SuppressWarnings("WeakerAccess")
        public StringBuilder lastCommandResult;
        @SuppressWarnings("WeakerAccess")
        public int exitCode;
        @SuppressWarnings("WeakerAccess")
        public boolean done = false;
        /*private boolean startCheck = false;

		public boolean isStartCheck() {
			return startCheck;
		}

		public RootCommand setStartCheck(boolean startCheck) {
			this.startCheck = startCheck;
			return this;
		}*/

        static abstract class Callback {

            /**
             * Optional user-specified callback
             */
            public abstract void cbFunc(RootCommand state);
        }

        /**
         * Set callback to run after command completion
         *
         * @param cb Callback object, with cbFunc() populated
         * @return RootCommand builder object
         */
        @SuppressWarnings("WeakerAccess")
        public RootCommand setCallback(Callback cb) {
            this.cb = cb;
            return this;
        }

        /**
         * Tell RootShell whether or not it should try to open a new root shell if the last attempt
         * died.  To avoid "thrashing" it might be best to only try this in response to a user
         * request
         *
         * @param reopenShell true to attempt reopening a failed shell
         * @return RootCommand builder object
         */
        RootCommand setReopenShell(boolean reopenShell) {
            this.reopenShell = reopenShell;
            return this;
        }

        public RootCommand setRes(StringBuilder res) {
            this.res = res;
            return this;
        }

        public final void run(Context ctx, boolean isSu, List<String> script, Dialog progressDialog) {
            RootShell.runScriptAsRoot(ctx, isSu, script, this, progressDialog);
        }

        /**
         * Run a series of commands as root; call cb.cbFunc() when complete
         *
         * @param ctx    Context object used to create toasts
         * @param isSu   weather do root command
         * @param script List of commands to run as root
         */
        public final void run(Context ctx, boolean isSu, List<String> script) {
            RootShell.runScriptAsRoot(ctx, isSu, script, this);
        }

        /**
         * Run a single command as root; call cb.cbFunc() when complete
         *
         * @param ctx  Context object used to create toasts
         * @param isSu weather do root command
         * @param cmd  Command to run as root
         */
        public final void run(Context ctx, boolean isSu, String cmd) {
            List<String> script = new ArrayList<>();
            script.add(cmd);
            RootShell.runScriptAsRoot(ctx, isSu, script, this);
        }

        public final void run(Context ctx, boolean isSu) {
            RootShell.runScriptAsRoot(ctx, isSu, null, this);
        }
    }

    private static void complete(final RootCommand state, int exitCode) {
//        LogUtil.d(TAG, "complete state=" + state + " exitcode=" + exitCode
//                + " state=" + state.script + "  waitQueue=" + waitQueue.size());
//        if (enableProfiling) {
//            LogUtil.d(TAG, "RootShell: " + state.script.size() + " commands completed in " +
//                    (new Date().getTime() - state.startTime.getTime()) + " ms");
//        }

        state.exitCode = exitCode;
        state.done = true;
        if (state.cb != null) {
            state.cb.cbFunc(state);
        }
    }

    private static void runNextSubmission(int exitCode) {
        LogUtil.i("runNextSubmission()");
        do {
            RootCommand state;
            try {
                state = waitQueue.remove();
                LogUtil.i("state -> " + state);
            } catch (NoSuchElementException e) {
                // nothing left to do
                LogUtil.i("e -> " + e);
                if (rootState == ShellState.BUSY) {
                    rootState = ShellState.READY;
                }
                break;
            }

            LogUtil.i("rootState -> " + rootState);

            if (enableProfiling) {
                state.startTime = new Date();
            }

            if (rootState == ShellState.FAIL) {
                // if we don't have root, abort all queued commands
                complete(state, exitCode);

                // @note fixbug:在waiteQueue中有多个任务时，直接complete将会导致后续的任务无法回调
                for (RootCommand leftState : waitQueue) {
                    complete(leftState, exitCode);
                }

                waitQueue.clear();
            } else if (rootState == ShellState.READY) {
                rootState = ShellState.BUSY;
                submitNextCommand(state);
            }
        } while (false);
    }

//    private static void runNextSubmission() {
//        runNextSubmission(null);
//    }

    @SuppressWarnings("Convert2streamapi")
    private static void submitNextCommand(final RootCommand state) {
        if (state.script == null || state.script.size() == 0) {
            complete(state, EXIT_NO_COMMAND);
            return;
        }
        String s = state.script.get(state.commandIndex);

        if (s != null) {
            if (s.startsWith("#NOCHK# ")) {
                s = s.replaceFirst("#NOCHK# ", "");
                state.ignoreExitCode = true;
            } else {
                state.ignoreExitCode = false;
            }
            state.lastCommand = s;
            state.lastCommandResult = new StringBuilder();

            Shell.OnCommandResultListener listener = (commandCode, exitCode, output) -> {
                if (output != null) {
                    for (String line : output) {
                        if (line != null && !line.equals("")) {
                            if (state.res != null) {
                                state.res.append(line).append("\n");
                            }
                            state.lastCommandResult.append(line).append("\n");
                        }
                    }
                }

                LogUtil.i("exitCode -> " + exitCode);
                LogUtil.i("state.retryExitCode -> " + state.retryExitCode);
                LogUtil.i("state.retryCount -> " + state.retryCount);
                if (exitCode >= 0 && exitCode == state.retryExitCode && state.retryCount < MAX_RETRIES) {
                    state.retryCount++;
//                        LogUtil.i("command '" + state.lastCommand + "' exited with status " + exitCode +
//                                ", retrying (attempt " + state.retryCount + "/" + MAX_RETRIES + ")");
                    submitNextCommand(state);
                    return;
                }

//                    LogUtil.i("state.script -> " + state.script);
//                    LogUtil.i("state.commandIndex -> " + state.commandIndex);

                state.commandIndex++;
                state.retryCount = 0;

                // 假如RootCommand对象里有3条命令,而且前面两条命令成功,第3条命令失败,那么前面两条成功执行的命令都会走,
                // 也就是说会调用complete()方法.但是第3条命令不管执行成功还是失败都不会走这里.因为最后1条命令执行完后的回调方法另有入口,
                // 也是调用complete()方法,这是整个RootCommand对象执行完后的回调.
                // 需要注意的是,如果第2条命令执行失败,那么第3条命令就不会再执行,第2条命令变成最后1条命令,
                // 第2条命令执行完后的回调就会走整个RootCommand对象执行完后的回调,那么只有第1条命令会这里,因为第1条命令成功执行了.
                // 如果第1条命令执行失败,那么第2条和第3条命令都不会执行,第1条命令变成最后1条命令.
                if (state.commandIndex < state.script.size()) {
                    complete(state, exitCode);
                }

                boolean errorExit = exitCode != 0 && !state.ignoreExitCode;
                if (state.commandIndex >= state.script.size() || errorExit) {
                    complete(state, exitCode);
                    if (exitCode < 0) {
                        rootState = ShellState.FAIL;
                    } else {
//                            if (errorExit) {
//                                LogUtil.i("command '" + state.lastCommand + "' exited with status " + exitCode +
//                                        "\nOutput:\n" + state.lastCommandResult);
//                            }
                        rootState = ShellState.READY;
                    }
                    runNextSubmission(exitCode);
                } else {
                    submitNextCommand(state);
                }
            };
            if (s != null) {
                try {
                    rootSession.addCommand(s, 0, listener);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void startShellInBackground(boolean isSu) {
        //start only rootSession is null
        if (rootSession == null) {
            rootSession = new Shell.Builder()
                    .setShell(isSu ? "su" : "sh")
                    .setWantSTDERR(true)
                    .setWatchdogTimeout(2)
                    .setAutoHandler(false)
                    .setHandler(null)
                    .open((commandCode, exitCode, output) -> {
                        if (exitCode < 0) {
                            LogUtil.i("Can't open root shell: exitCode " + exitCode);
                            rootState = ShellState.FAIL;
                            rootSession = null;
                        } else {
                            LogUtil.i("Root shell is open");
                            rootState = ShellState.READY;
                        }
                        runNextSubmission(exitCode);
                    });
            if (rootState == ShellState.FAIL) {
                rootSession = null;
            }
        }
    }

    private static void reOpenShell(boolean isSu) {
        LogUtil.i("reOpenShell()");
        rootState = ShellState.BUSY;
        startShellInBackground(isSu);
    }

    @SuppressWarnings("UnusedParameters")
    private static void runScriptAsRoot(Context ctx, boolean isSu, List<String> script,
                                        RootCommand state, Dialog progressDialog) {
        LogUtil.i("runScriptAsRoot() -> " + script);

        state.script = script;
        state.commandIndex = 0;
        state.retryCount = 0;

        waitQueue.add(state);
        LogUtil.i("waitQueue.size() -> " + waitQueue.size());
        LogUtil.i("rootState -> " + rootState);
        if (rootState == ShellState.INIT || (rootState == ShellState.FAIL && state.reopenShell)) {
            reOpenShell(isSu);
        } else if (rootState != ShellState.BUSY) {
//            if (progressDialog != null) {
//                progressDialog.show();
//            } else {
//                DialogFractory.showProgressDialog(ctx, true);
//            }
            ThreadManager.executeAsyncTask(() -> runNextSubmission(state.exitCode));
        } else {
            if (script == null || script.size() == 0) {
                complete(state, EXIT_NO_COMMAND);
            }
        }
    }

    private static void runScriptAsRoot(Context ctx, boolean isSu, List<String> script, RootCommand state) {
        runScriptAsRoot(ctx, isSu, script, state, null);
    }

}