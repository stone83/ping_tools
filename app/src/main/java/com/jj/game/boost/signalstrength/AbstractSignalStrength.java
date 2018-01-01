package com.jj.game.boost.signalstrength;

abstract class AbstractSignalStrength implements ISignalStrength {

    @SuppressWarnings("unused")
    protected boolean isAsuLevelUnknown(Integer asuLevel) {
        return asuLevel == null || asuLevel == 99 || asuLevel == 255;
    }

}
