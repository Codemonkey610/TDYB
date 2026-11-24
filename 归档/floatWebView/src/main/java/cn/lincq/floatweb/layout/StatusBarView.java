package cn.lincq.floatweb.layout;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.alibaba.fastjson.JSONObject;

import cn.lincq.floatweb.plugindata.SuspendedWindowData;

public class StatusBarView extends View {
    private Paint paint;
    float closeWidth;
    float closeHeight;
    float closeX;
    float closeY;
    private CloseClick closeClick;
    int height;
    int width;
    WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

    private int screen_width;

    private int screen_height;
    private final SuspendedWindowData windowData;
    private JSONObject settings;
    View rootView;

    public StatusBarView(Context context, SuspendedWindowData windowData) {
        super(context);
        this.windowData = windowData;
        this.settings = windowData.getSettings();
        init();
    }

    WindowManager.LayoutParams layoutParams;
    private int statusBarHeight;
    float dx;
    float dy;

    private void init() {
        this.paint = new Paint();
        this.paint.setColor(-1);
        this.paint.setTextSize(70.0F);
        this.closeWidth = this.paint.measureText("×");
        this.closeHeight = this.paint.descent() - this.paint.ascent();
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.screen_width = dm.widthPixels;
        this.screen_height = dm.heightPixels;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            this.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int touchX, touchY;
        float x = event.getX();
        float y = event.getY();
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        int dragType = this.settings.getIntValue("dragType");
        if (dragType == 0) dragType = 2;
        switch (event.getAction()) {
            case 0:
                if (x < this.closeX + this.closeWidth && x > this.closeX && y > this.closeY - this.closeHeight && y < this.closeY) {
                    if (this.closeClick != null) {
                        this.closeClick.click(this);
                    }
                } else {
                    performClick();
                }
                this.dx = x;
                this.dy = y;
                break;
            case 2:
                touchX = (int) (rawX - this.dx);
                touchY = (int) (rawY - this.dy) - this.statusBarHeight;
                touchX = Math.max(0, Math.min(touchX, this.screen_width - this.rootView.getLeft()));
                touchY = Math.max(0, Math.min(touchY, this.screen_height - this.rootView.getTop()));
                if (dragType == 2 || dragType == 3) {
                    this.layoutParams.y = touchY;
                    this.layoutParams.x = touchX;
                    this.windowManager.updateViewLayout(this.rootView, (ViewGroup.LayoutParams) this.layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (dragType == 3) {
                    // 添加吸边动画
                    int halfScreenWidth = this.screen_width / 2;
                    int finalX = this.layoutParams.x + this.rootView.getWidth() / 2 < halfScreenWidth ? 0 : this.screen_width - this.rootView.getWidth();
                    ObjectAnimator animator = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        animator = ObjectAnimator.ofInt(this, "x", this.layoutParams.x, finalX);
                        animator.setDuration(300); // 动画持续时间，可以根据需要调整
                        animator.setInterpolator(new DecelerateInterpolator()); // 减速插值器，使动画结束时不那么突然
                        animator.start();
                    }
                }
                break;
        }
        return true;
    }

    // 添加一个用于动画的 setter 方法
    public void setX(int x) {
        this.layoutParams.x = x;
        this.windowManager.updateViewLayout(this.rootView, (ViewGroup.LayoutParams) this.layoutParams);
    }


    public boolean performClick() {
        return super.performClick();
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.width = View.MeasureSpec.getSize(widthMeasureSpec);
        this.height = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == Integer.MIN_VALUE) {
            this.height = 100;
        }

        setMeasuredDimension(this.width, this.height);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(-2013265920);
        this.closeX = this.width - this.closeWidth - 20.0F;
        this.closeY = this.height - this.closeHeight / 2.0F + 20.0F;
        canvas.drawText("×", this.closeX, this.closeY, this.paint);
    }

    protected void onAttachedToWindow() {
        this.rootView = (View) this.windowData.getView();
        this.layoutParams = this.windowData.getLayoutParams();
        super.onAttachedToWindow();
    }


    public void setCloseClick(CloseClick closeClick) {
        this.closeClick = closeClick;
    }

    public static interface CloseClick {
        void click(View param1View);
    }
}
