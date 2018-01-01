package com.jj.game.boost.logtools;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.jj.game.boost.dao.DaoMaster;
import com.jj.game.boost.dao.DaoSession;
import com.jj.game.boost.dao.DelayLostSaveDao;
import com.jj.game.boost.domain.DelayLostSave;
import com.jj.game.boost.utils.LogUtil;
import com.jj.game.boost.utils.PreferenceUtils;
import com.jj.game.boost.utils.ThreadManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by huzd on 2017/7/25.
 */

public class LogService extends Service {
    public static final String IPADRESS = "www.baidu.com";
    public static final String EXE = "ping -c 1 " + IPADRESS;
    public static int PORT = 80;
    public static final String IP_KEY = "ip";
    public static final String INTER_KEY = "inter";
    public static final String PORT_KEY = "port";
    private MyBinder binder = new MyBinder();
    private Context mContext = null;
    public static final String PING = "ping -c 1 ";
    public volatile static String EXE2 = PING + IPADRESS;
    private Handler handler = new Handler();
    public volatile static int TIME_INTER = 1000;
    private String delay = "";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        return new MyBinder();
        return MyBinder2;
    }
    public class MyBinder extends Binder{
        public LogService getService(){
            return LogService.this;
        }
    }

    public ILogRemoteService.Stub MyBinder2 = new ILogRemoteService.Stub(){
        @Override
        public void saveIP(String ip) throws RemoteException {
            saveIpadress(ip);
        }

        @Override
        public void savePort(String port) throws RemoteException {
            saveServerPort(port);
        }

        @Override
        public void saveTime(String time) throws RemoteException {
            saveInterTime(time);
        }

        @Override
        public String getIP() throws RemoteException {
            return getIpadress();
        }

        @Override
        public String getPort() throws RemoteException {
            return getServerPort();
        }

        @Override
        public String getTime() throws RemoteException {
            return getInterTime();
        }

        @Override
        public void ping() throws RemoteException {
           pingroot();
        }

        @Override
        public void storageDelay() throws RemoteException {
//            storageDelay2();
        }
    };
    public void saveIpadress(String ip){
        PreferenceUtils.setPrefString(this, IP_KEY, ip);
        LogUtil.e("huzedong", " save ip : " + ip);
    }

    public void saveServerPort(String port){
        PreferenceUtils.setPrefString(this, PORT_KEY, port);
        LogUtil.e("huzedong", " save port : " + port);
    }

    public void saveInterTime(String time){
        PreferenceUtils.setPrefString(this, INTER_KEY, time);
        LogUtil.e("huzedong", " save time : " + time);
    }

    public String getIpadress(){
        String ip = PreferenceUtils.getPrefString(this, IP_KEY, IPADRESS);
        LogUtil.e("huzedong", " get ip : " + ip);
        return ip;
    }

    public String getServerPort(){
        String port = PreferenceUtils.getPrefString(this, PORT_KEY, String.valueOf(PORT));
        LogUtil.e("huzedong", " get port : " + port);
        return  port;
    }

    public String getInterTime(){
        String time = PreferenceUtils.getPrefString(this, INTER_KEY, String.valueOf(TIME_INTER));
        LogUtil.e("huzedong", " get time : " + time);
        return  time;
    }

    public void pingroot(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Process p = null;
                try {
                    p = Runtime.getRuntime().exec(EXE2);
                    BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String str;
                    while((str=buf.readLine())!=null){
                        if(str.contains("avg")){
                            int i=str.indexOf("/", 20);
                            int j=str.indexOf(".", i);
                            delay =str.substring(i+1, j);
                            delay = delay+"ms";
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(null != delay && !delay.equals("")){
                    delay = delay.replace("ms", "");
                    LogUtil.e("huzedong", " delay : " + delay);
                    ThreadManager.postDelayed(ThreadManager.THREAD_WORKER, new Runnable() {
                        @Override
                        public void run() {
                            DelayLostSave bean = new DelayLostSave();
                            bean.setDelay(delay);
                            dao.save(bean);
                        }
                    }, TIME_INTER);
                    handler.postDelayed(this, TIME_INTER);
                }
            }
        };
        handler.postDelayed(runnable, TIME_INTER);
    }
    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            LogUtil.e("huzedong", " =============================  key : " + key);
            if(key.equals(IP_KEY)){
                EXE2 = PING + sharedPreferences.getString(IP_KEY, IPADRESS);
            } else if(key.equals(PORT_KEY)){
                PORT = Integer.valueOf(sharedPreferences.getString(PORT_KEY, "0"));
            } else if(key.equals(INTER_KEY)){
                TIME_INTER = Integer.valueOf(sharedPreferences.getString(INTER_KEY, "0"));
            }
            LogUtil.e("huzedong", " ============================= ");
            LogUtil.e("huzedong", " EXE2 : " + EXE2);
        }
    };
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
        dao = daoSession.getDelayLostSaveDao();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LogService");
        wl.acquire();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mListener);
        wl.release();
    }
    DelayLostSaveDao dao = null;
    DaoSession daoSession = null;
    PowerManager pm;
    PowerManager.WakeLock wl;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mListener);
        setupDatabase();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.e("huzedong", " onUnbind  ");
        return super.onUnbind(intent);
    }
}
