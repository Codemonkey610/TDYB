package cn.lincq.accessibility;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import cn.lincq.accessibility.bean.AccNode;
import cn.lincq.accessibility.service.AccService;
import cn.lincq.accessibility.tool.AccessibilityUtil;
import cn.lincq.accessibility.tool.MyTool;
import cn.lincq.accessibility.tool.ScreenOnReceiver;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class AccessibilityModule extends UniModule {
    private final String TAG = getClass().getSimpleName();
    public UniJSCallback jsCallback = null;
    private final List<AccessibilityNodeInfo> nodeInfoList = new ArrayList();

    private AccNode getNodeInform(AccessibilityNodeInfo accessibilityNodeInfo, String[] strArr) {
        if (accessibilityNodeInfo != null) {
            AccNode accNode = new AccNode();
            for (String str : strArr) {
                if (str.equals("childCount")) {
                    accNode.setChildCount(accessibilityNodeInfo.getChildCount());
                }
                if (str.equals("id")) {
                    accNode.setId(String.format("%s", accessibilityNodeInfo.getViewIdResourceName()));
                }
                if (str.equals("className")) {
                    accNode.setClassName(MyTool.char2string(accessibilityNodeInfo.getClassName()));
                }
                if (str.equals("text")) {
                    accNode.setText(MyTool.char2string(accessibilityNodeInfo.getText()));
                }
                if (str.equals("packageName")) {
                    accNode.setPackageName(MyTool.char2string(accessibilityNodeInfo.getPackageName()));
                }
                if (str.equals("contentDesc")) {
                    accNode.setContentDesc(MyTool.char2string(accessibilityNodeInfo.getContentDescription()));
                }
                if (str.equals("checkable")) {
                    accNode.setCheckable(accessibilityNodeInfo.isCheckable());
                }
                if (str.equals(Constants.Name.CHECKED)) {
                    accNode.setChecked(accessibilityNodeInfo.isChecked());
                }
                if (str.equals("clickable")) {
                    accNode.setClickable(accessibilityNodeInfo.isClickable());
                }
                if (str.equals("enable")) {
                    accNode.setEnable(accessibilityNodeInfo.isEnabled());
                }
                if (str.equals("focusable")) {
                    accNode.setFocusable(accessibilityNodeInfo.isFocusable());
                }
                if (str.equals("focused")) {
                    accNode.setFocused(accessibilityNodeInfo.isFocused());
                }
                if (str.equals(Constants.Name.SCROLLABLE)) {
                    accNode.setScrollable(accessibilityNodeInfo.isScrollable());
                }
                if (str.equals("longClickable")) {
                    accNode.setLongClickable(accessibilityNodeInfo.isLongClickable());
                }
                if (str.equals(Constants.Value.PASSWORD)) {
                    accNode.setPassword(accessibilityNodeInfo.isPassword());
                }
                if (str.equals("selected")) {
                    accNode.setSelected(accessibilityNodeInfo.isSelected());
                }
                if (str.equals("bounds")) {
                    Rect rect = new Rect();
                    accessibilityNodeInfo.getBoundsInScreen(rect);
                    accNode.setBounds(rect);
                }
            }
            return accNode;
        }
        return null;
    }

    //    判断是否有悬浮窗权限
    @UniJSMethod(uiThread = false)
    public boolean checkFloatPermission() {
        if (MyTool.checkFloatPermission(this.mWXSDKInstance.getContext())) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            MyTool.requestFloatPermission(this.mWXSDKInstance.getContext());
            return false;
        }
        return false;
    }

    @UniJSMethod(uiThread = false)
    public void setPerformLongClick(Boolean isLong) {
        if (isLong == null) {
            isLong = true;
        }
        AccService.mAccService.setPerformLongClick(isLong);
    }

    //    点击符合id的节点，可以先用getNodeSizeById获取数量
    @UniJSMethod(uiThread = false)
    public boolean clickById(String str, int i) {
        return AccService.mAccService.clickNodeById(str, i);
    }

    //    点击第一个文字描述符合的节点
    @UniJSMethod(uiThread = false)
    public boolean clickFirstNodeByDesc(String str) {
        return AccService.mAccService.clickFirstNodeByContentDesc(str);
    }

    //    击第一个文字描述符合包含的节点
    @UniJSMethod(uiThread = false)
    public boolean clickFirstNodeByNotFullDesc(String str) {
        return AccService.mAccService.clickFirstNodeByNotFullContentDesc(str);
    }

    //    点击第一个文字符合包含的节点
    @UniJSMethod(uiThread = false)
    public boolean clickFirstNodeByNotFullText(String str) {
        AccService.mAccService.getRootInActiveWindow();
        return AccService.mAccService.clickFirstNodeByNotFullText(str);
    }

    //    点击第一个文字符合的节点
    @UniJSMethod(uiThread = false)
    public boolean clickFirstNodeByText(String str) {
        return AccService.mAccService.clickFirstNodeByText(str);
    }

    //    点击坐标
    @UniJSMethod(uiThread = false)
    public boolean clickLocation(int i, int i2) {
        return AccService.mAccService.tap_location(i, i2);
    }

    //    输入text
    @UniJSMethod(uiThread = false)
    public boolean edit(String viewId, String contentToPaste) {
        return AccService.mAccService.edit_input(AccService.mAccService.getRootInActiveWindow(), viewId, contentToPaste);
    }

    // 粘贴
    @UniJSMethod(uiThread = false)
    public boolean pasteToInput(String viewId, int index, String contentToPaste) {
        return AccService.mAccService.paste_to_input(AccService.mAccService.getRootInActiveWindow(), viewId, index, contentToPaste);
    }

    //    查询是否存在文字描述符合的节点
    @UniJSMethod(uiThread = false)
    public boolean findFirstNodeByContentDesc(String str) {
        return AccService.mAccService.findFirstNodeByContentDesc(AccService.mAccService.getRootInActiveWindow(), str) != null;
    }

    //    查询是否存在文字描述符合包含的节点
    @UniJSMethod(uiThread = false)
    public boolean findFirstNodeByNotFullDesc(String str) {
        return AccService.mAccService.findFirstNodeByNotFullContentDesc(AccService.mAccService.getRootInActiveWindow(), str) != null;
    }

    //    查询是否存在文字符合包含的节点
    @UniJSMethod(uiThread = false)
    public boolean findFirstNodeByNotFullText(String str) {
        return AccService.mAccService.findFirstNodeByNotFullText(AccService.mAccService.getRootInActiveWindow(), str) != null;
    }

    //    查询是否存在文字符合的节点
    @UniJSMethod(uiThread = false)
    public boolean findFirstNodeByText(String str) {
        return AccService.mAccService.findFirstNodeByText(AccService.mAccService.getRootInActiveWindow(), str) != null;
    }

    //    获取第nodeIndex个id符合的节点的第childIndex个节点信息，最大childIndex可以从childCount获取
    @UniJSMethod(uiThread = false)
    public AccNode getChildNodeInform(String str, int i, int i2) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfoByViewId = AccService.mAccService.getRootInActiveWindow().findAccessibilityNodeInfosByViewId(str);
        if (findAccessibilityNodeInfoByViewId.size() >= i + 1) {
            AccessibilityNodeInfo accessibilityNodeInfo = findAccessibilityNodeInfoByViewId.get(i);
            if (accessibilityNodeInfo.getChildCount() >= i2 + 1) {
                return MyTool.getNodeInform(accessibilityNodeInfo.getChild(i2));
            }
            return null;
        }
        return null;
    }

    //    获取第一个id符合的节点信息
    @UniJSMethod(uiThread = false)
    public AccNode getFirstNodeInformById(String str) {
        return MyTool.getNodeInform(AccService.mAccService.findFirstNodeById(AccService.mAccService.getRootInActiveWindow(), str));
    }

    //    获取第一个符合文字的节点信息，fields包含如下字段
    @UniJSMethod(uiThread = false)
    public AccNode getFirstNodeInformByText(String str) {
        return MyTool.getNodeInform(AccService.mAccService.findFirstNodeByText(AccService.mAccService.getRootInActiveWindow(), str));
    }

    //    获取关于text的第一个节点
    @UniJSMethod(uiThread = false)
    public AccNode text(String str) {
        return MyTool.getNodeInform(AccService.mAccService.findFirstNodeByText(AccService.mAccService.getRootInActiveWindow(), str));
    }

    //    获取第index个id符合的节点信息
    @UniJSMethod(uiThread = false)
    public AccNode getNodeInformById(String str, int i) {
        AccService.mAccService.getRootInActiveWindow();
        return MyTool.getNodeInform(AccService.mAccService.findNodeById(str, i));
    }

    //    获取id符合的节点数量
    @UniJSMethod(uiThread = false)
    public int getNodeSizeById(String str) {
        AccessibilityNodeInfo rootInActiveWindow = AccService.mAccService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            return rootInActiveWindow.findAccessibilityNodeInfosByViewId(str).size();
        }
        return 0;
    }

    @UniJSMethod(uiThread = false)
    public void getTagState(List<String> list, UniJSCallback uniJSCallback) {
        HashMap hashMap = new HashMap();
        AccService accService = AccService.mAccService;
        if (accService == null) {
            hashMap.put("state", -1);
            hashMap.put("des", "请打开无障碍服务");
            uniJSCallback.invoke(hashMap);
            return;
        }
        AccessibilityNodeInfo rootInActiveWindow = accService.getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            hashMap.put("state", -2);
            hashMap.put("des", "请重新获取");
            uniJSCallback.invoke(hashMap);
            return;
        }
        AccessibilityNodeInfo findFirstNodeByTextList = AccService.mAccService.findFirstNodeByTextList(rootInActiveWindow, list);
        if (findFirstNodeByTextList != null) {
            hashMap.put("state", 1);
        } else {
            hashMap.put("state", 0);
        }
        hashMap.put("text", findFirstNodeByTextList != null ? MyTool.char2string(findFirstNodeByTextList.getText()) : "");
        uniJSCallback.invoke(hashMap);
    }

    //  返回上一页
    @UniJSMethod(uiThread = false)
    public boolean goBack() {
        return AccService.mAccService.global_back();
    }

    //  回到桌面
    @UniJSMethod(uiThread = false)
    public boolean goHome() {
        return AccService.mAccService.global_home();
    }

    //    打开最近任务
    @UniJSMethod(uiThread = false)
    public boolean goRecentTasks() {
        return AccService.mAccService.global_recents();
    }

    //    打开无障碍设置页面
    @UniJSMethod(uiThread = false)
    public void goAliveSetting() {
        WXSDKInstance wXSDKInstance = this.mWXSDKInstance;
        if (wXSDKInstance == null || !(wXSDKInstance.getContext() instanceof Activity)) {
            return;
        }
        try {
            Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.mWXSDKInstance.getContext().startActivity(intent);
        } catch (Exception e) {
            Intent intent2 = new Intent("android.settings.SETTINGS");
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.mWXSDKInstance.getContext().startActivity(intent2);
            e.printStackTrace();
        }
    }

    //    用于后台唤起APP。需要后台弹窗权限
    @UniJSMethod(uiThread = false)
    public void moveAppToFront() {
        WXSDKInstance wXSDKInstance = this.mWXSDKInstance;
        if (wXSDKInstance == null || !(wXSDKInstance.getContext() instanceof Activity)) {
            return;
        }
        MyTool.moveAppToFront(this.mWXSDKInstance.getContext());
    }

    // 滑动屏幕 sx, sy, ey, ex, duration
    @UniJSMethod(uiThread = false)
    public void slideScreen(int i, int i2, int i3, int i4, int i5) {
        AccService.mAccService.slide_screen(i, i2, i3, i4, i5);
    }

    // 手势滑动
    @UniJSMethod(uiThread = false)
    public void performGestureSlideScreen(int[][] points, String duration, Boolean useCurve) {
        if (useCurve == null) useCurve = false;
        if (duration == null) {
            AccService.mAccService.performGestureSlide(points, useCurve);
        }
        AccService.mAccService.performGestureSlide(points, Integer.parseInt(duration), useCurve);
    }

    //    查找界面第一个满足条件的节点并执行对应操作
    @UniJSMethod(uiThread = false)
    public HashMap<Object, Object> node(JSONObject jSONObject, UniJSCallback uniJSCallback) {
        boolean bool;
        HashMap<Object, Object> hashMap = new HashMap<>();
        if (jSONObject == null) {
            jSONObject = new JSONObject();
        }
        String string = jSONObject.containsKey("edit") ? jSONObject.getString("edit") : "";
        if (!jSONObject.containsKey(Constants.Event.CLICK)) {
            bool = false;
        } else {
            bool = jSONObject.getBoolean(Constants.Event.CLICK);
        }
        AccessibilityNodeInfo findNodeWhere = AccessibilityUtil.findNodeWhere(AccService.mAccService.getRootInActiveWindow(), jSONObject);
        if (findNodeWhere != null) {
            if (!string.isEmpty()) {
                if (findNodeWhere.getClassName().equals("android.widget.EditText")) {
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", string);
                    findNodeWhere.performAction(1);
                    hashMap.put("edit", findNodeWhere.performAction(2097152, bundle));
                } else {
                    hashMap.put("edit", false);
                }
            }
            if (bool) {
                hashMap.put(Constants.Event.CLICK, AccService.mAccService.tap_bt(findNodeWhere));
            }
            hashMap.put("accNode", MyTool.getNodeInform(findNodeWhere));
            if (uniJSCallback != null) {
                uniJSCallback.invoke(hashMap);
            }
        }
        return hashMap;
    }

    //    查找所有与指定文字匹配的节点，并返回其中心坐标列表
    @UniJSMethod(uiThread = false)
    public List<List<Integer>> getCenterCoordinatesByText(String str, UniJSCallback uniJSCallback) {
        List<List<Integer>> lists = AccService.mAccService.getCenterCoordinatesByText(str);
        if (uniJSCallback != null) {
            uniJSCallback.invoke(lists);
        }
        return lists;
    }

    //    判断无障碍服务是否开启
    @UniJSMethod(uiThread = false)
    public boolean getServiceState() {
        if (this.mUniSDKInstance.getContext() instanceof Activity) {
            return MyTool.isStartAccessibilityService(this.mUniSDKInstance.getContext(), this.mUniSDKInstance.getContext().getPackageName());
        }
        return false;
    }

    //    获取应用包名
    @UniJSMethod(uiThread = false)
    public String getPackageName() {
        return this.mUniSDKInstance.getContext() instanceof Activity ? this.mUniSDKInstance.getContext().getPackageName() : "";
    }

    //    `start({applicationId, String[] packageNames, notificationTimeout, monitorAllEvent}, callback) -> boolean` 初始化无障碍服务
//      `applicationId` -> 无障碍应用的包名
//      `packageNames` -> 需要监听变化的应用包名
//	    `notificationTimeout` -> 通知栏监听时间
//      `monitorAllEvent` -> 是否监听所有事件
    @UniJSMethod(uiThread = false)
    public boolean start(JSONObject jSONObject, UniJSCallback uniJSCallback) {
//        if(AccService.mAccService.isStart) return false;
        String[] strArr = new String[0];
        if (jSONObject == null) {
            jSONObject = new JSONObject();
        }
        if (jSONObject.containsKey("packageNames")) {
            JSONArray jSONArray = jSONObject.getJSONArray("packageNames");
            String[] strArr2 = new String[jSONArray.size()];
            for (int i = 0; i < jSONArray.size(); i++) {
                strArr2[i] = jSONArray.getString(i);
            }
            strArr = strArr2;
        }
        long longValue = jSONObject.containsKey("notificationTimeout") ? jSONObject.getLong("notificationTimeout") : 20L;
        boolean booleanValue = jSONObject.containsKey("monitorAllEvent") && jSONObject.getBoolean("monitorAllEvent");
        WXSDKInstance wXSDKInstance = this.mWXSDKInstance;
        if (wXSDKInstance != null && (wXSDKInstance.getContext() instanceof Activity) && !MyTool.isStartAccessibilityService(this.mWXSDKInstance.getContext(), getPackageName())) {
            goAliveSetting();
            return false;
        }
        if (uniJSCallback != null && this.jsCallback == null) {
            this.jsCallback = uniJSCallback;
            AccService.mAccService.set_callback(uniJSCallback, booleanValue);
        }
        AccService.mAccService.set_acc_config(strArr, longValue);
        AccService.mAccService.isStart = true;
        return true;
    }

    //    停止
    @UniJSMethod(uiThread = false)
    public boolean stop(UniJSCallback uniJSCallback) {
        if (AccService.mAccService.isStart) {
            AccService.mAccService.isStart = false;
            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("code", 1);
            hashMap.put("msg", "已停止");
            if (uniJSCallback != null)
                uniJSCallback.invoke(hashMap);
            return true;
        }
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("code", -1);
        hashMap.put("msg", "未运行");
        if (uniJSCallback != null)
            uniJSCallback.invoke(hashMap);
        return false;
    }

    //    获取运行状态
    @UniJSMethod(uiThread = false)
    public boolean getState(UniJSCallback uniJSCallback) {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("code", -1);
        hashMap.put("msg", "");
        hashMap.put("status", AccService.mAccService.isStart);
        if (uniJSCallback != null)
            uniJSCallback.invoke(hashMap);
        return AccService.mAccService.isStart;
    }

    //    遍历节点
    @UniJSMethod(uiThread = false)
    public void forNodes(final UniJSCallback jsCallback) {
        this.nodeInfoList.clear();
        AccessibilityUtil.traverseNode(AccService.mAccService.getRootInActiveWindow(), 0, "-0-", new AccessibilityUtil.Callback() {
            public boolean validate(AccessibilityNodeInfo param1AccessibilityNodeInfo, int param1Int, String param1String) {
                AccessibilityModule.this.nodeInfoList.add(param1AccessibilityNodeInfo);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("index", AccessibilityModule.this.nodeInfoList.size() - 1);
                hashMap.put("depth", param1Int);
                hashMap.put("path", param1String);
                hashMap.put("accNode", MyTool.getNodeInform(param1AccessibilityNodeInfo));
                if (jsCallback != null)
                    jsCallback.invokeAndKeepAlive(hashMap);
                return false;
            }
        });
    }

    //    获取屏幕所有节点信息
    @UniJSMethod(uiThread = false)
    public void getAllNodes(final UniJSCallback jsCallback) {
        this.nodeInfoList.clear();
        final List<HashMap<Object, Object>> result = new ArrayList<>(); // 用于存储遍历结果
        final AtomicInteger counter = new AtomicInteger(0); // 用于计数，确保输出顺序一致
        final CountDownLatch latch = new CountDownLatch(1); // 用于等待遍历完成

        AccessibilityUtil.traverseNode(AccService.mAccService.getRootInActiveWindow(), 0, "-0-", new AccessibilityUtil.Callback() {
            public boolean validate(AccessibilityNodeInfo param1AccessibilityNodeInfo, int param1Int, String param1String) {
                AccessibilityModule.this.nodeInfoList.add(param1AccessibilityNodeInfo);
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("index", AccessibilityModule.this.nodeInfoList.size() - 1);
                hashMap.put("depth", param1Int);
                hashMap.put("path", param1String);
                hashMap.put("accNode", MyTool.getNodeInform(param1AccessibilityNodeInfo));

                int index = counter.getAndIncrement(); // 获取当前索引值
                result.add(index, hashMap); // 按照索引添加到集合中，保持顺序一致

                // 如果遍历到最后一个节点，释放 latch
                if (counter.get() == AccessibilityModule.this.nodeInfoList.size()) {
                    latch.countDown();
                }
                return false;
            }
        });

        try {
            // 等待遍历完成
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 遍历完成后，一次性输出结果（保持顺序一致）
        UniJSCallback uniJSCallback = jsCallback;
        if (uniJSCallback != null)
            uniJSCallback.invoke(result);
    }

    //    编辑节点text
    @UniJSMethod(uiThread = false)
    public boolean editByIndex(int i, String str) {
        if (this.nodeInfoList.size() >= i + 1) {
            AccessibilityNodeInfo accessibilityNodeInfo = this.nodeInfoList.get(i);
            if (accessibilityNodeInfo.getClassName().equals("android.widget.EditText")) {
                Bundle bundle = new Bundle();
                bundle.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", str);
                accessibilityNodeInfo.performAction(1);
                return accessibilityNodeInfo.performAction(2097152, bundle);
            }
            return false;
        }
        return false;
    }

    @UniJSMethod(uiThread = false)
    public boolean pasteToInputByIndex(int i, String contentToPaste) {
        if (this.nodeInfoList.size() >= i + 1) {
            AccessibilityNodeInfo accessibilityNodeInfo = this.nodeInfoList.get(i);
            return AccService.mAccService.paste_to_input_index(accessibilityNodeInfo, contentToPaste);
        }
        return false;
    }

    //    点击节点 index  forNode 遍历函数获得的index
    @UniJSMethod(uiThread = false)
    public boolean clickByIndex(int i) {
        if (this.nodeInfoList.size() >= i + 1) {
            return AccService.mAccService.tap_bt(this.nodeInfoList.get(i));
        }
        return false;
    }

    @UniJSMethod(uiThread = false)
    public boolean performNotificationClick() {
        return AccService.mAccService.performNotificationClick();
    }

    @UniJSMethod(uiThread = false)
    public List<AccNode> getAllWindowNodes() {
        return AccService.mAccService.getAllWindowNodes();
    }

    @UniJSMethod(uiThread = false)
    public boolean clickAllWindowsNodeById(String id, int index) {
        return AccService.mAccService.clickAllWindowsNodeById(id, index);
    }

    @UniJSMethod(uiThread = false)
    public boolean clickAllWindowsNodeByText(String text, int index) {
        return AccService.mAccService.clickAllWindowsNodeByText(text, index);
    }

    @UniJSMethod(uiThread = false)
    public void getAppVersion(String packageName, final UniJSCallback jsCallback) {
        jsCallback.invoke(MyTool.getAppVersion(this.mWXSDKInstance.getContext(), packageName));
    }

    @UniJSMethod(uiThread = false)
    public boolean openSpecifiedApp(String packageName) {
        return MyTool.openSpecifiedApp(this.mWXSDKInstance.getContext(), packageName);
    }

    @UniJSMethod(uiThread = false)
    public void closeAppByPackageName(String packageName) {
        MyTool.closeAppByPackageName(this.mWXSDKInstance.getContext(), packageName, AccService.mAccService);
    }

    @UniJSMethod(uiThread = false)
    public void openAppDetails(String packageName) {
        MyTool.openAppDetails(this.mWXSDKInstance.getContext(), packageName);
    }

    // 唤醒屏幕
    @UniJSMethod(uiThread = false)
    public void scheduleWakeUp(String time) {
        if (time == null) {
            time = "0";
        }
        // 唤醒屏幕
        ScreenOnReceiver.scheduleWakeUp(this.mWXSDKInstance.getContext(), Integer.parseInt(time));
    }
}
