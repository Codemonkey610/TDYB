package cn.lincq.floatweb.webapi;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class MapNavigation
{
  private static final String TAG = "MapNavigation";
  ViewGroup rootView;
  WebView webView;
  Context context;
  int x;
  int y;
  int height;
  int width;
  
  public MapNavigation(Context context, WebView webView, ViewGroup rootView) {
    this.webView = webView;
    this.rootView = rootView;
    this.context = context;
  }
  
  @JavascriptInterface
  public void setMapPosition(final int x, final int y, final int height, final int width) {
    this.x = x;
    this.y = y;
    this.height = height;
    this.width = width;
    ((Activity)this.webView.getContext()).runOnUiThread(new Runnable()
        {
          public void run() {
            int[] locationInWindow = new int[2];
            MapNavigation.this.webView.getLocationOnScreen(locationInWindow);
            Log.d("MapNavigation", "setMapPosition: webView.x=" + MapNavigation.this.webView.getLeft());
            Log.d("MapNavigation", "setMapPosition: webView.y=" + MapNavigation.this.webView.getTop());
            View view = new View(MapNavigation.this.context);
            view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            view.setBackgroundColor(-65281);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
              view.setX(MapNavigation.this.webView.getX() + x);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
              view.setY(MapNavigation.this.webView.getY() + y);
            }
            MapNavigation.this.rootView.addView(view);
            Log.d("MapNavigation", "setMapPosition: x=" + x);
            Log.d("MapNavigation", "setMapPosition: y=" + y);
            Log.d("MapNavigation", "setMapPosition: height=" + height);
            Log.d("MapNavigation", "setMapPosition: width=" + width);
          }
        });
  }
  
  public static int dpToPx(Context context, int dp) {
    float density = (context.getResources().getDisplayMetrics()).density;
    return Math.round(dp * density);
  }
}