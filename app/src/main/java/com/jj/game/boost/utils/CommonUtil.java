package com.jj.game.boost.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.ArrayMap;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jj.game.boost.JJBoostApplication;
import com.jj.game.boost.domain.ProcessInfo;
import com.jj.game.boost.traffic.TrafficManagerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommonUtil {

    @SuppressWarnings("FieldCanBeLocal")
    private static int EXCESS_VALUE = 20 * 1024;

    @SuppressWarnings("FieldCanBeLocal")
    private static int RUNNING_SERVICE_COUNT = 120;

    public static <T> List<T> listInit(List<T> list) {
        if (list == null) {
            list = new ArrayList<>();
        } else {
            if (list.size() > 0) {
                list.clear();
            }
        }
        return list;
    }

    public static <T> void listAdd(List<T> list, T t) {
        if (!list.contains(t)) {
            list.add(t);
        }
    }

    public static <T> void listRemove(List<T> list, T t) {
        if (list.contains(t)) {
            list.remove(t);
        }
    }

    @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
    public static Activity obtainTopActivity() {
        Activity topActivity = null;
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method getATMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            Object activityThread = getATMethod.invoke(null);
            activitiesField.setAccessible(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ArrayMap activites = (ArrayMap) activitiesField.get(activityThread);
                if (activites == null || activites.size() == 0) {
                    return null;
                }
                Object activityClientRecord = activites.valueAt(0);

                Class activityClientRecordClass = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                topActivity = (Activity) activityField.get(activityClientRecord);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
//        LogUtil.i("topActivity -> " + topActivity);
        return topActivity;
    }

    /**
     * 获取所有运行中的app的进程信息
     *
     * @param context
     * @param packageNames
     * @return
     */
    @SuppressWarnings({"JavaDoc", "WeakerAccess", "deprecation"})
    public static List<Object> obtainCurrentProcessInfo(Context context, String[] packageNames) {
        List<Object> result;
        List<ProcessInfo> processInfos;
        List<ProcessInfo> excessProcessInfos = null;
        long totalNetworkSpeed = 0;
        ProcessInfo processInfo;
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT < 24) {
                List<AndroidAppProcess> androidAppProcesses;
                androidAppProcesses = ProcessManager.getRunningAppProcesses();
                if (androidAppProcesses == null) {
                    return null;
                }
                AndroidAppProcess androidAppProcess;
                int size = androidAppProcesses.size();
                if (size == 0) {
                    return null;
                }
                String packageNameTemp;
                processInfos = new ArrayList<>();
                excessProcessInfos = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    androidAppProcess = androidAppProcesses.get(i);
                    if (androidAppProcess == null) {
                        continue;
                    }

                    packageNameTemp = androidAppProcess.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName : packageNames) {
                            if (packageNameTemp.equals(packageName)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{androidAppProcess.pid});

//                    LogUtil.i("------");
//                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
//                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    LogUtil.i("androidAppProcess.pid -> " + androidAppProcess.pid);
//                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
//                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
//                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
//                    LogUtil.i("enabledSetting -> " + enabledSetting);
//                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
//                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
//                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
//                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    processInfo = new ProcessInfo();
                    processInfo.setPackageName(packageNameTemp);
//                    processInfo.setProcessName(applicationInfo.processName);
//                    processInfo.setPid(androidAppProcess.pid);
//                    processInfo.setUid(applicationInfo.uid);
                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
//                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
//                    processInfo.setSystemApp(false);
                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
                            .getUidRxBytes(context, applicationInfo.uid);
                    Long networkSpeed = JJBoostApplication.application.mNetworkSpeeds.get(packageNameTemp);
                    if (networkSpeed != null) {
                        long temp = uidRxBytes - networkSpeed;
                        if (temp < 0) {
                            temp = Math.abs(temp);
                        }
                        processInfo.setNetworkSpeed(temp);
                    } else {
                        processInfo.setNetworkSpeed(0L);
                    }
                    if (!processInfos.contains(processInfo)) {
                        processInfos.add(processInfo);
                        networkSpeed = processInfo.getNetworkSpeed();
                        totalNetworkSpeed += networkSpeed;
                        if (networkSpeed >= EXCESS_VALUE) {
                            excessProcessInfos.add(processInfo);
                        }
                        JJBoostApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
                    }
                }
            } else {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(RUNNING_SERVICE_COUNT);
                if (runningServices == null) {
                    return null;
                }
                ActivityManager.RunningServiceInfo runningServiceInfo;
                int size = runningServices.size();
                LogUtil.i("运行中的服务的数量 -> " + size);
                if (size == 0) {
                    return null;
                }
                String packageNameTemp;
                processInfos = new ArrayList<>();
                excessProcessInfos = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    runningServiceInfo = runningServices.get(i);
                    if (runningServiceInfo == null) {
                        continue;
                    }

                    packageNameTemp = runningServiceInfo.service.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName : packageNames) {
                            if (packageNameTemp.equals(packageName)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningServiceInfo.pid});

//                    LogUtil.i("------");
//                    LogUtil.i("packageNameTemp -> " + packageNameTemp);
//                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    LogUtil.i("runningServiceInfo.pid -> " + runningServiceInfo.pid);
//                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
//                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
//                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
//                    LogUtil.i("enabledSetting -> " + enabledSetting);
//                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
//                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
//                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
//                            packageNameTemp) == PackageManager.PERMISSION_GRANTED;
//                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    processInfo = new ProcessInfo();
                    processInfo.setPackageName(packageNameTemp);
//                    processInfo.setProcessName(applicationInfo.processName);
//                    processInfo.setPid(runningServiceInfo.pid);
//                    processInfo.setUid(applicationInfo.uid);
                    processInfo.setAppIcon(applicationInfo.loadLogo(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
//                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
                    processInfo.setSystemApp(false);
                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
                            .getUidRxBytes(context, applicationInfo.uid);
                    Long networkSpeed = JJBoostApplication.application.mNetworkSpeeds.get(packageNameTemp);
                    if (networkSpeed != null) {
                        long temp = uidRxBytes - networkSpeed;
                        if (temp < 0) {
                            temp = Math.abs(temp);
                        }
                        processInfo.setNetworkSpeed(temp);
                    } else {
                        processInfo.setNetworkSpeed(0L);
                    }
                    if (!processInfos.contains(processInfo)) {
                        processInfos.add(processInfo);
                        networkSpeed = processInfo.getNetworkSpeed();
                        totalNetworkSpeed += networkSpeed;
                        if (networkSpeed >= EXCESS_VALUE) {
                            excessProcessInfos.add(processInfo);
                        }
                        JJBoostApplication.application.mNetworkSpeeds.put(packageNameTemp, uidRxBytes);
                    }
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            String[] arr;
            int size = runningAppProcesses.size();
            if (size == 0) {
                return null;
            }
            processInfos = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                runningAppProcessInfo = runningAppProcesses.get(i);
                if (runningAppProcessInfo == null) {
                    continue;
                }
                arr = runningAppProcessInfo.pkgList;
                if (arr == null || arr.length == 0) {
                    continue;
                }

//                Debug.MemoryInfo[] processMemoryInfos = activityManager.getProcessMemoryInfo(new int[]{runningAppProcessInfo.pid});

                processInfo = new ProcessInfo();
                excessProcessInfos = new ArrayList<>();
                boolean iscontain;
                for (String anArr : arr) {
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
//                    if (anArr.contains("setting") || anArr.contains("launcher")) {
//                        continue;
//                    }
                    iscontain = false;
                    if (packageNames != null) {
                        for (String packageName : packageNames) {
                            if (anArr.equals(packageName)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    try {
                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                        LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                    if (applicationInfo.uid == 1000) {
//                        continue;
//                    }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            anArr) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    LogUtil.i("------");
//                    LogUtil.i("anArr -> " + anArr);
//                    LogUtil.i("applicationInfo.processName -> " + applicationInfo.processName);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
//                    LogUtil.i("runningAppProcessInfo.pid -> " + runningAppProcessInfo.pid);
//                    LogUtil.i("applicationInfo.uid -> " + applicationInfo.uid);
//                    LogUtil.i("applicationInfo.enabled -> " + applicationInfo.enabled);
//                    int enabledSetting = (int) ReflectUtils.obtainNonStaticFieldValue(applicationInfo, "enabledSetting");
//                    LogUtil.i("enabledSetting -> " + enabledSetting);
//                    int flags = applicationInfo.flags & ApplicationInfo.FLAG_STOPPED;
//                    LogUtil.i("(flags==ApplicationInfo.FLAG_STOPPED) -> " + (flags == ApplicationInfo.FLAG_STOPPED));
//                    boolean hasInternetPermissions = packageManager.checkPermission(Manifest.permission.INTERNET,
//                            anArr) == PackageManager.PERMISSION_GRANTED;
//                    LogUtil.i("hasInternetPermissions -> " + hasInternetPermissions);

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    processInfo.setPackageName(anArr);
//                    processInfo.setProcessName(applicationInfo.processName);
//                    processInfo.setPid(runningAppProcessInfo.pid);
//                    processInfo.setUid(applicationInfo.uid);
                    processInfo.setAppIcon(applicationInfo.loadIcon(packageManager));
                    processInfo.setAppName(applicationLabel.toString());
//                    processInfo.setMemorySize((long) processMemoryInfos[0].getTotalPss());
                    processInfo.setSystemApp(false);
                    long uidRxBytes = TrafficManagerFactory.createTrafficManager(context)
                            .getUidRxBytes(context, applicationInfo.uid);
                    Long networkSpeed = JJBoostApplication.application.mNetworkSpeeds.get(anArr);
                    if (networkSpeed != null) {
                        long temp = uidRxBytes - networkSpeed;
                        if (temp < 0) {
                            temp = Math.abs(temp);
                        }
                        processInfo.setNetworkSpeed(temp);
                    } else {
                        processInfo.setNetworkSpeed(0L);
                    }
                    if (!processInfos.contains(processInfo)) {
                        processInfos.add(processInfo);
                        networkSpeed = processInfo.getNetworkSpeed();
                        totalNetworkSpeed += networkSpeed;
                        if (networkSpeed >= EXCESS_VALUE) {
                            excessProcessInfos.add(processInfo);
                        }
                        JJBoostApplication.application.mNetworkSpeeds.put(anArr, uidRxBytes);
                    }
                }
            }
        }
        if (processInfos.size() == 0) {
            return null;
        }

        // 不显示所有耗流量的app了,不需要再排序.
//        Collections.sort(processInfos);

        if (excessProcessInfos != null) {
            Collections.sort(excessProcessInfos);
        }
        result = new ArrayList<>();
        result.add(processInfos);
        result.add(totalNetworkSpeed);
        result.add(excessProcessInfos);
        return result;
    }

    @SuppressWarnings({"deprecation", "unused", "UnusedAssignment"})
    public static boolean isRunningApp(Context context, String packageName, String[] packageNames) {
        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT < 24) {
                List<AndroidAppProcess> androidAppProcesses = ProcessManager.getRunningAppProcesses();
                if (androidAppProcesses == null) {
                    return false;
                }
                AndroidAppProcess androidAppProcess;
                int size = androidAppProcesses.size();
                if (size == 0) {
                    return false;
                }
                String packageNameTemp;
                for (int i = 0; i < size; i++) {
                    androidAppProcess = androidAppProcesses.get(i);
                    if (androidAppProcess == null) {
                        continue;
                    }

                    packageNameTemp = androidAppProcess.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName2 : packageNames) {
                            if (packageNameTemp.equals(packageName2)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
                        applicationLabel = applicationInfo.loadLabel(packageManager);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    if (packageNameTemp.equals(packageName)) {
                        return true;
                    }
                }
            } else {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(RUNNING_SERVICE_COUNT);
                if (runningServices == null) {
                    return false;
                }
                ActivityManager.RunningServiceInfo runningServiceInfo;
                int size = runningServices.size();
                if (size == 0) {
                    return false;
                }
                String packageNameTemp;
                for (int i = 0; i < size; i++) {
                    runningServiceInfo = runningServices.get(i);
                    if (runningServiceInfo == null) {
                        continue;
                    }

                    packageNameTemp = runningServiceInfo.service.getPackageName();
                    if (packageNameTemp == null) {
                        continue;
                    }
//                    if (packageNameTemp.contains("setting") || packageNameTemp.contains("launcher")) {
//                        continue;
//                    }
                    boolean iscontain = false;
                    if (packageNames != null) {
                        for (String packageName2 : packageNames) {
                            if (packageNameTemp.equals(packageName2)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(packageNameTemp, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
//                    CharSequence applicationLabel = context.getPackageManager()
//                            .getApplicationLabel(context.getPackageManager()
//                                    .getApplicationInfo(packageNameTemp, 0));
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                        applicationLabel = applicationInfo.loadLabel(packageManager);
//                    LogUtil.i("applicationLabel -> " + applicationLabel);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                if (applicationInfo.uid == 1000) {
//                    continue;
//                }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            packageNameTemp) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    if (packageNameTemp.equals(packageName)) {
                        return true;
                    }
                }
            }
        } else {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            String[] arr;
            int size = runningAppProcesses.size();
            if (size == 0) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                runningAppProcessInfo = runningAppProcesses.get(i);
                if (runningAppProcessInfo == null) {
                    continue;
                }
                arr = runningAppProcessInfo.pkgList;
                if (arr == null || arr.length == 0) {
                    continue;
                }
                boolean iscontain;
                for (String anArr : arr) {
                    ApplicationInfo applicationInfo;
                    CharSequence applicationLabel;
//                    if (anArr.contains("setting") || anArr.contains("launcher")) {
//                        continue;
//                    }
                    iscontain = false;
                    if (packageNames != null) {
                        for (String packageName2 : packageNames) {
                            if (anArr.equals(packageName2)) {
                                iscontain = true;
                                break;
                            }
                        }
                    }
                    if (iscontain) {
                        continue;
                    }
                    try {
                        applicationInfo = packageManager.getApplicationInfo(anArr, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        continue;
                    }
                    try {
                        applicationLabel = applicationInfo.loadLabel(packageManager);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
//                    if (applicationInfo.uid == 1000) {
//                        continue;
//                    }
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                        continue;
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    if (packageManager.checkPermission(Manifest.permission.INTERNET,
                            anArr) == PackageManager.PERMISSION_DENIED) {
                        continue;
                    }

//                    String lowerCase = applicationLabel.toString().toLowerCase();
//                    if (lowerCase.contains("android系统") || lowerCase.contains("android 系统")) {
//                        continue;
//                    }

                    if (anArr.equals(packageName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 获取当前应用的主进程id
     *
     * @param context
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static int obtainCurrentMainProcessId(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return appProcess.pid;
            }
        }
        return -1;
    }

    /**
     * 跳转到当前应用的设置界面
     *
     * @param context
     */
    @SuppressWarnings("JavaDoc")
    public static void goToAppSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    @SuppressWarnings("unused")
    public static Typeface getSourceTypeFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSansCN-Regular.otf");
    }

    public static boolean isOnMainThread() {
        return ((Looper.myLooper() != null) && (Looper.myLooper() == Looper.getMainLooper()));
    }

    @SuppressWarnings("deprecation")
    public static boolean isInstallApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
