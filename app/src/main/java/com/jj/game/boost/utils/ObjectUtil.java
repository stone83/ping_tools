package com.jj.game.boost.utils;

import com.jj.game.boost.dynamicpermissions.DynamicPermissionManager;
import com.ccmt.library.lru.LruMap;

public class ObjectUtil {

    public static DynamicPermissionManager obtainDynamicPermissionManager() {
        Class<DynamicPermissionManager> cla = DynamicPermissionManager.class;
        return LruMap.getInstance().createOrGetElement(cla.getName(), cla,
                DynamicPermissionManager::new);
    }

}