package cn.lincq.floatweb.layout;

import android.content.Context;
import android.widget.LinearLayout;

import cn.lincq.floatweb.plugindata.SuspendedWindowData;

public class CustomLinearLayout extends LinearLayout {
    private SuspendedWindowData windowData;

    public CustomLinearLayout(Context context, SuspendedWindowData windowData) {
        super(context);
        this.windowData = windowData;
    }
}
