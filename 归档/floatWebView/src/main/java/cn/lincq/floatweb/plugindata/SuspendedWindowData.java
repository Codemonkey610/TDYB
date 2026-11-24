package cn.lincq.floatweb.plugindata;

import android.view.ViewGroup;
import android.view.WindowManager;
import com.alibaba.fastjson.JSONObject;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;


public class SuspendedWindowData
{
  private ViewGroup view;
  private WindowManager.LayoutParams layoutParams;
  private String html = "";
  private String css = "";
  private String javaScript = "";
  private JSONObject data = new JSONObject();
  private UniJSCallback callback;
  private JSONObject settings = new JSONObject();
  
  private boolean isVisible;

  public String getHtml() {
    return this.html;
  }
  
  public void setHtml(String html) {
    this.html = html;
  }
  
  public String getCss() {
    return this.css;
  }
  
  public void setCss(String css) {
    this.css = css;
  }
  
  public String getJavaScript() {
    return this.javaScript;
  }
  
  public void setJavaScript(String javaScript) {
    this.javaScript = javaScript;
  }
  
  public JSONObject getData() {
    return this.data;
  }
  
  public void setData(JSONObject data) {
    this.data = data;
  }
  
  public UniJSCallback getCallback() {
    return this.callback;
  }
  
  public void setCallback(UniJSCallback callback) {
    this.callback = callback;
  }
  
  public JSONObject getSettings() {
    return this.settings;
  }

  public void setSettings(JSONObject newSettings) {
    if (this.settings == null) {
      // 如果当前没有设置，直接使用新的设置
      this.settings = newSettings;
    } else {
      // 如果当前有设置，合并新的设置
      for (String key : newSettings.keySet()) {
        this.settings.put(key, newSettings.get(key));
      }
    }
  }


  public ViewGroup getView() {
    return this.view;
  }
  
  public void setView(ViewGroup view) {
    this.view = view;
  }
  
  public WindowManager.LayoutParams getLayoutParams() {
    return this.layoutParams;
  }
  
  public void setLayoutParams(WindowManager.LayoutParams layoutParams) {
    this.layoutParams = layoutParams;
  }
  
  public boolean isVisible() {
    return this.isVisible;
  }
  
  public void setVisible(boolean visible) {
    this.isVisible = visible;
  }
}