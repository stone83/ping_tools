package com.jj.game.boost.modebiz;

import android.content.Context;

/**
 * Created by huzd on 2017/7/4.
 */

public interface IJJBoostDetectBiz {
     //当前网络的类型
     String getCurrentNetType();
     //当前网络名称，包括wifi和4g，其内部具体区分
     String getCurrentNetName();
     //当前网络强度，包括wifi和4g，其内部具体区分
     String getCurrentNetLevel();
     boolean getBoostStart();
//     //当前进程列表，包含更应用的流量
//     List<ProcessInfo> getCurrentProcessInfo();
     /**
      * 检测指定包名的应用是否运行中
      * @param context
      * @param packageName
      * @return
      */
     @SuppressWarnings("JavaDoc")
     boolean isPackageNameRunning(Context context,String packageName);
}
