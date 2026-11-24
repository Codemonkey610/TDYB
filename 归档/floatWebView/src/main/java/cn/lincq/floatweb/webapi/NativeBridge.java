package cn.lincq.floatweb.webapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;

import cn.lincq.floatweb.WebFloatModule;
import cn.lincq.floatweb.plugindata.ResultJsonObject;
import cn.lincq.floatweb.plugindata.SuspendedWindowData;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;


public class NativeBridge
{
    JSONObject data;
    UniJSCallback callback;
    Context context;
    Activity activity;
    WebFloatModule uniWebkitModule;
    SuspendedWindowData windowData;
    public NativeBridge(final SuspendedWindowData windowData, WebFloatModule Module) {
        this.uniWebkitModule = Module;
        this.windowData = windowData;
        this.data = windowData.getData();
        this.callback = windowData.getCallback();
        this.context = Module.mUniSDKInstance.getContext();
        this.activity = (Activity) Module.mUniSDKInstance.getContext();
    }
    class Toggle implements Runnable {
        final SuspendedWindowData suspendedWindowData;
        final Boolean goggle;
        Toggle(SuspendedWindowData suspendedWindowData, Boolean goggle) {
            this.suspendedWindowData = suspendedWindowData;
            this.goggle = goggle;
        }
        @Override
        public void run() {
            try {
                uniWebkitModule.toggle(suspendedWindowData, goggle);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
    class UpdateFrame implements Runnable {
        final JSONObject params;
        UpdateFrame(JSONObject obj) {
            this.params = obj;
        }
        @Override
        public void run() {
            try {
                uniWebkitModule.updateFrame(params);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
    class SetFocusable implements Runnable {
        private final Boolean isAllow;
        private final String id;
        private final Activity activity; // 假设您有一个Activity的引用

        SetFocusable(Activity activity, boolean isAllow, String id) {
            this.activity = activity;
            this.isAllow = isAllow;
            this.id = id;
        }

        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        uniWebkitModule.setFocusable(isAllow, id);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Error setting focusable: " + e.getMessage());
                    }
                }
            });
        }
    }
    class SetDragType implements Runnable {
        final int type;
        final String id;
        SetDragType(int type, String id) {
            this.type = type;
            this.id = id;
        }
        @Override
        public void run() {
            try {
                uniWebkitModule.setDragType(type, id);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
    @JavascriptInterface
    public String getData() {
        return this.windowData.getData().toJSONString();
    }
    @JavascriptInterface
    public boolean callback(String key, String value) {
        try {
            JSONObject data = new JSONObject();
            data.put(key, value);
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnSuccess(data, "callback"));
        } catch (Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(e.getMessage()));
            return false;
        }
        return true;
    }

    @JavascriptInterface
    public boolean goApp() {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(this.context.getPackageName(), "io.dcloud.PandoraEntry");
            intent.putExtra("action", "goApp");
            this.context.startActivity(intent);
        } catch (Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(e.getMessage()));
            return false;
        }
        return true;
    }
    @JavascriptInterface
    public boolean hide() {
        return hide(null);
    }
    @JavascriptInterface
    public boolean hide(String key) {
        if (key == null) {
            this.activity.runOnUiThread(new Toggle(windowData, false));
        } else {
            SuspendedWindowData wd = uniWebkitModule.getWindowData(key);
            this.activity.runOnUiThread(new Toggle(wd, false));
            return false;
        }
        return true;
    }
    @JavascriptInterface
    public boolean show() {
        return show(null);
    }
    @JavascriptInterface
    public boolean show(String key) {
        try {
            if (key == null) {
                this.activity.runOnUiThread(new Toggle(windowData, true));
            } else {
                SuspendedWindowData wd = uniWebkitModule.getWindowData(key);
                this.activity.runOnUiThread(new Toggle(wd, true));
            }
        }catch (Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(e.getMessage()));
            return false;
        }
        return true;
    }
    @JavascriptInterface
    public boolean resize(int width, int height) {
        return resize(width, height, null);
    }
    @JavascriptInterface
    public boolean resize(int width, int height, String id) {
        try {
            JSONObject params = new JSONObject();
            params.put("width", width);
            params.put("height", height);
            if(id != null) params.put("id", id);
            this.activity.runOnUiThread(new UpdateFrame(params));
        } catch (Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(-1, e.getMessage()));
            return false;
        }
        return true;
    }
    @JavascriptInterface
    public boolean setPosition(int x, int y) {
        return setPosition(x, y, null);
    }
    @JavascriptInterface
    public boolean setPosition(int x, int y, String id) {
        try {
            JSONObject params = new JSONObject();
            params.put("x", x);
            params.put("y", y);
            if(id != null) params.put("id", id);
            this.activity.runOnUiThread(new UpdateFrame(params));
        } catch (Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(-1, e.getMessage()));
            return false;
        }
        return true;
    }
    @JavascriptInterface
    public boolean setDragType(int type) {
        return setDragType(type, null);
    }
    @JavascriptInterface
    public boolean setDragType(int type, String id) {
        try {
            this.activity.runOnUiThread(new SetDragType(type, id));
        }catch(Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(-1, e.getMessage()));
            return false;
        }
        return true;
    }
    @JavascriptInterface
    public boolean setFocusable(boolean isFocusable) {
        return setFocusable(isFocusable, null);
    }
    @JavascriptInterface
    public boolean setFocusable(boolean isFocusable, String id) {
        try {
            this.activity.runOnUiThread(new SetFocusable(this.activity, isFocusable, id));
        }catch(Exception e) {
            this.callback.invokeAndKeepAlive(new ResultJsonObject().returnFailed(-1, e.getMessage()));
            return false;
        }
        return true;
    }
}