package com.jj.game.boost.logtools;

import android.support.v4.app.Fragment;

import com.jj.game.boost.JJBoostApplication;

public class AbstractFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        JJBoostApplication.getRefWatcher().watch(this);
    }

}
