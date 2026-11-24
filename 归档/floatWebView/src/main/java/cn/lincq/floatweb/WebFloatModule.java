package cn.lincq.floatweb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.lincq.floatweb.Service.MyAccessibilityService;
import cn.lincq.floatweb.layout.CustomLinearLayout;
import cn.lincq.floatweb.layout.CustomWebView;
import cn.lincq.floatweb.layout.StatusBarView;
import cn.lincq.floatweb.plugindata.ResultJsonObject;
import cn.lincq.floatweb.plugindata.SuspendedWindowData;
import cn.lincq.floatweb.utils.DisplayUtil;
import cn.lincq.floatweb.utils.Tools;
import cn.lincq.floatweb.webapi.Generator;
import cn.lincq.floatweb.webapi.MapNavigation;
import cn.lincq.floatweb.webapi.NativeBridge;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;
import io.dcloud.feature.uniapp.utils.UniLogUtils;

public class WebFloatModule extends UniModule {
  private final Map<String, SuspendedWindowData> suspendedWindowList = new HashMap<>();
  private WindowManager windowManager;
  private UniJSCallback successCallback;
  public String DEFAULT_ID = "window";
  private UniJSCallback failCallback;
  int screen_width;
  int screen_height;
  public ResultJsonObject resJson = new ResultJsonObject();
  public String getId(String id) {
    return (id == null || id.equals("")) ? DEFAULT_ID : id;
  }
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d("WebFloatModule", "onActivityResult: ");
    if (requestCode == 11111) {
      if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this.mWXSDKInstance.getContext())) {
        if (this.successCallback != null) {
          this.successCallback.invoke(null);
        }
      }
      else if (this.failCallback != null) {
        this.failCallback.invoke(null);
      } 
    }
  }
  @UniJSMethod(uiThread = false)
  public boolean applyPermission(UniJSCallback successCallback, UniJSCallback failCallback) {
    // 保存回调函数以便后续使用
    this.successCallback = successCallback;
    this.failCallback = failCallback;
    // 获取当前上下文中的Activity
    Activity activity = (Activity)this.mUniSDKInstance.getContext();
    // 检查当前Android版本是否大于等于23，并且是否具有悬浮窗权限
    if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity)) {
      // 如果没有权限，则启动权限请求
      Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
      intent.setData(Uri.parse("package:" + activity.getPackageName()));
      activity.startActivityForResult(intent, 11111);
      // 调用失败的回调函数
      if (failCallback != null) {
        failCallback.invoke(resJson.returnFailed("打开悬浮窗权限请求"));
      }
    } else {
      // 如果已经有权限，调用成功的回调函数
      if (successCallback != null) {
        successCallback.invoke(resJson.returnSuccess("已取得权限"));
      }
    }
    return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(activity);
  }

  @UniJSMethod(uiThread = false)
  public boolean canDrawOverlays() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Android M 及以上
      return Settings.canDrawOverlays(this.mUniSDKInstance.getContext());
    } else {
      return true; // 在 Android M 以下，默认拥有悬浮窗权限
    }
  }

  @UniJSMethod
  public void create(JSONObject params , UniJSCallback callback) {
    Context context = this.mUniSDKInstance.getContext();
    if (params == null) params = new JSONObject();
    String key = params.containsKey("id") ? params.getString("id") : DEFAULT_ID;
    String html = params.containsKey("html") ? params.getString("html") : "";
    String css = params.containsKey("css") ? params.getString("css") : "";
    String javaScript = params.containsKey("javaScript") ? params.getString("javaScript") : "";
    JSONObject data = params.containsKey("data") ? params.getJSONObject("data") : new JSONObject();
    // 配置
    JSONObject settings = new JSONObject();
    settings.put("x", params.containsKey("x") ? params.getInteger("x") : 0);
    settings.put("y", params.containsKey("y") ? params.getInteger("y") : 500);
    String url = params.containsKey("url") ? params.getString("url") : "file:///android_asset/index.html";
    if(!Tools.isUrl(url)) {
      callback.invoke(resJson.returnFailed("url Unable to find file path!"));
      return;
    }
    settings.put("url", url);
    settings.put("width", DisplayUtil.pxToDp(context, params.containsKey("width") ? params.getInteger("width") : 50));
    settings.put("height", DisplayUtil.pxToDp(context, params.containsKey("height") ? params.getInteger("height") : 50));
    settings.put("toolbar", params.containsKey("toolbar") ? params.getBoolean("toolbar") : false);
    settings.put("dragType", params.containsKey("dragType") ? params.getInteger("dragType") : 2);
    settings.put("transparent", params.containsKey("transparent") ? params.getBoolean("transparent") : true);
    settings.put("debug", params.containsKey("debug") ? params.getBoolean("debug") : false);
    //  加载参数
    try {
      this.suspendedWindowList.put(key, new SuspendedWindowData());
      SuspendedWindowData suspendedWindowData = getWindowData(key);
      suspendedWindowData.setHtml(html);
      suspendedWindowData.setCss(css);
      suspendedWindowData.setJavaScript(javaScript);
      suspendedWindowData.setData(data);
      if(callback != null) {
        suspendedWindowData.setCallback(callback);
      }
      suspendedWindowData.setSettings(settings);
      createOverlayWindow(suspendedWindowData);
      JSONObject resData = new JSONObject();
      resData.put("id", key);
      callback.invokeAndKeepAlive(resJson.returnSuccess(resData, "创建成功"));
    }catch (Exception e){
      if (callback != null) {
        callback.invoke(resJson.returnFailed(e.getMessage()));
      }
    }
  }
  @UniJSMethod
  public void show(String key) {
    key = getId(key);
    SuspendedWindowData suspendedWindowData = getWindowData(key);
    if(suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
    if(suspendedWindowData.isVisible()) return;
    toggle(key, true);
  }
  @UniJSMethod
  public void hide(String key) {
    key = getId(key);
    SuspendedWindowData suspendedWindowData = getWindowData(key);
    if(suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
    if(!suspendedWindowData.isVisible()) return;
    toggle(key, false);
  }
  @UniJSMethod
  public void toggle(String key, Boolean goggle) {
    key = getId(key);
    SuspendedWindowData suspendedWindowData = getWindowData(key);
    if(suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
    toggle(suspendedWindowData, goggle);
  }
  public void toggle(SuspendedWindowData suspendedWindowData, Boolean goggle) {
    if (goggle == null) {
      goggle = !suspendedWindowData.isVisible();
    }
    final View view = suspendedWindowData.getView();
    final WindowManager.LayoutParams layoutParams = suspendedWindowData.getLayoutParams();
    final boolean shouldShow = goggle;
    suspendedWindowData.setVisible(shouldShow);
    if (shouldShow) {
      // 显示视图
      if (view.getParent() == null) {
        // 视图尚未添加到窗口管理器中，添加它
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          view.setAlpha(0f);
        }
        this.windowManager.addView(view, layoutParams);
      }
      // 开始显示动画
      view.setVisibility(View.VISIBLE); // 确保视图是可见的
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
        view.animate().alpha(1f).setDuration(300).setListener(null);
      }
    } else {
      // 隐藏动画
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
        view.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            // 在动画结束之前设置视图不可见
            view.setVisibility(View.GONE); // 将视图设置为不可见
            // 注意：不要在这里移除视图，以便可以再次显示
          }
        });
      }
    }
  }
  @UniJSMethod
  public void destroy(String key, UniJSCallback successCallback) {
    if (key == null) key = DEFAULT_ID;
    try {
      SuspendedWindowData suspendedWindowData = getWindowData(key);
      if (suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
      final View view = suspendedWindowData.getView();
      windowManager.removeView(view);
      if (successCallback != null) successCallback.invoke(resJson.returnSuccess());
    }catch(Exception e) {
      if (successCallback != null) successCallback.invoke(resJson.returnFailed(e.getMessage()));
    }
  }
  @UniJSMethod
  public void update(JSONObject params, UniJSCallback successCallback) {
    String id = params.containsKey("id") ? params.getString("id") : DEFAULT_ID;
    try {
      if (params.containsKey("width") || params.containsKey("height")) {
        resize(params);  // 设置大小
      }
      if (params.containsKey("x") || params.containsKey("y")) {
        setPosition(params);// 设置位置
      }
      if (params.containsKey("dragType")) {
        setDragType(params.getInteger("dragType"), id);
      }
      boolean isReload = false;
      SuspendedWindowData suspendedWindowData = getWindowData(id);
      if (suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
      if (params.containsKey("html")) {
        suspendedWindowData.setHtml(params.getString("html"));
        isReload = true;
      }
      if (params.containsKey("css")) {
        suspendedWindowData.setCss(params.getString("css"));
        isReload = true;
      }
      if (params.containsKey("javaScript")) {
        suspendedWindowData.setJavaScript(params.getString("javaScript"));
        isReload = true;
      }
      if (params.containsKey("data")) {
        JSONObject data  = params.getJSONObject("data");
        suspendedWindowData.setData(data);
        evalJS("window.receiveUpdateData = JSON.stringify(`" + data.toJSONString() + "`);", id);
        isReload = false;
      }
      if (params.containsKey("url")) {
        String url = params.getString("url");
        if(!Tools.isUrl(url)) {
          throw new RuntimeException("url Unable to find file path!");
        }else{
          changeURL(url, id);
          isReload = false;
        }
      }
      if(isReload) reload(id);
      successCallback.invoke(resJson.returnSuccess("success"));
      if (suspendedWindowData.getCallback() != null) {
        suspendedWindowData.getCallback().invokeAndKeepAlive(resJson.returnSuccess(params, "update"));
      }
    }catch(Exception e) {
      successCallback.invoke(resJson.returnFailed(e.getMessage()));
    }
  }
  @UniJSMethod
  public void resize(JSONObject params) {
    // 检查参数中是否包含id，如果不包含，则使用默认值
    String id = params.containsKey("id") ? params.getString("id") : DEFAULT_ID;
    SuspendedWindowData suspendedWindowData = getWindowData(id);
    // 获取WebView和其父视图
    ViewGroup viewGroup = suspendedWindowData.getView();
    WebView webView = viewGroup.getChildCount() == 1 ? (WebView) viewGroup.getChildAt(0) : (WebView) viewGroup.getChildAt(1);
    // 如果webView为空，则记录错误并返回
    if (webView == null) {
      UniLogUtils.e("WebFloatModule", "请初始化弹窗");
      throw new IllegalArgumentException("请初始化弹窗");
    }
    // 获取webView的布局参数
    ViewGroup.LayoutParams layoutParamsWebView = webView.getLayoutParams();
    WindowManager.LayoutParams layoutParams = suspendedWindowData.getLayoutParams();
    Context context = this.mUniSDKInstance.getContext();
    // 如果参数中包含width，则更新webView和悬浮窗的宽度
    if (params.containsKey("width")) {
      int newWidth = DisplayUtil.pxToDp(context, params.getInteger("width"));
      layoutParamsWebView.width = newWidth;
      layoutParams.width = newWidth;
    }
    // 如果参数中包含height，则更新webView和悬浮窗的高度
    if (params.containsKey("height")) {
      int newHeight = DisplayUtil.pxToDp(context, params.getInteger("height"));
      layoutParamsWebView.height = newHeight;
      layoutParams.height = newHeight;
    }
    // 如果宽度或高度有更新，则应用新的布局参数
    if (params.containsKey("width") || params.containsKey("height")) {
      this.windowManager.updateViewLayout(suspendedWindowData.getView(), layoutParams);
      webView.setLayoutParams(layoutParamsWebView);
    }
  }
  @UniJSMethod
  public void setPosition(JSONObject params) {
    String id = params.getString("id");
    if(!params.containsKey("id")) id = DEFAULT_ID;
    SuspendedWindowData suspendedWindowData = getWindowData(id);
    if (suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
    WindowManager.LayoutParams layoutParams = suspendedWindowData.getLayoutParams();
    if(params.containsKey("x"))  layoutParams.x = params.getInteger("x");
    if(params.containsKey("y"))  layoutParams.y = params.getInteger("y");
    if(params.containsKey("x") || params.containsKey("y")) {
      this.windowManager.updateViewLayout(suspendedWindowData.getView(), layoutParams);
    }
  }
  @UniJSMethod
  public void updateFrame(JSONObject params) {
    // 设置大小
    resize(params);
    // 设置位置
    setPosition(params);
  }
  @UniJSMethod
  public void setDragType(int type, String id) {
    id = getId(id);
    SuspendedWindowData suspendedWindowData = getWindowData(id);
    if(suspendedWindowData == null) throw new IllegalArgumentException("悬浮窗不存在");
    JSONObject newSetting = new JSONObject();
    newSetting.put("dragType", type);
    suspendedWindowData.setSettings(newSetting);
  }
  @UniJSMethod
  public void setFocusable(Boolean isAllow, String id) {
    // 确保UI操作在主线程上执行
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("setFocusable must be called on the main thread");
    }
    id = getId(id);
    SuspendedWindowData suspendedWindowData = getWindowData(id);
    if (suspendedWindowData == null) {
      throw new IllegalArgumentException("悬浮窗不存在");
    }
    if(!suspendedWindowData.isVisible()) {
      throw new IllegalArgumentException("View is not attached to window manager, skipping update");
    }
    WindowManager.LayoutParams layoutParams = suspendedWindowData.getLayoutParams();
    if (isAllow != null) {
      layoutParams.flags = isAllow ?
              WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL :
              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    } else {
      layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // 确保视图已经添加到窗口管理器中
      if (suspendedWindowData.getView().isAttachedToWindow()) {
        this.windowManager.updateViewLayout(suspendedWindowData.getView(), layoutParams);
      } else {
        throw new IllegalArgumentException("View is not attached to window manager, skipping update");
      }
    }else{
      this.windowManager.updateViewLayout(suspendedWindowData.getView(), layoutParams);
    }
  }

  @UniJSMethod
  public void changeURL(String url, String key) {
      key = getId(key);
      WebView webView;
      ViewGroup viewGroup = getWindowData(key).getView();
      if (viewGroup.getChildCount() == 1) {
        webView = (WebView) viewGroup.getChildAt(0);
      } else {
        webView = (WebView) viewGroup.getChildAt(1);
      }
      if (webView == null) {
        UniLogUtils.e("WebFloatModule", "请初始化弹窗");
        throw new IllegalArgumentException("请初始化弹窗");
      }
      webView.loadUrl(url);
  }
  @UniJSMethod
  public void reload(String key) {
    key = getId(key);
    WebView webView;
    ViewGroup viewGroup = getWindowData(key).getView();
    if (viewGroup.getChildCount() == 1) {
      webView = (WebView)viewGroup.getChildAt(0);
    } else {
      webView = (WebView)viewGroup.getChildAt(1);
    }
    if (webView == null) {
      UniLogUtils.e("WebFloatModule", "请初始化弹窗");
      throw new IllegalArgumentException("请初始化弹窗");
    }
    webView.reload();
  }
  @UniJSMethod
  public void exeFunc(String function, String key) {
    changeURL("javascript:" + function, key);
  }
  @UniJSMethod
  public void evalJS(String js, String key) {
    key = getId(key);
    exeFunc("(function() { "+js+" })()", key);
  }

  @UniJSMethod
  public void goToPermissionSetting() {
    Context context = this.mUniSDKInstance.getContext();
    DisplayUtil.goToPermissionSetting(context);
  }

  public SuspendedWindowData getWindowData(String key) {
    SuspendedWindowData suspendedWindowData = this.suspendedWindowList.get(key);
    if (suspendedWindowData == null) {
      Log.d("WebFloatModule", "getWindowData: " + this.suspendedWindowList);
      UniLogUtils.e("WebFloatModule", "悬浮窗不存在");
      throw new IllegalArgumentException("悬浮窗不存在");
    } 
    return suspendedWindowData;
  }
//  private void createOverlayWindow(SuspendedWindowData windowData) {
//    JSONObject settings = windowData.getSettings();
//    Integer width = (Integer)settings.get("width");
//    Integer height = (Integer)settings.get("height");
//    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams((width == null) ? -2 : width, (height == null) ? -2 : height, 2038, 32, -3);
//    DisplayMetrics dm = new DisplayMetrics();
//    ((Activity)this.mUniSDKInstance.getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
//    this.screen_width = dm.widthPixels;
//    this.screen_height = dm.heightPixels;
//    if (this.windowManager == null) {
//      this.windowManager = (WindowManager)this.mUniSDKInstance.getContext().getSystemService(Context.WINDOW_SERVICE);
//    }
//    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//    ViewGroup rootView = getView(windowData);
//    Integer x = (Integer)windowData.getSettings().get("x");
//    if (x != null) {
//      x = Math.max(x, 0);
//      x = Math.min(x, this.screen_width - rootView.getLeft());
//      layoutParams.x = x;
//    }
//    Integer y = (Integer)windowData.getSettings().get("y");
//    if (y != null) {
//      y = Math.max(y, 0);
//      y = Math.min(y, this.screen_height - rootView.getTop());
//      layoutParams.y = y;
//    }
//    layoutParams.gravity = 51;
//    windowData.setView(rootView);
//    windowData.setLayoutParams(layoutParams);
//    windowData.setVisible(false);
//  }

  private void createOverlayWindow(SuspendedWindowData windowData) {
    JSONObject settings = windowData.getSettings();
    Integer width = (Integer) settings.get("width");
    Integer height = (Integer) settings.get("height");

    int windowType;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      windowType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
    }

    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
            (width == null) ? WindowManager.LayoutParams.WRAP_CONTENT : width,
            (height == null) ? WindowManager.LayoutParams.WRAP_CONTENT : height,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            PixelFormat.TRANSLUCENT
    );
    DisplayMetrics dm = new DisplayMetrics();
    ((Activity) this.mUniSDKInstance.getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
    this.screen_width = dm.widthPixels;
    this.screen_height = dm.heightPixels;

    if (this.windowManager == null) {
      this.windowManager = (WindowManager) this.mUniSDKInstance.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    ViewGroup rootView = getView(windowData);
    Integer x = (Integer) windowData.getSettings().get("x");
    if (x != null) {
      x = Math.max(x, 0);
      x = Math.min(x, this.screen_width - rootView.getLeft());
      layoutParams.x = x;
    }

    Integer y = (Integer) windowData.getSettings().get("y");
    if (y != null) {
      y = Math.max(y, 0);
      y = Math.min(y, this.screen_height - rootView.getTop());
      layoutParams.y = y;
    }

    layoutParams.gravity = Gravity.TOP | Gravity.LEFT; // 等价于 51
    windowData.setView(rootView);
    windowData.setLayoutParams(layoutParams);
    windowData.setVisible(false);
  }


  private ViewGroup getView(final SuspendedWindowData windowData) {
    JSONObject settings = windowData.getSettings();
    String url = (String) settings.get("url");
    Integer width = (Integer) settings.get("width");
    Integer height = (Integer) settings.get("height");
    Boolean toolbar = (Boolean) settings.get("toolbar");
    Boolean debug = (Boolean) settings.get("debug");
    Boolean transparent = (Boolean) settings.get("transparent");
    final LinearLayout rootView = new CustomLinearLayout(this.mUniSDKInstance.getContext(), windowData);
    rootView.setLayoutParams(new LinearLayout.LayoutParams((width == null) ? -1 : width, (height == null) ? -1 : height));
    rootView.setOrientation(LinearLayout.VERTICAL);
    WebView webView = new CustomWebView(this.mWXSDKInstance.getContext(), windowData);
    webView.setLayoutParams(new ViewGroup.LayoutParams((width == null) ? -2 : width, (height == null) ? -2 : height));
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
      webSettings.setAllowFileAccess(true);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
      webSettings.setDomStorageEnabled(true);  // 启用 DOM 存储
    }
    if (transparent != null && transparent) {
      webView.setBackgroundColor(0);
    }
    if (url != null) {
      webView.loadUrl(url);
//      注入js代码
      webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
          super.onPageFinished(view, url);
          // 页面加载完成后注入JavaScript代码
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("javascript:(function() { \n" +
                    "function putCallback(key, value) {\n" +
                    "\tlet valueStr = typeof value != 'string' ? JSON.stringify(value) : value;\n" +
                    "\tAndroid.callback(key, valueStr);\n" +
                    "}\n" +
                    "function getData(callback) { Object.defineProperty(window, 'receiveUpdateData', { set(value) { callback && typeof callback == 'function' && callback(JSON.parse(value)); } }); }\n" +
                    "window.onload = () => { window.receiveUpdateData = JSON.parse(Android.getData()); };\n" +
                    "Object.assign(window, {\n" +
                    "\tputCallback: putCallback,\n" +
                    "\tgetData: getData\n" +
                    "});\n" +
                    "const style = document.createElement('style');\n" +
                    "style.innerHTML = Generator.getCssCode();\n" +
                    "const head = document.querySelector('head');\n" +
                    "head.appendChild(style);\n" +
                    "const html = document.createElement('html');\n" +
                    "html.innerHTML = Generator.getHtmlCode();\n" +
                    "const body = document.querySelector('body');\n" +
                    "body.appendChild(html);\n" +
                    "const script = document.createElement('script');\n" +
                    "script.innerHTML = Generator.getJavaScriptCode();\n" +
                    "body.appendChild(script);\n})()", null);
          }
        }
      });
    }
    webView.addJavascriptInterface(new Generator(windowData, this), "Generator");
    webView.addJavascriptInterface(new NativeBridge(windowData, this), "Android");
    webView.addJavascriptInterface(new MapNavigation(this.mUniSDKInstance.getContext(), webView, rootView), "MapData");
    if (debug != null && debug && Build.VERSION.SDK_INT >= 19) {
      WebView.setWebContentsDebuggingEnabled(true);
    }

    StatusBarView statusBar = new StatusBarView(this.mUniSDKInstance.getContext(), windowData);
    statusBar.setCloseClick(new StatusBarView.CloseClick() {
      public void click(View v) {
        WebFloatModule.this.windowManager.removeView((View) rootView);
        windowData.setVisible(false);
        if (windowData.getCallback() != null) {
          windowData.getCallback().invokeAndKeepAlive(resJson.returnSuccess("close"));
        }
      }
    });
    if (toolbar != null && toolbar) {
      rootView.addView(statusBar);
    }
    rootView.addView(webView);
    return rootView;
  }
}
