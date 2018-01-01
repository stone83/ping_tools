package com.jj.game.boost.domain;

import android.graphics.drawable.Drawable;

public class ProcessInfo implements Comparable<ProcessInfo> {
    private Drawable appIcon;
    private String appName;
    private String processName;
    private String packageName;
    private Integer pid;
    private Integer uid;
    private Long memorySize;
    private Boolean isChecked;
    private Boolean isSystemApp;
    private Boolean isLocked;
    private Long networkSpeed;

    public Boolean isLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packname) {
        this.packageName = packname;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Boolean isChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public Boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(Boolean systemApp) {
        isSystemApp = systemApp;
    }

    public Long getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(Long memorySize) {
        this.memorySize = memorySize;
    }

    public Long getNetworkSpeed() {
        return networkSpeed;
    }

    public void setNetworkSpeed(Long networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessInfo that = (ProcessInfo) o;

        return packageName.equals(that.packageName);

    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
    }

    @Override
    public int compareTo(ProcessInfo o) {
        return (int) (o.networkSpeed - networkSpeed);
    }
}
