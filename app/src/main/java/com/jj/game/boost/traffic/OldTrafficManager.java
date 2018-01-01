package com.jj.game.boost.traffic;

import android.content.Context;
import android.net.TrafficStats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author myx
 *         by 2017-06-05
 */
public class OldTrafficManager extends AbstractTrafficManager {

    private String mUidStatDir;

    private static final long serialVersionUID = 1474716992443035296L;
    private static final int TYPE_TCP_RCV = 1;
    private static final int TYPE_TCP_SND = 2;

    OldTrafficManager() {
        mUidStatDir = File.separator + "proc" + File.separator + "uid_stat";
    }

    @Override
    public long getUidRxBytes(Context context, int uid) {
//        LogUtil.i("OldTrafficManager getUidRxBytes()");
        long uidRxBytes = TrafficStats.getUidRxBytes(uid);
        if (uidRxBytes == TrafficStats.UNSUPPORTED) {
            return readTraffic(mUidStatDir + File.separator + uid, TYPE_TCP_RCV);
        }
        return uidRxBytes;
    }

    @Override
    public long getUidRxBytesMobile(int uid) {
        return 0;
    }

    @Override
    public long getUidRxBytesWifi(int uid) {
        return 0;
    }

    @Override
    public long getUidTxBytes(Context context, int uid) {
//        LogUtil.i("OldTrafficManager getUidTxBytes()");
        long uidTxBytes = TrafficStats.getUidTxBytes(uid);
        if (uidTxBytes == TrafficStats.UNSUPPORTED) {
            return readTraffic(mUidStatDir + File.separator + uid, TYPE_TCP_SND);
        }
        return uidTxBytes;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private long readTraffic(String dir, int type) {
        File file;
        if (type == TYPE_TCP_RCV) {
            file = new File(dir + File.separator + "tcp_rcv");
        } else {
            file = new File(dir + File.separator + "tcp_snd");
        }
//        LogUtil.i("file.canRead() -> " + file.canRead());
//        LogUtil.i("file.canWrite() -> " + file.canWrite());
//        LogUtil.i("file.canExecute() -> " + file.canExecute());
        String line;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            if ((line = br.readLine()) != null) {
                return Long.parseLong(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    @Override
    public long getUidTxBytesMobile(int uid) {
        return 0;
    }

    @Override
    public long getUidTxBytesWifi(int uid) {
        return 0;
    }

}
