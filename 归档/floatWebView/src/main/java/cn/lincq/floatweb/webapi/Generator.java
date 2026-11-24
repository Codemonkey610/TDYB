package cn.lincq.floatweb.webapi;

import android.webkit.JavascriptInterface;

import cn.lincq.floatweb.WebFloatModule;
import cn.lincq.floatweb.plugindata.SuspendedWindowData;

public class Generator {
  private SuspendedWindowData windowData;
  public Generator(final SuspendedWindowData windowData, WebFloatModule Module) {
    this.windowData = windowData;
  }
  
  @JavascriptInterface
  public String getHtmlCode() {
    return this.windowData.getHtml();
  }
  @JavascriptInterface
  public String getCssCode() {
    return this.windowData.getCss();
  }
  @JavascriptInterface
  public String getJavaScriptCode() {
    return this.windowData.getJavaScript();
  }
}