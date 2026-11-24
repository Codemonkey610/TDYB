package cn.lincq.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import cn.lincq.accessibility.bean.UniMessage;
import cn.lincq.accessibility.tool.MyTool;
import cn.lincq.accessibility.bean.AccNode;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityWindowInfo;

public class AccService extends AccessibilityService {
    public static AccService mAccService;
    public boolean isStart = false; // 用于判断是否开始
    public boolean b;
    public UniJSCallback c = null;
    public Map<String, Object> d = new HashMap();
    public AccessibilityFloatView e;
    public final String a = getClass().getSimpleName();
    private AccessibilityEvent tempAccessibilityEvent = null;
    private boolean longClick = false;  // 假设长按为200ms，点击为20ms

    public final AccNode a(AccessibilityNodeInfo accessibilityNodeInfo) {
        AccNode accNode = new AccNode();
        accNode.setText(MyTool.char2string(accessibilityNodeInfo.getText()));
        accNode.setPackageName(MyTool.char2string(accessibilityNodeInfo.getPackageName()));
        accNode.setContentDesc(MyTool.char2string(accessibilityNodeInfo.getContentDescription()));
        accNode.setClassName(MyTool.char2string(accessibilityNodeInfo.getClassName()));
        accNode.setChecked(accessibilityNodeInfo.isChecked());
        accNode.setFocused(accessibilityNodeInfo.isFocused());
        accNode.setPassword(accessibilityNodeInfo.isPassword());
        accNode.setSelected(accessibilityNodeInfo.isSelected());
        Rect rect = new Rect();
        accessibilityNodeInfo.getBoundsInScreen(rect);
        accNode.setBounds(rect);
        return accNode;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 获取应用程序的标签（即名称）
//        String appName = getAppName();
    }
    @Override
    public void onInterrupt() {
      this.isStart = false;
    }

    private String getAppName() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 获取所有窗口的节点
    public List<AccNode> getAllWindowNodes() {
        List<AccNode> nodeList = new ArrayList<>();
        List<AccessibilityWindowInfo> windows = getWindows();

        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo rootNode = window.getRoot();
            if (rootNode != null) {
                traverseNode(rootNode, nodeList);
            }
        }
        return nodeList;
    }

    private void traverseNode(AccessibilityNodeInfo nodeInfo, List<AccNode> nodeList) {
        if (nodeInfo != null) {
            nodeList.add(MyTool.getNodeInform(nodeInfo));
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                traverseNode(nodeInfo.getChild(i), nodeList);
            }
        }
    }
    // 根据只要ID点击指定的 index 个节点
    public boolean clickAllWindowsNodeById(String id, int index) {
        List<AccessibilityNodeInfo> matchingNodes = new ArrayList<>();
        List<AccessibilityWindowInfo> windows = getWindows();
        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo rootNode = window.getRoot();
            if (rootNode != null) {
                findNodesById(rootNode, id, matchingNodes);
            }
        }

        if (index >= 0 && index < matchingNodes.size()) {
            AccessibilityNodeInfo nodeToClick = matchingNodes.get(index);
//            if (nodeToClick.isClickable()) {
//                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                tap_bt(nodeToClick);
                return true;
//            }
        }
        return false;
    }

    private void findNodesById(AccessibilityNodeInfo nodeInfo, String id, List<AccessibilityNodeInfo> matchingNodes) {
        if (nodeInfo != null) {
            if (id != null && id.equals(nodeInfo.getViewIdResourceName())) {
                matchingNodes.add(nodeInfo);
            }
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                findNodesById(nodeInfo.getChild(i), id, matchingNodes);
            }
        }
    }

    public boolean clickAllWindowsNodeByText(String text, int index) {
        List<AccessibilityNodeInfo> matchingNodes = new ArrayList<>();
        List<AccessibilityWindowInfo> windows = getWindows();
        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo rootNode = window.getRoot();
            if (rootNode != null) {
                findNodesByText(rootNode, text, matchingNodes);
            }
        }

        if (index >= 0 && index < matchingNodes.size()) {
            AccessibilityNodeInfo nodeToClick = matchingNodes.get(index);
//            if (nodeToClick.isClickable()) {
//                nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                tap_bt(nodeToClick);
                return true;
//            }
        }

        return false;
    }

    private void findNodesByText(AccessibilityNodeInfo nodeInfo, String text, List<AccessibilityNodeInfo> matchingNodes) {
        if (nodeInfo != null) {
            if (text != null && (text.equals(MyTool.char2string(nodeInfo.getText())) || text.equals(MyTool.char2string(nodeInfo.getContentDescription())))) {
                matchingNodes.add(nodeInfo);
            }
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                findNodesByText(nodeInfo.getChild(i), text, matchingNodes);
            }
        }
    }

    public void slide_screen(int startX, int startY, int endX, int endY, int duration) {
      Path path = new Path();
      path.moveTo(startX, startY);
      path.lineTo(endX, endY);

      GestureDescription.Builder gestureBuilder = null;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          gestureBuilder = new GestureDescription.Builder();
          gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));
          dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
              @Override
              public void onCompleted(GestureDescription gestureDescription) {
                  super.onCompleted(gestureDescription);
//                  Log.d(TAG, "滑动完成");
              }

              @Override
              public void onCancelled(GestureDescription gestureDescription) {
                  super.onCancelled(gestureDescription);
//                  Log.d(TAG, "滑动取消");
              }
          }, null);
        }
  }
    // 手势滑动
    public void performGestureSlide(int[][] points, int duration, boolean useCurve) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            // 每个内部数组都是[x, y]的形式
            for (int i = 0; i < points.length; i++) {
                int[] point = points[i];
                if (i == 0) {
                    // 第一个点，使用moveTo
                    path.moveTo(point[0], point[1]);
                } else {
                    if (useCurve) {
                        // 使用quadTo创建曲线
                        int[] prevPoint = points[i - 1];
                        path.quadTo(prevPoint[0], prevPoint[1], point[0], point[1]);
                    } else {
                        // 不使用曲线，使用lineTo创建直线
                        path.lineTo(point[0], point[1]);
                    }
                }
            }

            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));

            GestureDescription gestureDescription = gestureBuilder.build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    // 手势完成后的操作
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    // 手势取消后的操作
                }
            }, null);
        }
    }

    public void performGestureSlide(int[][] segments, boolean useCurve) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            for (int i = 0; i < segments.length; i++) {
                int[] segment = segments[i];
                int startX = segment[0], startY = segment[1], endX = segment[2], endY = segment[3], duration = segment[4];

                Path path = new Path();
                path.moveTo(startX, startY);
                if (useCurve && i > 0) {
                    // 使用quadTo创建曲线，需要前一个线段的结束坐标
                    int[] prevSegment = segments[i - 1];
                    int prevEndX = prevSegment[2], prevEndY = prevSegment[3];
                    path.quadTo(prevEndX, prevEndY, startX, startY);
                } else {
                    // 不使用曲线或第一个点，使用lineTo创建直线
                    path.lineTo(endX, endY);
                }

                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, duration));

                final GestureDescription gestureDescription = gestureBuilder.build();

                final int delay = i == 0 ? 0 : segments[i - 1][4]; // 如果不是第一个线段，则延迟前一个线段的duration时间
                mainHandler.postDelayed(() -> {
                    dispatchGesture(gestureDescription, new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                            // 手势完成后的操作
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            // 手势取消后的操作
                        }
                    }, null);
                }, delay);
            }
        }
    }


    public final String a(CharSequence charSequence) {
        return charSequence != null ? charSequence.toString() : "";
    }

    public boolean clickFirstNodeByContentDesc(String str) {
        AccessibilityNodeInfo findFirstNodeByContentDesc = findFirstNodeByContentDesc(getRootInActiveWindow(), str);
        if (findFirstNodeByContentDesc != null) {
            return tap_bt(findFirstNodeByContentDesc);
        }
        return false;
    }

    public boolean clickFirstNodeById(String str) {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            return tap_bt(rootInActiveWindow, str);
        }
        return false;
    }

    public boolean clickFirstNodeByNotFullContentDesc(String str) {
        AccessibilityNodeInfo findFirstNodeByNotFullContentDesc = findFirstNodeByNotFullContentDesc(getRootInActiveWindow(), str);
        if (findFirstNodeByNotFullContentDesc != null) {
            return tap_bt(findFirstNodeByNotFullContentDesc);
        }
        return false;
    }

    public boolean clickFirstNodeByNotFullText(String str) {
        AccessibilityNodeInfo findFirstNodeByNotFullText = findFirstNodeByNotFullText(getRootInActiveWindow(), str);
        if (findFirstNodeByNotFullText != null) {
            return tap_bt(findFirstNodeByNotFullText);
        }
        return false;
    }

    public boolean clickFirstNodeByText(String str) {
        AccessibilityNodeInfo findFirstNodeByText = findFirstNodeByText(getRootInActiveWindow(), str);
        if (findFirstNodeByText != null) {
            return tap_bt(findFirstNodeByText);
        }
        return false;
    }

    public boolean clickNodeById(String str, int i) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(str);
        if (findAccessibilityNodeInfosByViewId.size() < i + 1) {
            return false;
        }
        AccessibilityNodeInfo nodeInfo = findAccessibilityNodeInfosByViewId.get(i);
        if(longClick && nodeInfo.isVisibleToUser()) {
            return performLongClick(nodeInfo);
        }
        if(nodeInfo.isVisibleToUser() && !nodeInfo.performAction(16)) {
            return forceClick(nodeInfo);
        }
        return false;
    }

    public boolean edit_input(AccessibilityNodeInfo accessibilityNodeInfo, String str, int i, String str2) {
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(str);
            if (findAccessibilityNodeInfosByViewId.isEmpty() || findAccessibilityNodeInfosByViewId.size() > i + 1) {
                return false;
            }
            AccessibilityNodeInfo accessibilityNodeInfo2 = findAccessibilityNodeInfosByViewId.get(i);
            if (accessibilityNodeInfo2.getClassName().equals("android.widget.EditText")) {
                Bundle bundle = new Bundle();
                bundle.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", str2);
                accessibilityNodeInfo2.performAction(1);
                return accessibilityNodeInfo2.performAction(2097152, bundle);
            }
            return false;
        }
        return false;
    }

    public boolean edit_input(AccessibilityNodeInfo accessibilityNodeInfo, String str, String str2) {
        if (accessibilityNodeInfo != null) {
            for (AccessibilityNodeInfo accessibilityNodeInfo2 : accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(str)) {
                if (accessibilityNodeInfo2.getClassName().equals("android.widget.EditText")) {
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", str2);
                    accessibilityNodeInfo2.performAction(1);
                    return accessibilityNodeInfo2.performAction(2097152, bundle);
                }
            }
            return false;
        }
        return false;
    }

    public boolean paste_to_input(AccessibilityNodeInfo accessibilityNodeInfo, String viewId, int index, String contentToPaste) {
        if (accessibilityNodeInfo != null) {
            // 获取剪贴板服务
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建一个新的文本剪贴数据
            ClipData clip = ClipData.newPlainText("label", contentToPaste);
            // 设置剪贴板的内容
            clipboard.setPrimaryClip(clip);

            // 查找具有指定ID的节点
            List<AccessibilityNodeInfo> nodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(viewId);
            if (nodeInfos.isEmpty() || nodeInfos.size() > index + 1) {
                return false;
            }
            AccessibilityNodeInfo nodeInfo = nodeInfos.get(index);
            if (nodeInfo.getClassName().equals("android.widget.EditText") && nodeInfo.isEditable()) {
                // 尝试给EditText设置焦点
                if (nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)) {
                    // 等待焦点设置完成
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 执行粘贴操作
                return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
        }
        return false;
    }


    public boolean paste_to_input_index(AccessibilityNodeInfo nodeInfo, String contentToPaste) {
        if (nodeInfo != null) {
            // 获取剪贴板服务
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建一个新的文本剪贴数据
            ClipData clip = ClipData.newPlainText("label", contentToPaste);
            // 设置剪贴板的内容
            clipboard.setPrimaryClip(clip);

            if (nodeInfo.getClassName().equals("android.widget.EditText") && nodeInfo.isEditable()) {
                // 尝试给EditText设置焦点
                if (nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)) {
                    // 等待焦点设置完成
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 执行粘贴操作
                return nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }
        }
        return false;
    }

    public AccessibilityNodeInfo findFirstNodeByContentDesc(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (a(accessibilityNodeInfo.getContentDescription()).equals(str)) {
                    return accessibilityNodeInfo;
                }
                return null;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (child.getContentDescription() != null && a(child.getContentDescription()).equals(str)) {
                        return child;
                    }
                    AccessibilityNodeInfo findFirstNodeByContentDesc = findFirstNodeByContentDesc(child, str);
                    if (findFirstNodeByContentDesc != null) {
                        return findFirstNodeByContentDesc;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public AccessibilityNodeInfo findFirstNodeById(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (accessibilityNodeInfo != null) {
            Iterator<AccessibilityNodeInfo> it = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(str).iterator();
            if (it.hasNext()) {
                return it.next();
            }
            return null;
        }
        return null;
    }

    public AccessibilityNodeInfo findFirstNodeByNotFullContentDesc(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (a(accessibilityNodeInfo.getContentDescription()).contains(str)) {
                    return accessibilityNodeInfo;
                }
                return null;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (child.getContentDescription() != null && a(child.getContentDescription()).contains(str)) {
                        return child;
                    }
                    AccessibilityNodeInfo findFirstNodeByNotFullContentDesc = findFirstNodeByNotFullContentDesc(child, str);
                    if (findFirstNodeByNotFullContentDesc != null) {
                        return findFirstNodeByNotFullContentDesc;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public AccessibilityNodeInfo findFirstNodeByNotFullText(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (a(accessibilityNodeInfo.getText()).contains(str)) {
                    return accessibilityNodeInfo;
                }
                return null;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (child.getText() != null && a(child.getText()).contains(str)) {
                        return child;
                    }
                    AccessibilityNodeInfo findFirstNodeByNotFullText = findFirstNodeByNotFullText(child, str);
                    if (findFirstNodeByNotFullText != null) {
                        return findFirstNodeByNotFullText;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public AccessibilityNodeInfo findFirstNodeByText(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (a(accessibilityNodeInfo.getText()).equals(str)) {
                    return accessibilityNodeInfo;
                }
                return null;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (child.getText() != null && a(child.getText()).equals(str)) {
                        return child;
                    }
                    AccessibilityNodeInfo findFirstNodeByText = findFirstNodeByText(child, str);
                    if (findFirstNodeByText != null) {
                        return findFirstNodeByText;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public AccessibilityNodeInfo findFirstNodeByTextList(AccessibilityNodeInfo accessibilityNodeInfo, List<String> list) {
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (list.contains(a(accessibilityNodeInfo.getText()))) {
                    return accessibilityNodeInfo;
                }
                return null;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (child.getText() != null && list.contains(a(child.getText()))) {
                        return child;
                    }
                    AccessibilityNodeInfo findFirstNodeByTextList = findFirstNodeByTextList(child, list);
                    if (findFirstNodeByTextList != null) {
                        return findFirstNodeByTextList;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public AccessibilityNodeInfo findNodeById(String str, int i) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(str);
        if (findAccessibilityNodeInfosByViewId.size() >= i + 1) {
            return findAccessibilityNodeInfosByViewId.get(i);
        }
        return null;
    }

    public AccessibilityNodeInfo findNodeText(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (accessibilityNodeInfo.getText() != null) {
                    this.c.invokeAndKeepAlive(new UniMessage("text", accessibilityNodeInfo.getText().toString()));
                }
                if (accessibilityNodeInfo.getContentDescription() != null) {
                    this.c.invokeAndKeepAlive(new UniMessage("text", accessibilityNodeInfo.getContentDescription().toString()));
                }
                return accessibilityNodeInfo;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (child.getText() != null) {
                        this.c.invokeAndKeepAlive(new UniMessage("text", child.getText().toString()));
                    }
                    if (child.getContentDescription() != null) {
                        this.c.invokeAndKeepAlive(new UniMessage("text", child.getContentDescription().toString()));
                    }
                    AccessibilityNodeInfo findNodeText = findNodeText(child);
                    if (findNodeText != null) {
                        return findNodeText;
                    }
                }
            }
            return null;
        }
        return null;
    }

    public boolean forceClick(AccessibilityNodeInfo accessibilityNodeInfo) {
        Rect rect = new Rect();
        accessibilityNodeInfo.getBoundsInScreen(rect);
        if (accessibilityNodeInfo.isVisibleToUser()) {
            return tap_location(rect.centerX(), rect.centerY());
        }
        return false;
    }

    public boolean global_back() {
        return mAccService.performGlobalAction(1);
    }

    public boolean global_home() {
        return mAccService.performGlobalAction(2);
    }

    public boolean global_recents() {
        return mAccService.performGlobalAction(3);
    }

    public boolean hideFloatView() {
        AccessibilityFloatView accessbilityFloatView = this.e;
        if (accessbilityFloatView == null) {
            return false;
        }
        accessbilityFloatView.hideView();
        return true;
    }

    public void initFloatView(boolean z, String str) {
        this.e = new AccessibilityFloatView(mAccService, z, str);
    }

    public boolean native_click_bt(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        for (AccessibilityNodeInfo accessibilityNodeInfo2 : accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(str)) {
            if(longClick && accessibilityNodeInfo2.isVisibleToUser()) {
                return performLongClick(accessibilityNodeInfo2);
            }
            if (accessibilityNodeInfo2.isVisibleToUser()) {
                return accessibilityNodeInfo2.performAction(16);
            }
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if(!this.isStart || this.c == null) return; // 未开始执行 直接 return
        try {
            if (this.b) {
                this.d.clear();
                this.d.put("event_code", accessibilityEvent.getEventType());
                this.d.put("event_type", getEventTypeDescription(accessibilityEvent.getEventType()));
                this.d.put("package_name", accessibilityEvent.getPackageName());
                this.d.put("class_name", accessibilityEvent.getClassName());
                this.d.put("notification_desc", enentTypeHasNotification(accessibilityEvent));
                this.c.invokeAndKeepAlive(this.d);
                return;
            }

            UniMessage uniMessage = new UniMessage();
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                uniMessage.setType("用户点击");
            } else if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
                uniMessage.setType("用户长按");
            } else if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                uniMessage.setType("输入框更改了输入内容");
            } else if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                uniMessage.setType("页面状态变化了");
            } else if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED) {
                uniMessage.setType("点击组件事件");
            }else if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                uniMessage.setType("Notification变化");
                uniMessage.setNotificationDesc(enentTypeHasNotification(accessibilityEvent));
            }
//            String strType = this.getEventTypeDescription(accessibilityEvent.getEventType());
//            uniMessage.setType(strType);
            if (uniMessage.getType() != null) {
                AccessibilityNodeInfo source = accessibilityEvent.getSource();
                if (source != null) {
                    uniMessage.setAccNode(MyTool.getNodeInform(source));
                    if (uniMessage.getType().equals("TYPE_VIEW_CLICKED") || uniMessage.getType().equals("TYPE_VIEW_LONG_CLICKED")) {
                        findNodeText(source);
                    }
                }
                this.c.invokeAndKeepAlive(uniMessage);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        this.c = null;
        mAccService = null;
        this.d = new HashMap();
        super.onDestroy();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        mAccService = this;
    }

    private HashMap<Object, Object> enentTypeHasNotification(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            // 获取通知信息
            Parcelable data = accessibilityEvent.getParcelableData();
            if (data instanceof Notification) {
                Notification notification = (Notification) data;
                // 获取通知的标题
                String title = null;
                title = notification.extras.getString(Notification.EXTRA_TITLE);
                // 获取通知的内容文本
                String text = null;
                text = notification.extras.getString(Notification.EXTRA_TEXT);
                // 获取通知的子标题
                String subText = null;
                subText = notification.extras.getString(Notification.EXTRA_SUB_TEXT);
                HashMap<Object, Object> hashMap = new HashMap<>();
                if (title != null && !title.isEmpty()) hashMap.put("title", title);
                if (text != null && !text.isEmpty()) hashMap.put("text", text);
                if (subText != null && !subText.isEmpty()) hashMap.put("subText", subText);
                this.tempAccessibilityEvent = accessibilityEvent;
                return hashMap;
            }
        }
        return null;
    }

    public boolean performNotificationClick() {
        AccessibilityEvent event = this.tempAccessibilityEvent;
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            PendingIntent pendingIntent = notification.contentIntent;
            // pendingIntent.send();
            Intent broadcastIntent = new Intent(this, MyBroadcastReceiver.class);
            broadcastIntent.putExtra("pending_intent", pendingIntent);
            sendBroadcast(broadcastIntent);
            return true;
        }
        return false;
    }

    public String getEventTypeDescription(int eventType) {
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "View被点击";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "View被长按";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "View被选中";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "View获得焦点";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "View文本变化";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "打开了一个PopupWindow，Menu或Dialog";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "Notification变化";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "一个View进入悬停";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "一个View退出悬停";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "触摸浏览事件开始";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "触摸浏览事件完成";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "窗口的内容发生变化，或子树根布局发生变化";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "View滚动";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "Edittext文字选中发生改变事件";
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "应用产生一个通知事件";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                return "获得无障碍焦点事件";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                return "无障碍焦点事件清除";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "在给定的移动粒度下遍历视图文本的事件";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "开始手势监测";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "结束手势监测";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "触摸屏幕事件开始";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "触摸屏幕事件结束";
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "屏幕上的窗口变化事件";
            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
                return "View中的上下文点击事件";
            case AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT:
                return "辅助用户读取当前屏幕事件";
            default:
                return "其他事件类型";
        }
    }

    public void set_acc_config(String[] strArr, long j) {
        AccessibilityServiceInfo serviceInfo = getServiceInfo();
        serviceInfo.packageNames = strArr;
        serviceInfo.notificationTimeout = j;
        setServiceInfo(serviceInfo);
    }

    public void set_callback(UniJSCallback uniJSCallback, boolean z) {
        this.c = uniJSCallback;
        this.b = z;
    }

    public boolean showFloatView() {
        AccessibilityFloatView accessbilityFloatView = this.e;
        if (accessbilityFloatView == null) {
            return false;
        }
        accessbilityFloatView.showView();
        return true;
    }

    public boolean tap_bt(AccessibilityNodeInfo accessibilityNodeInfo) {
        if(longClick && accessibilityNodeInfo.isVisibleToUser()) {
            return performLongClick(accessibilityNodeInfo);
        }
        if (accessibilityNodeInfo.isVisibleToUser() && !accessibilityNodeInfo.performAction(16)) {
            return forceClick(accessibilityNodeInfo);
        }
        return false;
    }

    public boolean tap_bt(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        for (AccessibilityNodeInfo accessibilityNodeInfo2 : accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(str)) {
            if(longClick && accessibilityNodeInfo2.isVisibleToUser()) {
                return performLongClick(accessibilityNodeInfo2);
            }
            if (accessibilityNodeInfo2.isVisibleToUser() && !accessibilityNodeInfo2.performAction(16)) {
                return forceClick(accessibilityNodeInfo2);
            }
        }
        return false;
    }

    public boolean tap_location(int i, int i2) {
        if (Build.VERSION.SDK_INT >= 24) {
            Path path = new Path();
            path.moveTo(i, i2);
            return mAccService.dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, 0L, 20L)).build(), null, null);
        }
        return false;
    }

    private boolean performLongClick(AccessibilityNodeInfo nodeInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && nodeInfo != null) {
            boolean isClick = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            setPerformLongClick(false);
            return isClick;
        }
        setPerformLongClick(false);
        return false;
    }

    public void setPerformLongClick(boolean isLongClick) {
        this.longClick = isLongClick;
    }

    /**
     * 查找所有与指定文字匹配的节点，并返回其中心坐标列表
     *
     * @param targetText 要查找的文字
     * @return List<List<Integer>> 包含所有符合条件的节点的中心坐标，每个坐标为一个 List [x, y]
     */
    public List<List<Integer>> getCenterCoordinatesByText(String targetText) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<List<Integer>> centerCoordinatesList = new ArrayList<>();  // 存储中心坐标的二维列表
        if (rootNode != null) {
            findNodesRecursive(rootNode, targetText, centerCoordinatesList);
        }
        return centerCoordinatesList;
    }

    /**
     * 递归查找包含指定文字的节点并计算其中心坐标
     *
     * @param node                 当前遍历的节点
     * @param targetText           要匹配的文字
     * @param centerCoordinatesList 中心坐标的二维列表
     */
    private void findNodesRecursive(AccessibilityNodeInfo node, String targetText, List<List<Integer>> centerCoordinatesList) {
        if (node == null) return;

        // 检查节点是否包含指定文字
        if (node.getText() != null && targetText.equals(node.getText().toString())) {
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);  // 获取节点的屏幕坐标

            // 计算中心点坐标
            int centerX = (bounds.left + bounds.right) / 2;
            int centerY = (bounds.top + bounds.bottom) / 2;

            // 将中心点坐标保存为 List [x, y]
            List<Integer> centerCoordinates = new ArrayList<>();
            centerCoordinates.add(centerX);
            centerCoordinates.add(centerY);

            centerCoordinatesList.add(centerCoordinates);  // 将节点的中心坐标加入到二维列表中
        }

        // 递归遍历所有子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            findNodesRecursive(node.getChild(i), targetText, centerCoordinatesList);
        }
    }

}
