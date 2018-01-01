package com.jj.game.boost.signalstrength;

import com.ccmt.library.lru.LruMap;

/**
 * @author myx
 *         by 2017-07-07
 */
public class SignalStrengthFactory {

    public static ISignalStrength createSignalStrength() {
        LruMap lruMap = LruMap.getInstance();
        String name = MobileSignalStrength.class.getName();
        ISignalStrength signalStrength = (ISignalStrength) lruMap.get(name);
        if (signalStrength == null) {
            signalStrength = new MobileSignalStrength();
            lruMap.put(name, signalStrength);
        }
        return signalStrength;
    }

}
