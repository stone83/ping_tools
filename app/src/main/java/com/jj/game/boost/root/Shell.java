/*
 * Copyright (C) 2012-2015 Jorrit "Chainfire" Jongma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jj.game.boost.root;

import android.os.Handler;
import android.os.Looper;

import com.ccmt.library.exception.ShellOnMainThreadException;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.ThreadManager;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class providing functionality to execute commands in a (root) shell
 */
public class Shell {

    private static final int WATCH_DOG_TIME_OUT = 60;

    /**
     * <p>
     * Runs commands using the supplied shell, and returns the output, or null
     * in case of errors.
     * </p>
     * <p>
     * This method is deprecated and only provided for backwards compatibility.
     * Use {@link #run(String, String[], String[], boolean)} instead, and see
     * that same method for usage notes.
     * </p>
     *
     * @param shell      The shell to use for executing the commands
     * @param commands   The commands to execute
     * @param wantSTDERR Return STDERR in the output ?
     * @return Output of the commands, or null in case of an error
     */
    @Deprecated
    public static List<String> run(String shell, String[] commands, boolean wantSTDERR) {
        return run(shell, commands, null, wantSTDERR);
    }

    /**
     * <p>
     * Runs commands using the supplied shell, and returns the output, or null
     * in case of errors.
     * </p>
     * <p>
     * Note that due to compatibility with older Android versions, wantSTDERR is
     * not implemented using redirectErrorStream, but rather appended to the
     * output. STDOUT and STDERR are thus not guaranteed to be in the correct
     * order in the output.
     * </p>
     * <p>
     * Note as well that this code will intentionally crash when run in debug
     * mode from the main thread of the application. You should always execute
     * shell commands from a background thread.
     * </p>
     * <p>
     * When in debug mode, the code will also excessively log the commands
     * passed to and the output returned from the shell.
     * </p>
     * <p>
     * Though this function uses background threads to gobble STDOUT and STDERR
     * so a deadlock does not occur if the shell produces massive output, the
     * output is still stored in a List&lt;String&gt;, and as such doing
     * something like <em>'ls -lR /'</em> will probably have you run out of
     * memory.
     * </p>
     *
     * @param shell       The shell to use for executing the commands
     * @param commands    The commands to execute
     * @param environment List of all environment variables (in 'key=value'
     *                    format) or null for defaults
     * @param wantSTDERR  Return STDERR in the output ?
     * @return Output of the commands, or null in case of an error
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    public static List<String> run(String shell, String[] commands, String[] environment,
                                   boolean wantSTDERR) {
        if (RootUtil.isOnMainThread()) {
            throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_COMMAND);
        }

        List<String> res = Collections.synchronizedList(new ArrayList<String>());

        try {
            // Combine passed environment with system environment
            if (environment != null) {
                Map<String, String> newEnvironment = new HashMap<>();
                newEnvironment.putAll(System.getenv());
                int split;
                for (String entry : environment) {
                    if ((split = entry.indexOf("=")) >= 0) {
                        newEnvironment.put(entry.substring(0, split), entry.substring(split + 1));
                    }
                }
                int i = 0;
                environment = new String[newEnvironment.size()];
                for (Map.Entry<String, String> entry : newEnvironment.entrySet()) {
                    environment[i] = entry.getKey() + "=" + entry.getValue();
                    i++;
                }
            }

            // setup our process, retrieve STDIN stream, and STDOUT/STDERR
            // gobblers
            Process process = Runtime.getRuntime().exec(shell, environment);
            DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
            StreamGobbler STDOUT = new StreamGobbler(process.getInputStream(),
                    res);
            StreamGobbler STDERR = new StreamGobbler(process.getErrorStream(),
                    wantSTDERR ? res : null);

            // start gobbling and write our commands to the shell
            STDOUT.start();
            STDERR.start();
            try {
                for (String write : commands) {
//                    Debug.logCommand(String.format("[%s+] %s", shellUpper, write));
                    STDIN.write((write + "\n").getBytes("UTF-8"));
                    STDIN.flush();
                }
                STDIN.write("exit\n".getBytes("UTF-8"));
                STDIN.flush();
            } catch (IOException e) {
                if (e.getMessage().contains("EPIPE")) {
                    // method most horrid to catch broken pipe, in which case we
                    // do nothing. the command is not a shell, the shell closed
                    // STDIN, the script already contained the exit command, etc.
                    // these cases we want the output instead of returning null
                } else {
                    // other issues we don't know how to handle, leads to
                    // returning null
                    throw e;
                }
            }

            // wait for our process to finish, while we gobble away in the
            // background
            process.waitFor();

            // make sure our threads are done gobbling, our streams are closed,
            // and the process is destroyed - while the latter two shouldn't be
            // needed in theory, and may even produce warnings, in "normal" Java
            // they are required for guaranteed cleanup of resources, so lets be
            // safe and do this on Android as well
            try {
                STDIN.close();
            } catch (IOException e) {
                // might be closed already
            }
            STDOUT.join();
            STDERR.join();
            process.destroy();

            // in case of su, 255 usually indicates access denied
            if (SU.isSU(shell) && (process.exitValue() == 255)) {
                res = null;
            }
        } catch (IOException e) {
            // shell probably not found
            res = null;
        } catch (InterruptedException e) {
            // this should really be re-thrown
            res = null;
        }

//        Debug.logCommand(String.format("[%s%%] END", shell.toUpperCase(Locale.ENGLISH)));
        return res;
    }

    private static String[] availableTestCommands = new String[]{
            "echo -BOC-",
            "id"
    };

    /**
     * See if the shell is alive, and if so, check the UID
     *
     * @param ret          Standard output from running availableTestCommands
     * @param checkForRoot true if we are expecting this shell to be running as
     *                     root
     * @return true on success, false on error
     */
    private static boolean parseAvailableResult(List<String> ret, boolean checkForRoot) {
        if (ret == null)
            return false;

        // this is only one of many ways this can be done
        boolean echo_seen = false;

        for (String line : ret) {
            if (line.contains("uid=")) {
                // id command is working, let's see if we are actually root
                return !checkForRoot || line.contains("uid=0");
            } else if (line.contains("-BOC-")) {
                // if we end up here, at least the su command starts some kind
                // of shell, let's hope it has root privileges - no way to know without
                // additional native binaries
                echo_seen = true;
            }
        }

        return echo_seen;
    }

    /**
     * This class provides utility functions to easily execute commands using SH
     */
    @SuppressWarnings("unused")
    public static class SH {
        /**
         * Runs command and return output
         *
         * @param command The command to run
         * @return Output of the command, or null in case of an error
         */
        public static List<String> run(String command) {
            return Shell.run("sh", new String[]{
                    command
            }, null, false);
        }

        /**
         * Runs commands and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null in case of an error
         */
        public static List<String> run(List<String> commands) {
            return Shell.run("sh", commands.toArray(new String[commands.size()]), null, false);
        }

        /**
         * Runs commands and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null in case of an error
         */
        public static List<String> run(String[] commands) {
            return Shell.run("sh", commands, null, false);
        }
    }

    /**
     * This class provides utility functions to easily execute commands using SU
     * (root shell), as well as detecting whether or not root is available, and
     * if so which version.
     */
    @SuppressWarnings("WeakerAccess")
    public static class SU {
        private static Boolean isSELinuxEnforcing = null;
        private static String[] suVersion = new String[]{
                null, null
        };

        /**
         * Runs command as root (if available) and return output
         *
         * @param command The command to run
         * @return Output of the command, or null if root isn't available or in
         * case of an error
         */
        public static List<String> run(String command) {
            return Shell.run("su", new String[]{
                    command
            }, null, false);
        }

        /**
         * Runs commands as root (if available) and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null if root isn't available or in
         * case of an error
         */
        public static List<String> run(List<String> commands) {
            return Shell.run("su", commands.toArray(new String[commands.size()]), null, false);
        }

        /**
         * Runs commands as root (if available) and return output
         *
         * @param commands The commands to run
         * @return Output of the commands, or null if root isn't available or in
         * case of an error
         */
        public static List<String> run(String[] commands) {
            return Shell.run("su", commands, null, false);
        }

        /**
         * Detects whether or not superuser access is available, by checking the
         * output of the "id" command if available, checking if a shell runs at
         * all otherwise
         *
         * @return True if superuser access available
         */
        public static boolean available() {
            // this is only one of many ways this can be done
            List<String> ret = run(Shell.availableTestCommands);
            return Shell.parseAvailableResult(ret, true);
        }

        /**
         * <p>
         * Detects the version of the su binary installed (if any), if supported
         * by the binary. Most binaries support two different version numbers,
         * the public version that is displayed to users, and an internal
         * version number that is used for version number comparisons. Returns
         * null if su not available or retrieving the version isn't supported.
         * </p>
         * <p>
         * Note that su binary version and GUI (APK) version can be completely
         * different.
         * </p>
         * <p>
         * This function caches its result to improve performance on multiple
         * calls
         * </p>
         *
         * @param internal Request human-readable version or application
         *                 internal version
         * @return String containing the su version or null
         */
        public static synchronized String version(boolean internal) {
            int idx = internal ? 0 : 1;
            if (suVersion[idx] == null) {
                String version = null;

                List<String> ret = Shell.run(
                        internal ? "su -V" : "su -v",
                        new String[]{"exit"},
                        null,
                        false
                );

                if (ret != null) {
                    for (String line : ret) {
                        if (!internal) {
                            if (!line.trim().equals("")) {
                                version = line;
                                break;
                            }
                        } else {
                            try {
                                if (Integer.parseInt(line) > 0) {
                                    version = line;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                // should be parsable, try next line otherwise
                            }
                        }
                    }
                }

                suVersion[idx] = version;
            }
            return suVersion[idx];
        }

        /**
         * Attempts to deduce if the shell command refers to a su shell
         *
         * @param shell Shell command to run
         * @return Shell command appears to be su
         */
        public static boolean isSU(String shell) {
            // Strip parameters
            int pos = shell.indexOf(' ');
            if (pos >= 0) {
                shell = shell.substring(0, pos);
            }

            // Strip path
            pos = shell.lastIndexOf('/');
            if (pos >= 0) {
                shell = shell.substring(pos + 1);
            }

            return shell.equals("su");
        }

        /**
         * Constructs a shell command to start a su shell using the supplied uid
         * and SELinux context. This is can be an expensive operation, consider
         * caching the result.
         *
         * @param uid     Uid to use (0 == root)
         * @param context (SELinux) context name to use or null
         * @return Shell command
         */
        public static String shell(int uid, String context) {
            // su[ --context <context>][ <uid>]
            String shell = "su";

            if ((context != null) && isSELinuxEnforcing()) {
                String display = version(false);
                String internal = version(true);

                // We only know the format for SuperSU v1.90+ right now
                // TODO add detection for other su's that support this
                if ((display != null) &&
                        (internal != null) &&
                        (display.endsWith("SUPERSU")) &&
                        (Integer.valueOf(internal) >= 190)) {
                    shell = String.format(Locale.ENGLISH, "%s --context %s", shell, context);
                }
            }

            // Most su binaries support the "su <uid>" format, but in case
            // they don't, lets skip it for the default 0 (root) case
            if (uid > 0) {
                shell = String.format(Locale.ENGLISH, "%s %d", shell, uid);
            }

            return shell;
        }

        /**
         * Constructs a shell command to start a su shell connected to mount
         * master daemon, to perform public mounts on Android 4.3+ (or 4.2+ in
         * SELinux enforcing mode)
         *
         * @return Shell command
         */
        @SuppressWarnings("unused")
        public static String shellMountMaster() {
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                return "su --mount-master";
            }
            return "su";
        }

        /**
         * Detect if SELinux is set to enforcing, caches result
         *
         * @return true if SELinux set to enforcing, or false in the case of
         * permissive or not present
         */
        @SuppressWarnings({"TryFinallyCanBeTryWithResources", "unchecked", "ThrowFromFinallyBlock"})
        public static synchronized boolean isSELinuxEnforcing() {
            if (isSELinuxEnforcing == null) {
                Boolean enforcing = null;

                // First known firmware with SELinux built-in was a 4.2 (17)
                // leak
                if (android.os.Build.VERSION.SDK_INT >= 17) {
                    // Detect enforcing through sysfs, not always present
                    File f = new File("/sys/fs/selinux/enforce");
                    if (f.exists()) {
                        try {
                            InputStream is = new FileInputStream("/sys/fs/selinux/enforce");
                            try {
                                enforcing = (is.read() == '1');
                            } finally {
                                is.close();
                            }
                        } catch (Exception e) {
                            // we might not be allowed to read, thanks SELinux
                        }
                    }

                    // 4.4+ has a new API to detect SELinux mode, so use it
                    // SELinux is typically in enforced mode, but emulators may have SELinux disabled
                    if (enforcing == null) {
                        try {
                            Class seLinux = Class.forName("android.os.SELinux");
                            Method isSELinuxEnforced = seLinux.getMethod("isSELinuxEnforced");
                            enforcing = (Boolean) isSELinuxEnforced.invoke(seLinux.newInstance());
                        } catch (Exception e) {
                            // 4.4+ release builds are enforcing by default, take the gamble
                            enforcing = (android.os.Build.VERSION.SDK_INT >= 19);
                        }
                    }
                }

                if (enforcing == null) {
                    enforcing = false;
                }

                isSELinuxEnforcing = enforcing;
            }
            return isSELinuxEnforcing;
        }

        /**
         * <p>
         * Clears results cached by isSELinuxEnforcing() and version(boolean
         * internal) calls.
         * </p>
         * <p>
         * Most apps should never need to call this, as neither enforcing status
         * nor su version is likely to change on a running device - though it is
         * not impossible.
         * </p>
         */
        @SuppressWarnings("unused")
        public static synchronized void clearCachedResults() {
            isSELinuxEnforcing = null;
            suVersion[0] = null;
            suVersion[1] = null;
        }
    }

    private interface OnResult {
        // for any onCommandResult callback
        int WATCHDOG_EXIT = -1;
        int SHELL_DIED = -2;

        // for Interactive.open() callbacks only
        int SHELL_EXEC_FAILED = -3;
        int SHELL_WRONG_UID = -4;
        int SHELL_RUNNING = 0;
    }

    /**
     * Command result callback, notifies the recipient of the completion of a
     * command block, including the (last) exit code, and the full output
     */
    @SuppressWarnings("WeakerAccess")
    public interface OnCommandResultListener extends OnResult {
        /**
         * <p>
         * Command result callback
         * </p>
         * <p>
         * Depending on how and on which thread the shell was created, this
         * callback may be executed on one of the gobbler threads. In that case,
         * it is important the callback returns as quickly as possible, as
         * delays in this callback may pause the native process or even result
         * in a deadlock
         * </p>
         * <p>
         * See {@link Interactive} for threading details
         * </p>
         *
         * @param commandCode Value previously supplied to addCommand
         * @param exitCode    Exit code of the last command in the block
         * @param output      All output generated by the command block
         */
        void onCommandResult(int commandCode, int exitCode, List<String> output);
    }

    /**
     * Command per line callback for parsing the output line by line without
     * buffering It also notifies the recipient of the completion of a command
     * block, including the (last) exit code.
     */
    @SuppressWarnings("WeakerAccess")
    public interface OnCommandLineListener extends OnResult, StreamGobbler.OnLineListener {
        /**
         * <p>
         * Command result callback
         * </p>
         * <p>
         * Depending on how and on which thread the shell was created, this
         * callback may be executed on one of the gobbler threads. In that case,
         * it is important the callback returns as quickly as possible, as
         * delays in this callback may pause the native process or even result
         * in a deadlock
         * </p>
         * <p>
         * See {@link Interactive} for threading details
         * </p>
         *
         * @param commandCode Value previously supplied to addCommand
         * @param exitCode    Exit code of the last command in the block
         */
        void onCommandResult(int commandCode, int exitCode);
    }

    /**
     * Internal class to store command block properties
     */
    private static class Command {
        private static int commandCounter = 0;

        private final String[] commands;
        private final int code;
        private final OnCommandResultListener onCommandResultListener;
        private final OnCommandLineListener onCommandLineListener;
        private final String marker;

        public Command(String[] commands, int code,
                       OnCommandResultListener onCommandResultListener,
                       OnCommandLineListener onCommandLineListener) {
            this.commands = commands;
            this.code = code;
            this.onCommandResultListener = onCommandResultListener;
            this.onCommandLineListener = onCommandLineListener;
            this.marker = UUID.randomUUID().toString() + String.format("-%08x", ++commandCounter);
        }
    }

    /**
     * Builder class for {@link Interactive}
     */
    public static class Builder {
        private Handler handler = null;
        private boolean autoHandler = true;
        private String shell = "sh";
        private boolean wantSTDERR = false;
        private List<Command> commands = new LinkedList<>();
        private Map<String, String> environment;
        private StreamGobbler.OnLineListener onSTDOUTLineListener = null;
        private StreamGobbler.OnLineListener onSTDERRLineListener = null;
        private int watchdogTimeout = 0;

        /**
         * <p>
         * Set a custom handler that will be used to post all callbacks to
         * </p>
         * <p>
         * See {@link Interactive} for further details on threading and
         * handlers
         * </p>
         *
         * @param handler Handler to use
         * @return This Builder object for method chaining
         */
        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * <p>
         * Automatically create a handler if possible ? Default to true
         * </p>
         * <p>
         * See {@link Interactive} for further details on threading and
         * handlers
         * </p>
         *
         * @param autoHandler Auto-create handler ?
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder setAutoHandler(boolean autoHandler) {
            this.autoHandler = autoHandler;
            return this;
        }

        /**
         * Set shell binary to use. Usually "sh" or "su", do not use a full path
         * unless you have a good reason to
         *
         * @param shell Shell to use
         * @return This Builder object for method chaining
         */
        public Builder setShell(String shell) {
            this.shell = shell;
            return this;
        }

        /**
         * Convenience function to set "sh" as used shell
         *
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder useSH() {
            return setShell("sh");
        }

        /**
         * Convenience function to set "su" as used shell
         *
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder useSU() {
            return setShell("su");
        }

        /**
         * Set if error output should be appended to command block result output
         *
         * @param wantSTDERR Want error output ?
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder setWantSTDERR(boolean wantSTDERR) {
            this.wantSTDERR = wantSTDERR;
            return this;
        }

        /**
         * Add or update an environment variable
         *
         * @param key   Key of the environment variable
         * @param value Value of the environment variable
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addEnvironment(String key, String value) {
            if (environment == null) {
                environment = new HashMap<>();
            }
            environment.put(key, value);
            return this;
        }

        /**
         * Add or update environment variables
         *
         * @param addEnvironment Map of environment variables
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addEnvironment(Map<String, String> addEnvironment) {
            if (environment == null) {
                environment = new HashMap<>();
            }
            environment.putAll(addEnvironment);
            return this;
        }

        /**
         * Add a command to execute
         *
         * @param command Command to execute
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addCommand(String command) {
            return addCommand(command, 0, null);
        }

        /**
         * <p>
         * Add a command to execute, with a callback to be called on completion
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param command                 Command to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder addCommand(String command, int code,
                                  OnCommandResultListener onCommandResultListener) {
            return addCommand(new String[]{
                    command
            }, code, onCommandResultListener);
        }

        /**
         * Add commands to execute
         *
         * @param commands Commands to execute
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addCommand(List<String> commands) {
            return addCommand(commands, 0, null);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         *                                (of all commands)
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder addCommand(List<String> commands, int code,
                                  OnCommandResultListener onCommandResultListener) {
            return addCommand(commands.toArray(new String[commands.size()]), code,
                    onCommandResultListener);
        }

        /**
         * Add commands to execute
         *
         * @param commands Commands to execute
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder addCommand(String[] commands) {
            return addCommand(commands, 0, null);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         *                                (of all commands)
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder addCommand(String[] commands, int code,
                                  OnCommandResultListener onCommandResultListener) {
            this.commands.add(new Command(commands, code, onCommandResultListener, null));
            return this;
        }

        /**
         * <p>
         * Set a callback called for every line output to STDOUT by the shell
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param onLineListener Callback to be called for each line
         * @return This Builder object for method chaining
         */
        @SuppressWarnings({"unused", "WeakerAccess"})
        public Builder setOnSTDOUTLineListener(StreamGobbler.OnLineListener onLineListener) {
            this.onSTDOUTLineListener = onLineListener;
            return this;
        }

        /**
         * <p>
         * Set a callback called for every line output to STDERR by the shell
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param onLineListener Callback to be called for each line
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder setOnSTDERRLineListener(StreamGobbler.OnLineListener onLineListener) {
            this.onSTDERRLineListener = onLineListener;
            return this;
        }

        /**
         * <p>
         * Enable command timeout callback
         * </p>
         * <p>
         * This will invoke the onCommandResult() callback with exitCode
         * WATCHDOG_EXIT if a command takes longer than watchdogTimeout seconds
         * to complete.
         * </p>
         * <p>
         * If a watchdog timeout occurs, it generally means that the Interactive
         * session is out of sync with the shell process. The caller should
         * close the current session and open a new one.
         * </p>
         *
         * @param watchdogTimeout Timeout, in seconds; 0 to disable
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("WeakerAccess")
        public Builder setWatchdogTimeout(int watchdogTimeout) {
            this.watchdogTimeout = watchdogTimeout;
            return this;
        }

        /**
         * <p>
         * Enable/disable reduced logcat output
         * </p>
         * <p>
         * Note that this is a global setting
         * </p>
         *
         * @param useMinimal true for reduced output, false for full output
         * @return This Builder object for method chaining
         */
        @SuppressWarnings("unused")
        public Builder setMinimalLogging(boolean useMinimal) {
            return this;
        }

        /**
         * Construct a {@link Interactive} instance, and start the shell
         *
         * @return Interactive shell
         */
        public Interactive open() {
            return new Interactive(this, null);
        }

        /**
         * Construct a {@link Interactive} instance, try to start the
         * shell, and call onCommandResultListener to report success or failure
         *
         * @param onCommandResultListener Callback to return shell open status
         * @return Interactive shell
         */
        @SuppressWarnings("JavaDoc")
        public Interactive open(OnCommandResultListener onCommandResultListener) {
            return new Interactive(this, onCommandResultListener);
        }
    }

    /**
     * <p>
     * An interactive shell - initially created with {@link Builder} -
     * that executes blocks of commands you supply in the background, optionally
     * calling callbacks as each block completes.
     * </p>
     * <p>
     * STDERR output can be supplied as well, but due to compatibility with
     * older Android versions, wantSTDERR is not implemented using
     * redirectErrorStream, but rather appended to the output. STDOUT and STDERR
     * are thus not guaranteed to be in the correct order in the output.
     * </p>
     * <p>
     * Note as well that the close() and waitForIdle() methods will
     * intentionally crash when run in debug mode from the main thread of the
     * application. Any blocking call should be run from a background thread.
     * </p>
     * <p>
     * When in debug mode, the code will also excessively log the commands
     * passed to and the output returned from the shell.
     * </p>
     * <p>
     * Though this function uses background threads to gobble STDOUT and STDERR
     * so a deadlock does not occur if the shell produces massive output, the
     * output is still stored in a List&lt;String&gt;, and as such doing
     * something like <em>'ls -lR /'</em> will probably have you run out of
     * memory when using a {@link OnCommandResultListener}. A work-around
     * is to not supply this callback, but using (only)
     * {@link Builder#setOnSTDOUTLineListener(StreamGobbler.OnLineListener)}. This way,
     * an internal buffer will not be created and wasting your memory.
     * </p>
     * <h3>Callbacks, threads and handlers</h3>
     * <p>
     * On which thread the callbacks execute is dependent on your
     * initialization. You can supply a custom Handler using
     * {@link Builder#setHandler(Handler)} if needed. If you do not supply
     * a custom Handler - unless you set
     * {@link Builder#setAutoHandler(boolean)} to false - a Handler will
     * be auto-created if the thread used for instantiation of the object has a
     * Looper.
     * </p>
     * <p>
     * If no Handler was supplied and it was also not auto-created, all
     * callbacks will be called from either the STDOUT or STDERR gobbler
     * threads. These are important threads that should be blocked as little as
     * possible, as blocking them may in rare cases pause the native process or
     * even create a deadlock.
     * </p>
     * <p>
     * The main thread must certainly have a Looper, thus if you call
     * {@link Builder#open()} from the main thread, a handler will (by
     * default) be auto-created, and all the callbacks will be called on the
     * main thread. While this is often convenient and easy to code with, you
     * should be aware that if your callbacks are 'expensive' to execute, this
     * may negatively impact UI performance.
     * </p>
     * <p>
     * Background threads usually do <em>not</em> have a Looper, so calling
     * {@link Builder#open()} from such a background thread will (by
     * default) result in all the callbacks being executed in one of the gobbler
     * threads. You will have to make sure the code you execute in these
     * callbacks is thread-safe.
     * </p>
     */
    @SuppressWarnings({"WeakerAccess", "JavadocReference"})
    public static class Interactive {
        private final Handler handler;
        private final boolean autoHandler;
        private final String shell;
        private final boolean wantSTDERR;
        private final List<Command> commands;
        private final Map<String, String> environment;
        private final StreamGobbler.OnLineListener onSTDOUTLineListener;
        private final StreamGobbler.OnLineListener onSTDERRLineListener;
        private int watchdogTimeout;

        private volatile Process process = null;
        private DataOutputStream STDIN = null;
        private StreamGobbler STDOUT = null;
        private StreamGobbler STDERR = null;
        private ScheduledThreadPoolExecutor watchdog = null;

        private volatile boolean running = false;
        private volatile boolean idle = true; // read/write only synchronized
        @SuppressWarnings("unused")
        private volatile boolean closed = true;
        private volatile int callbacks = 0;
        private volatile int watchdogCount;

        private final Object idleSync = new Object();
        private final Object callbackSync = new Object();

        private volatile int lastExitCode = 0;
        private volatile String lastMarkerSTDOUT = null;
        private volatile String lastMarkerSTDERR = null;
        private volatile Command command = null;
        private volatile List<String> buffer = null;

        /**
         * The only way to create an instance: Shell.Builder::open()
         *
         * @param builder Builder class to take values from
         */
        @SuppressWarnings("JavaDoc")
        private Interactive(final Builder builder,
                            final OnCommandResultListener onCommandResultListener) {
            autoHandler = builder.autoHandler;
            shell = builder.shell;
            wantSTDERR = builder.wantSTDERR;
            commands = builder.commands;
            environment = builder.environment;
            onSTDOUTLineListener = builder.onSTDOUTLineListener;
            onSTDERRLineListener = builder.onSTDERRLineListener;
            watchdogTimeout = builder.watchdogTimeout;

            // If a looper is available, we offload the callbacks from the
            // gobbling threads
            // to whichever thread created us. Would normally do this in open(),
            // but then we could not declare handler as final
            if ((Looper.myLooper() != null) && (builder.handler == null) && autoHandler) {
                handler = new Handler();
            } else {
                handler = builder.handler;
            }

            if (onCommandResultListener != null) {
                // Allow up to 60 seconds for SuperSU/Superuser dialog, then enable
                // the user-specified timeout for all subsequent operations
//                watchdogTimeout = 60;
                if (watchdogTimeout == 0) {
                    watchdogTimeout = WATCH_DOG_TIME_OUT;
                }

                commands.add(0, new Command(Shell.availableTestCommands, 0, (commandCode, exitCode, output) -> {
                    if ((exitCode == OnCommandResultListener.SHELL_RUNNING) &&
                            !Shell.parseAvailableResult(output, SU.isSU(shell))) {
                        // shell is up, but it's brain-damaged
                        exitCode = OnCommandResultListener.SHELL_WRONG_UID;
                    }
//                        watchdogTimeout = builder.watchdogTimeout;
                    onCommandResultListener.onCommandResult(0, exitCode, output);
                }, null));
            }

            boolean open = open();
            LogUtil.i("open -> " + open);
            if (!open && (onCommandResultListener != null)) {
                LogUtil.i("root进程没有成功打开");
                onCommandResultListener.onCommandResult(0,
                        OnCommandResultListener.SHELL_EXEC_FAILED, null);
            }
        }

        /**
         * <p>
         * Add a command to execute, with a callback to be called on completion
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param command                 Command to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         */
        public void addCommand(String command, int code,
                               OnCommandResultListener onCommandResultListener) {
            addCommand(new String[]{
                    command
            }, code, onCommandResultListener);
        }

        /**
         * <p>
         * Add commands to execute, with a callback to be called on completion
         * (of all commands)
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Interactive} for further details
         * </p>
         *
         * @param commands                Commands to execute
         * @param code                    User-defined value passed back to the callback
         * @param onCommandResultListener Callback to be called on completion
         *                                (of all commands)
         */
        @SuppressWarnings("WeakerAccess")
        public synchronized void addCommand(String[] commands, int code,
                                            OnCommandResultListener onCommandResultListener) {
            this.commands.add(new Command(commands, code, onCommandResultListener, null));
            runNextCommand();
        }

        /**
         * Run the next command if any and if ready, signals idle state if no
         * commands left
         */
        private void runNextCommand() {
            runNextCommand(true);
        }

        /**
         * Start the periodic timer when a command is submitted
         *
         * @param countDownLatch
         */
        @SuppressWarnings("JavaDoc")
        private void startWatchdog() {
            if (watchdogTimeout == 0) {
                return;
            }
            watchdogCount = 0;
            watchdog = new ScheduledThreadPoolExecutor(1);
            watchdog.scheduleAtFixedRate(this::handleWatchdog, 0, 1, TimeUnit.SECONDS);
        }

        /**
         * Called from a ScheduledThreadPoolExecutor timer thread every second
         * when there is an outstanding command
         *
         * @param countDownLatch
         */
        @SuppressWarnings("JavaDoc")
        private synchronized void handleWatchdog() {
            if (watchdog == null) {
                return;
            }
            if (watchdogTimeout == 0) {
                return;
            }

            final int exitCode;
            if (!isRunning()) {
                exitCode = OnCommandResultListener.SHELL_DIED;
            } else if (watchdogCount++ < watchdogTimeout) {
                return;
            } else {
                exitCode = OnCommandResultListener.WATCHDOG_EXIT;
            }

            postCallback(command, exitCode, buffer);

            // prevent multiple callbacks for the same command
            endWatchdog();
        }

        private void endWatchdog() {
            ThreadManager.post(() -> {
//                command = null;
//                buffer = null;
//                idle = true;

                if (watchdog != null) {
                    synchronized (Interactive.this) {
                        if (watchdog != null) {
                            watchdog.shutdown();
                            watchdog = null;

                            kill();
                        }
                    }
                }
            });
        }

        /**
         * Disable the watchdog timer upon command completion
         */
        private void stopWatchdog() {
            if (watchdog != null) {
                watchdog.shutdownNow();
                watchdog = null;
            }
        }

        /**
         * Run the next command if any and if ready
         *
         * @param notifyIdle signals idle state if no commands left ?
         */
        @SuppressWarnings("TryWithIdenticalCatches")
        private void runNextCommand(boolean notifyIdle) {
            // must always be called from a synchronized method
            boolean running = isRunning();
            if (!running)
                idle = true;

            for (int i = 0; i < commands.size(); i++) {
                LogUtil.i("Arrays.toString(commands.get(i).commands) -> " + Arrays.toString(commands.get(i).commands));
            }

            if (running && idle && (commands.size() > 0)) {
                Command command = commands.get(0);
                commands.remove(0);

                buffer = null;
                lastExitCode = 0;
                lastMarkerSTDOUT = null;
                lastMarkerSTDERR = null;

                if (command.commands.length > 0) {
                    try {
                        if (command.onCommandResultListener != null) {
                            // no reason to store the output if we don't have an
                            // OnCommandResultListener
                            // user should catch the output with an
                            // OnLineListener in this case
                            buffer = Collections.synchronizedList(new ArrayList<String>());
                        }

                        idle = false;
                        this.command = command;
                        if (Arrays.asList(command.commands).contains("echo -BOC-")) {
                            // 执行su命令的时候,如果root管理应用是询问模式,当手机已经root时,要么弹出root授权对话框,
                            // 要么超时.当手机没有root时,会抛出异常.超时就需要做相应处理.
                            LogUtil.i("执行su命令");
//                            startWatchdog();
                        }
                        LogUtil.i("Thread.currentThread().getName() -> " + Thread.currentThread().getName());
                        for (String write : command.commands) {
                            STDIN.write((write + "\n").getBytes("UTF-8"));
                        }
                        STDIN.write(("echo " + command.marker + " $?\n").getBytes("UTF-8"));
                        STDIN.write(("echo " + command.marker + " >&2\n").getBytes("UTF-8"));
                        STDIN.flush();
                    } catch (IOException e) {
                        // STDIN might have closed
                        LogUtil.i("e -> " + e);
                    } catch (Exception e) {
                        LogUtil.i("e -> " + e);
                    }
                } else {
                    runNextCommand(false);
                }
            } else if (!running) {
                // our shell died for unknown reasons - abort all submissions
                while (commands.size() > 0) {
                    postCallback(commands.remove(0), OnCommandResultListener.SHELL_DIED, null);
                }
            }

            if (idle && notifyIdle) {
                synchronized (idleSync) {
                    idleSync.notifyAll();
                }
            }
        }

        /**
         * Processes a STDOUT/STDERR line containing an end/exitCode marker
         */
        private synchronized void processMarker() {
            if (command.marker.equals(lastMarkerSTDOUT)
                    && (command.marker.equals(lastMarkerSTDERR))) {
                postCallback(command, lastExitCode, buffer);
                stopWatchdog();
                command = null;
                buffer = null;
                idle = true;
                runNextCommand();
            }
        }

        /**
         * Process a normal STDOUT/STDERR line
         *
         * @param line     Line to process
         * @param listener Callback to call or null
         */
        private synchronized void processLine(String line, StreamGobbler.OnLineListener listener) {
            if (listener != null) {
                if (handler != null) {
                    final String fLine = line;
                    final StreamGobbler.OnLineListener fListener = listener;

                    startCallback();
                    handler.post(() -> {
                        try {
                            fListener.onLine(fLine);
                        } finally {
                            endCallback();
                        }
                    });
                } else {
                    listener.onLine(line);
                }
            }
        }

        /**
         * Add line to internal buffer
         *
         * @param line Line to add
         */
        private synchronized void addBuffer(String line) {
            if (buffer != null) {
                buffer.add(line);
            }
        }

        /**
         * Increase callback counter
         */
        private void startCallback() {
            synchronized (callbackSync) {
                callbacks++;
            }
        }

        /**
         * Schedule a callback to run on the appropriate thread
         */
        private void postCallback(final Command fCommand, final int fExitCode,
                                  final List<String> fOutput) {
            if (fCommand.onCommandResultListener == null && fCommand.onCommandLineListener == null) {
                return;
            }
            if (handler == null) {
                if (fCommand.onCommandResultListener != null)
                    fCommand.onCommandResultListener.onCommandResult(fCommand.code, fExitCode,
                            fOutput);
                if (fCommand.onCommandLineListener != null)
                    fCommand.onCommandLineListener.onCommandResult(fCommand.code, fExitCode);
                return;
            }
            startCallback();
            //@note 此处有一bug：当handler为执行命令的线程，因watchdog超时会将process关闭，导致
            //handler post的runnable没有执行，从而无法回调用户
            handler.post(() -> {
                try {
                    if (fCommand.onCommandResultListener != null)
                        fCommand.onCommandResultListener.onCommandResult(fCommand.code,
                                fExitCode, fOutput);
                    if (fCommand.onCommandLineListener != null)
                        fCommand.onCommandLineListener
                                .onCommandResult(fCommand.code, fExitCode);
                } finally {
                    endCallback();
                }
            });
        }

        /**
         * Decrease callback counter, signals callback complete state when
         * dropped to 0
         */
        private void endCallback() {
            synchronized (callbackSync) {
                callbacks--;
                if (callbacks == 0) {
                    callbackSync.notifyAll();
                }
            }
        }

        /**
         * Internal call that launches the shell, starts gobbling, and starts
         * executing commands. See {@link Interactive}
         *
         * @return Opened successfully ?
         */
        private synchronized boolean open() {
            try {
                // setup our process, retrieve STDIN stream, and STDOUT/STDERR
                // gobblers
                LogUtil.i("open()");
                LogUtil.i("RootUtil.isOnMainThread() -> " + RootUtil.isOnMainThread());

                // 在samsung sm-g9200手机上,root权限管理软件为kingroot,设置询问模式,不执行以下代码,当前将会阻塞,
                // 必须要休眠才可以,而且数字小了也不行,经过多次测试,500毫秒是最佳值了.
//                SystemClock.sleep(500);

                // 暂时保留,上线前删除.
//                if (environment.size() == 0) {
//                    process = Runtime.getRuntime().exec(shell);
//                } else {
//                    Map<String, String> newEnvironment = new HashMap<>();
//                    newEnvironment.putAll(System.getenv());
//                    newEnvironment.putAll(environment);
//                    int i = 0;
//                    String[] env = new String[newEnvironment.size()];
//                    for (Map.Entry<String, String> entry : newEnvironment.entrySet()) {
//                        env[i] = entry.getKey() + "=" + entry.getValue();
//                        i++;
//                    }
//                    process = Runtime.getRuntime().exec(shell, env);
//                }
                Map<String, String> newEnvironment = new HashMap<>();
                newEnvironment.putAll(System.getenv());
                if (environment != null) {
                    if (environment.size() != 0) {
                        newEnvironment.putAll(environment);
                    }
                }
                int i = 0;
                String[] env = new String[newEnvironment.size()];
                for (Map.Entry<String, String> entry : newEnvironment.entrySet()) {
                    env[i] = entry.getKey() + "=" + entry.getValue();
                    i++;
                }

                if (process != null) {
                    process = null;
                }
                process = Runtime.getRuntime().exec(shell, env);
//                process = Runtime.getRuntime().exec(shell);

                STDIN = new DataOutputStream(process.getOutputStream());
                STDOUT = new StreamGobbler(process.getInputStream(), line -> {
                    synchronized (Interactive.this) {
                        if (command == null) {
                            return;
                        }

                        String contentPart = line;
                        String markerPart = null;

                        int markerIndex = line.indexOf(command.marker);
                        if (markerIndex == 0) {
                            contentPart = null;
                            markerPart = line;
                        } else if (markerIndex > 0) {
                            contentPart = line.substring(0, markerIndex);
                            markerPart = line.substring(markerIndex);
                        }

                        LogUtil.i("out line -> " + line);
//                        LogUtil.i("out contentPart -> " + contentPart);
//                        LogUtil.i("out markerPart -> " + markerPart);

                        if (contentPart != null) {
                            addBuffer(contentPart);
                            processLine(contentPart, onSTDOUTLineListener);
                            processLine(contentPart, command.onCommandLineListener);
                        }

                        if (markerPart != null) {
                            try {
                                lastExitCode = Integer.valueOf(
                                        markerPart.substring(command.marker.length() + 1), 10);
                            } catch (Exception e) {
                                // this really shouldn't happen
                                e.printStackTrace();
                            }
                            lastMarkerSTDOUT = command.marker;
                            processMarker();
                        }
                    }
                });
                STDERR = new StreamGobbler(process.getErrorStream(), line -> {
                    synchronized (Interactive.this) {
                        if (command == null) {
                            return;
                        }

                        String contentPart = line;

                        int markerIndex = line.indexOf(command.marker);
                        if (markerIndex == 0) {
                            contentPart = null;
                        } else if (markerIndex > 0) {
                            contentPart = line.substring(0, markerIndex);
                        }

                        LogUtil.i("error line -> " + line);
//                        LogUtil.i("error contentPart -> " + contentPart);

                        if (contentPart != null) {
                            if (wantSTDERR)
                                addBuffer(contentPart);
                            processLine(contentPart, onSTDERRLineListener);
                        }

                        if (markerIndex >= 0) {
                            lastMarkerSTDERR = command.marker;
                            processMarker();
                        }
                    }
                });

                // start gobbling and write our commands to the shell
                STDOUT.start();
                STDERR.start();

                running = true;
                closed = false;

                runNextCommand();

                return true;
            } catch (IOException e) {
                // shell probably not found
                return false;
            }
        }

        /**
         * Close shell and clean up all resources. Call this when you are done
         * with the shell. If the shell is not idle (all commands completed) you
         * should not call this method from the main UI thread because it may
         * block for a long time. This method will intentionally crash your app
         * (if in debug mode) if you try to do this anyway.
         */
        @SuppressWarnings("TryWithIdenticalCatches")
        public void close() {
            boolean _idle = isIdle(); // idle must be checked synchronized

            synchronized (this) {
                if (!running)
                    return;
                running = false;
                closed = true;
            }

            // This method should not be called from the main thread unless the
            // shell is idle and can be cleaned up with (minimal) waiting. Only
            // throw in debug mode.
            if (RootUtil.isOnMainThread()) {
                throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_NOT_IDLE);
            }

            if (!_idle)
                waitForIdle();

            try {
                try {
                    STDIN.write(("exit\n").getBytes("UTF-8"));
                    STDIN.flush();
                } catch (IOException e) {
                    if (e.getMessage().contains("EPIPE")) {
                        // we're not running a shell, the shell closed STDIN,
                        // the script already contained the exit command, etc.
                    } else {
                        throw e;
                    }
                }

                // wait for our process to finish, while we gobble away in the
                // background
                process.waitFor();

                // make sure our threads are done gobbling, our streams are
                // closed, and the process is destroyed - while the latter two
                // shouldn't be needed in theory, and may even produce warnings,
                // in "normal" Java they are required for guaranteed cleanup of
                // resources, so lets be safe and do this on Android as well
                try {
                    STDIN.close();
                } catch (IOException e) {
                    // STDIN going missing is no reason to abort
                }
                STDOUT.join();
                STDERR.join();
                stopWatchdog();
                process.destroy();
            } catch (IOException e) {
                // various unforseen IO errors may still occur
            } catch (InterruptedException e) {
                // this should really be re-thrown
            }
        }

        /**
         * Try to clean up as much as possible from a shell that's gotten itself
         * wedged. Hopefully the StreamGobblers will croak on their own when the
         * other side of the pipe is closed.
         */
        @SuppressWarnings("WeakerAccess")
        public void kill() {
            running = false;
            closed = true;

            try {
                STDIN.close();
            } catch (IOException e) {
                // in case it was closed
            }
            try {
                process.destroy();
                process = null;
            } catch (Exception e) {
                // in case it was already destroyed or can't be
            }

            idle = true;
            synchronized (idleSync) {
                idleSync.notifyAll();
            }
        }

        /**
         * Is our shell still running ?
         *
         * @return Shell running ?
         */
        public boolean isRunning() {
            if (process == null) {
                return false;
            }
            try {
                process.exitValue();
                return false;
            } catch (IllegalThreadStateException e) {
                // if this is thrown, we're still running
            }
            return true;
        }

        /**
         * Have all commands completed executing ?
         *
         * @return Shell idle ?
         */
        @SuppressWarnings("WeakerAccess")
        public synchronized boolean isIdle() {
            if (!isRunning()) {
                idle = true;
                synchronized (idleSync) {
                    idleSync.notifyAll();
                }
            }
            return idle;
        }

        /**
         * <p>
         * Wait for idle state. As this is a blocking call, you should not call
         * it from the main UI thread. If you do so and debug mode is enabled,
         * this method will intentionally crash your app.
         * </p>
         * <p>
         * If not interrupted, this method will not return until all commands
         * have finished executing. Note that this does not necessarily mean
         * that all the callbacks have fired yet.
         * </p>
         * <p>
         * If no Handler is used, all callbacks will have been executed when
         * this method returns. If a Handler is used, and this method is called
         * from a different thread than associated with the Handler's Looper,
         * all callbacks will have been executed when this method returns as
         * well. If however a Handler is used but this method is called from the
         * same thread as associated with the Handler's Looper, there is no way
         * to know.
         * </p>
         * <p>
         * In practice this means that in most simple cases all callbacks will
         * have completed when this method returns, but if you actually depend
         * on this behavior, you should make certain this is indeed the case.
         * </p>
         * <p>
         * See {@link Interactive} for further details on threading and
         * handlers
         * </p>
         *
         * @return True if wait complete, false if wait interrupted
         */
        @SuppressWarnings("WeakerAccess")
        public boolean waitForIdle() {
            if (RootUtil.isOnMainThread()) {
                throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_WAIT_IDLE);
            }

            if (isRunning()) {
                synchronized (idleSync) {
                    while (!idle) {
                        try {
                            idleSync.wait();
                        } catch (InterruptedException e) {
                            return false;
                        }
                    }
                }

                if ((handler != null) &&
                        (handler.getLooper() != null) &&
                        (handler.getLooper() != Looper.myLooper())) {
                    // If the callbacks are posted to a different thread than
                    // this one, we can wait until all callbacks have called
                    // before returning. If we don't use a Handler at all, the
                    // callbacks are already called before we get here. If we do
                    // use a Handler but we use the same Looper, waiting here
                    // would actually block the callbacks from being called

                    synchronized (callbackSync) {
                        while (callbacks > 0) {
                            try {
                                callbackSync.wait();
                            } catch (InterruptedException e) {
                                return false;
                            }
                        }
                    }
                }
            }

            return true;
        }

        /**
         * Are we using a Handler to post callbacks ?
         *
         * @return Handler used ?
         */
        @SuppressWarnings("unused")
        public boolean hasHandler() {
            return (handler != null);
        }
    }

}
