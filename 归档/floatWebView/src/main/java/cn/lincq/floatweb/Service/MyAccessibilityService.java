package cn.lincq.floatweb.Service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.View;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.graphics.Color;
import android.view.Gravity;

import androidx.annotation.RequiresApi;

import cn.lincq.floatweb.plugindata.SuspendedWindowData;

@RequiresApi(api = Build.VERSION_CODES.DONUT)
public class MyAccessibilityService extends AccessibilityService {

    private WindowManager windowManager;
    private View overlayView;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 根据需要处理无障碍事件
    }

    @Override
    public void onInterrupt() {
        // 当服务被中断时移除悬浮窗
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
//        createAccessibilityOverlay();
    }

    private void createAccessibilityOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, // 使用 Accessibility Overlay 类型
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );

        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;

        // 创建一个简单的视图示例，你可以根据需求自定义视图
        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.parseColor("#80FF0000")); // 半透明红色

        windowManager.addView(overlayView, layoutParams);
    }
    public void showCustomOverlay(SuspendedWindowData windowData) {
        WindowManager.LayoutParams layoutParams = windowData.getLayoutParams();
        View view = windowData.getView();

        if (windowManager != null && view != null && !windowData.isVisible()) {
            windowManager.addView(view, layoutParams);
            windowData.setVisible(true);
        }
    }

    public void hideCustomOverlay(SuspendedWindowData windowData) {
        WindowManager.LayoutParams layoutParams = windowData.getLayoutParams();
        View view = windowData.getView();

        if (windowManager != null && view != null && windowData.isVisible()) {
            windowManager.removeView(view);
            windowData.setVisible(false);
        }
    }
}