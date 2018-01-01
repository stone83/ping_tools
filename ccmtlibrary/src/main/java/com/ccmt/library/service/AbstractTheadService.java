package com.ccmt.library.service;

import android.content.Intent;

/**
 * @author myx
 *         by 2017-06-20
 */
public abstract class AbstractTheadService extends AbstractService {

    protected boolean mIsExit;

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsExit = true;
    }

    @Override
    protected void doTask(Intent intent) {
        mIsExit = false;
    }

}
