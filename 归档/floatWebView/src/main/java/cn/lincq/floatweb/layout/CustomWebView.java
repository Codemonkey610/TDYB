package cn.lincq.floatweb.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;

import cn.lincq.floatweb.plugindata.SuspendedWindowData;

public class CustomWebView extends WebView {
    private float startx;
    private float starty;
    private boolean toolbar;
    private static final int MARGIN = 100; // 吸附范围，可以根据需要调整
    private static final int ANIMATION_DURATION = 300; // 动画持续时间，可以根据需要调整
    WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
    private SuspendedWindowData windowData;
    private JSONObject settings;
    private View rootView;
    WindowManager.LayoutParams layoutParams;
    public CustomWebView(Context context, SuspendedWindowData windowData) {
        super(context);
        this.settings = windowData.getSettings();
        this.windowData = windowData;
        this.toolbar = this.settings.getBoolean("toolbar");
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int dragType = this.settings.getIntValue("dragType");
        if(dragType == 0) dragType = 2;
        // 如果 toolbar 显示这是用 他的拖拽效果
        if(toolbar) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录触摸的起始位置
                startx = event.getRawX();
                starty = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 计算触摸的偏移量
                if(dragType == 2 || dragType == 3) {
                    float movex = event.getRawX() - startx;
                    float movey = event.getRawY() - starty;
                    // 更新悬浮窗的位置
                    this.layoutParams.x += (int) movex;
                    this.layoutParams.y += (int) movey;
                    this.windowManager.updateViewLayout(this.rootView, (ViewGroup.LayoutParams) layoutParams);
                    // 重新设置起始位置
                    startx = event.getRawX();
                    starty = event.getRawY();
                }
                break;
            case MotionEvent.ACTION_UP:
                // 在手指抬起时，可以添加其他逻辑
                if(dragType == 3) {
                    int x = layoutParams.x;
                    int y = layoutParams.y;
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    int windowWidth = windowData.getLayoutParams().width;
//                    int windowHeight = this.getHeight();
                    int windowHeight = windowData.getLayoutParams().height;
                    // 判断悬浮窗应该吸附到哪一边缘
                    int toX;
                    if (x + windowWidth / 2 < screenWidth / 2) {
                        // 吸附到左侧边缘
                        toX = 0;
                    } else {
                        // 吸附到右侧边缘
                        toX = screenWidth - windowWidth;
                    }

                    // 使用动画来平滑地移动悬浮窗到吸附的位置
                    ObjectAnimator animator = ObjectAnimator.ofInt(this, "x", x, toX);
                    animator.setDuration(ANIMATION_DURATION);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // 动画结束后更新LayoutParams
                            layoutParams.x = toX;
                            windowManager.updateViewLayout(CustomWebView.this.rootView, layoutParams);
                        }
                    });
                    animator.start();
                }
                break;
        }
        // 调用父类的 onTouchEvent 方法来处理触摸事件
        return super.onTouchEvent(event);
    }

    public void setX(int x) {
        layoutParams.x = x;
        windowManager.updateViewLayout(this.rootView, layoutParams);
    }

    public void setY(int y) {
        layoutParams.y = y;
        windowManager.updateViewLayout(this.rootView, layoutParams);
    }

    protected void onAttachedToWindow() {
        this.rootView = (View)this.windowData.getView();
        this.layoutParams = this.windowData.getLayoutParams();
        super.onAttachedToWindow();
    }
}
