package com.jj.game.boost.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.jj.game.boost.R;

/**
 * @author myx
 *         by 2017-07-11
 */
public class RightView extends LinearLayout {

    public RightView(Context context) {
        super(context);
        init();
    }

    public RightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.custom_view_right, this);
    }

}
