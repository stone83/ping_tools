package com.jj.game.boost.traffic;

import android.content.Context;

import com.ccmt.library.lru.LruMap;
import com.jj.game.boost.view.AbstractUserPermissionsCheckActivity;

/**
 * @author myx
 *         by 2017-06-05
 */
public class TrafficManagerFactory {

    private static ITrafficManager createOldTrafficManager() {
        LruMap lruMap = LruMap.getInstance();
        String name = OldTrafficManager.class.getName();
        ITrafficManager trafficManager = (ITrafficManager) lruMap.get(name);
        if (trafficManager == null) {
            trafficManager = new OldTrafficManager();
            lruMap.put(name, trafficManager);
        }
        return trafficManager;
    }

    /**
     * @param context
     * @return 如果返回null, 说明需要用户授权.
     */
    @SuppressWarnings({"NewApi", "JavaDoc"})
    private static ITrafficManager createNewTrafficManager(Context context) {
        LruMap lruMap = LruMap.getInstance();
        String name = NewTrafficManager.class.getName();
        ITrafficManager trafficManager = (ITrafficManager) lruMap.get(name);
        if (trafficManager == null) {
            trafficManager = new NewTrafficManager();
            lruMap.put(name, trafficManager);
        }
        if (!AbstractUserPermissionsCheckActivity.hasPermissionToReadNetworkStats(context)) {
//            LogUtil.i("让用户去授权");
//            AbstractUserPermissionsCheckActivity.sIsAuthorization = true;
//            AbstractUserPermissionsCheckActivity.requestReadNetworkStats(context);
            return null;
        }
        return trafficManager;
    }

    public static ITrafficManager createTrafficManager(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return createNewTrafficManager(context);
        }
        return createOldTrafficManager();
    }

}
