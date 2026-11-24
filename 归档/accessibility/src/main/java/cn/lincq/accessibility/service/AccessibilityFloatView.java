package cn.lincq.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import cn.lincq.accessibility.R;
import cn.lincq.accessibility.bean.ScreenProperty;
import cn.lincq.accessibility.tool.MyTool;
public class AccessibilityFloatView {
    public final AccessibilityService a;
    public WindowManager bWindowManager;
    public WindowManager.LayoutParams cLayoutParams;
    public LayoutInflater d;
    public View eView;
    public int fWidth;
    public int gHeightAndStatusBar;
    public boolean h;
    public boolean isShow;

    public AccessibilityFloatView(AccessibilityService accessibilityService, boolean z, String str) {
        getClass().getSimpleName();
        this.isShow = false;
        this.fWidth = 0;
        this.gHeightAndStatusBar = 0;
        ScreenProperty androiodScreenProperty = MyTool.getAndroiodScreenProperty(accessibilityService);
        this.fWidth = androiodScreenProperty.getScreenWidthPx();
        this.gHeightAndStatusBar = androiodScreenProperty.getScreenHeightPx() + MyTool.getStatusBarHeight(accessibilityService);
        this.a = accessibilityService;
        this.h = z;
        a(str);
    }

    public final void a(String str) {
        this.bWindowManager = (WindowManager) this.a.getSystemService("window");
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        this.cLayoutParams = layoutParams;
        if (Build.VERSION.SDK_INT >= 26) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        layoutParams.width = this.fWidth;
        layoutParams.height = this.gHeightAndStatusBar;
        layoutParams.format = -3;
        if (this.h) {
            layoutParams.flags = 1792;
        } else {
            layoutParams.flags = 1848;
        }
        layoutParams.gravity = 48;
        layoutParams.x = 0;
        this.cLayoutParams = layoutParams;
        LayoutInflater from = LayoutInflater.from(this.a.getApplicationContext());
        this.d = from;
        View inflate = from.inflate(R.layout.float_view_window, (ViewGroup) null);
        this.eView = inflate;
        inflate.setBackgroundColor(Color.parseColor(str));
    }

    public void hideView() {
        if (this.isShow) {
            WindowManager windowManager = this.bWindowManager;
            if (windowManager != null) {
                windowManager.removeView(this.eView);
            }
            this.isShow = false;
        }
    }

    public void showView() {
        WindowManager windowManager;
        if ((Build.VERSION.SDK_INT >= 23 && !MyTool.checkFloatPermission(this.a)) || this.isShow || (windowManager = this.bWindowManager) == null) {
            return;
        }
        windowManager.addView(this.eView, this.cLayoutParams);
        this.isShow = true;
    }
}
