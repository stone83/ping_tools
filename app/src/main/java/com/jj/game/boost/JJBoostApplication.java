
package com.jj.game.boost;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.ccmt.library.global.Global;
import com.jj.game.boost.dao.DaoMaster;
import com.jj.game.boost.dao.DaoSession;
import com.jj.game.boost.dao.DelayLostSaveDao;
import com.jj.game.boost.logtools.ILogRemoteService;
import com.jj.game.boost.logtools.LogService;
import com.jj.game.boost.utils.CommonUtil;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.PreferenceUtils;
import com.jj.game.boost.utils.SystemUtil;
import com.jj.game.boost.utils.ThreadManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JJBoostApplication extends Application {

    public static JJBoostApplication application;
    private static DaoSession daoSession;
    //    private List<Activity> allActivities;
    private RefWatcher mRefWatcher;
    public Handler mHandlerAccelerate = new Handler();
    public Map<String, Long> mNetworkSpeeds = new HashMap<>();
    public boolean mIsLoaded;
    private ServiceConnection sc = new MyServiceConnection();
    private LogService.MyBinder mBinder;
    private LogService mBindService;
    private ILogRemoteService mILogRemoteService;
    public LogService getmBindService(){
        return  mBindService;
    }
    public ILogRemoteService getmBindService2(){
        return  mILogRemoteService;
    }
    /**
     * 每个Activity和Fragment的onDestroy()方法被调用时调用该方法
     *
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static RefWatcher getRefWatcher() {
        return ((JJBoostApplication) application.getApplicationContext()).mRefWatcher;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

//        EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();

        if (LeakCanary.isInAnalyzerProcess(application)) {
            return;
        }
        mRefWatcher = LeakCanary.install(application);

        // 解决InputMethodManager类的内存泄露问题
        SystemUtil.fixMemoryLeak(this);

        //Tencent Bugly 初始化 第三个参数，测试阶段建议设置成true，发布时设置为false
        CrashReport.initCrashReport(getApplicationContext(), "a35ee3e22", false);

        // 由于有运行在其他进程的组件,所以如果当前运行的进程如果不是主进程,就不用再做初始化操作.
        int pid = CommonUtil.obtainCurrentMainProcessId(this);
        if (Process.myPid() != pid) {
            return;
        }

        //EventBus索引生成，只在主进程初始化一次
//        if (TextUtils.equals(CommonUtil.getCurrentProcessName(application), getPackageName())) {
//            EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
//        }

        LogUtil.i("JJBoostApplication onCreate()");

        Global.serializableFileDir = getFileStreamPath("Ser").getAbsolutePath();
        Global.serializableFileDirNotDelete = getFileStreamPath("SerNotDelete")
                .getAbsolutePath();
        // Global.serializableFileDir = getFilesDir().getAbsolutePath()
        // + File.separator + "Ser";
        // Global.serializableFileDirNotDelete = getFilesDir().getAbsolutePath()
        // + File.separator + "SerNotDelete";

        ThreadManager.startup();

        setupDatabase();
        excutebindService();
        if (!BuildConfig.IS_DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                // LogUtil.i("ex.getMessage() -> " + ex.getMessage());
                // LogUtil.i("ex.getLocalizedMessage() -> "
                // + ex.getLocalizedMessage());
                // LogUtil.i("ex.getStackTrace() -> "
                // + Arrays.toString(ex.getStackTrace()));

//                if (allActivities != null) {
//                    Iterator<Activity> ite = allActivities.iterator();
//                    while (ite.hasNext()) {
//                        ite.next().onBackPressed();
//                        ite.remove();
//                    }
//                    allActivities = null;
//                }

                if (Global.allRunningServices != null) {
                    Iterator<Class<? extends Service>> ite = Global.allRunningServices
                            .iterator();
                    while (ite.hasNext()) {
                        stopService(new Intent(application, ite.next()));
                        ite.remove();
                    }
                    Global.allRunningServices = null;
                }

                Process.killProcess(Process.myPid());
            });
        }

//        FileUtil.deleteDir(StorageUtil.getIncrementalUpdatingDir(application));

//        allActivities = new ArrayList<>();
    }

//    /**
//     * 每次进程启动时,都会调用该方法,而且在onCreate()方法之前被调用,主要用来dex突破65535的限制.
//     *
//     * @param base
//     */
//    @SuppressWarnings("JavaDoc")
//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//
//        if (getPackageName().equals(CommonUtil.getProcessName(this))) {
////            MultiDex.install(this);
//        }
//    }

    public void excutebindService(){
        Intent intent = new Intent(this, LogService.class);
//        intent.setAction("com.logservice");
//        intent.setPackage("com.jj.game.boost.logtools");
        bindService(intent, sc, Context.BIND_AUTO_CREATE);
    }
    public void excuteUnbindService() {
        unbindService(sc);
    }
    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            LogUtil.e("huzedong", " onServiceConnected ");
//            mBinder = (LogService.MyBinder) binder;
//            mBindService = mBinder.getService();
            mILogRemoteService = ILogRemoteService.Stub.asInterface(binder);
            try {
                PreferenceUtils.setPrefBoolean(getApplicationContext(), "isStart", true);
                mILogRemoteService.ping();
//                mILogRemoteService.storageDelay();
            } catch (RemoteException e) {
                PreferenceUtils.setPrefBoolean(getApplicationContext(), "isStart", false);
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.e("huzedong", " onServiceDisconnected ");
            mILogRemoteService = null;
        }
    }

    public static JJBoostApplication getAccelerateApplication() {
        return application;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        excuteUnbindService();
    }

    /**
     * 配置数据库
     */
    private void setupDatabase() {
        //创建数据库point.db"
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, this.getFilesDir()
                .getAbsolutePath().replace("files", "databases") + File.separator + "point", null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取Dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
    public static DelayLostSaveDao getDaoInstant() {
        return daoSession.getDelayLostSaveDao();
    }
}
