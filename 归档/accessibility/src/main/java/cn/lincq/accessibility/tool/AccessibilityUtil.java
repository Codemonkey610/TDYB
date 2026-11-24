package cn.lincq.accessibility.tool;

import android.view.accessibility.AccessibilityNodeInfo;
import cn.lincq.accessibility.bean.AccNode;
import cn.lincq.accessibility.service.AccService;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.el.parse.Operators;

public class AccessibilityUtil {

    public interface Callback {
        boolean validate(AccessibilityNodeInfo accessibilityNodeInfo, int i, String str);
    }

    public static void traverseNode(AccessibilityNodeInfo accessibilityNodeInfo, int i, String str, Callback callback) {
        if (accessibilityNodeInfo == null || callback.validate(accessibilityNodeInfo, i, str)) {
            return;
        }
        for (int i2 = 0; i2 < accessibilityNodeInfo.getChildCount(); i2++) {
            traverseNode(accessibilityNodeInfo.getChild(i2), i + 1, str + i2 + Operators.SUB, callback);
        }
    }

    public static Boolean NodeMatch(AccessibilityNodeInfo accessibilityNodeInfo, JSONObject jSONObject) {
        boolean z = true;
        if (jSONObject == null) {
            jSONObject = new JSONObject();
        }
        String string = jSONObject.containsKey("id") ? jSONObject.getString("id") : null;
        String string2 = jSONObject.containsKey("text") ? jSONObject.getString("text") : null;
        String string3 = jSONObject.containsKey("className") ? jSONObject.getString("className") : null;
        String string4 = jSONObject.containsKey("packageName") ? jSONObject.getString("packageName") : null;
        String string5 = jSONObject.containsKey("contentDesc") ? jSONObject.getString("contentDesc") : null;
        String string6 = jSONObject.containsKey("contentDescMatch") ? jSONObject.getString("contentDescMatch") : null;
        String string7 = jSONObject.containsKey("textMatch") ? jSONObject.getString("textMatch") : null;
        if (accessibilityNodeInfo == null) {
            accessibilityNodeInfo = AccService.mAccService.getRootInActiveWindow();
        }
        if (accessibilityNodeInfo != null) {
            AccNode nodeInform = MyTool.getNodeInform(accessibilityNodeInfo);
            if (string != null && !string.equals(nodeInform.getId())) {
                z = false;
            }
            if (string2 != null && !string2.equals(nodeInform.getText())) {
                z = false;
            }
            if (string7 != null && !nodeInform.getText().matches(string7)) {
                z = false;
            }
            if (string6 != null && !nodeInform.getContentDesc().matches(string6)) {
                z = false;
            }
            if (string3 != null && !string3.equals(nodeInform.getClassName())) {
                z = false;
            }
            if (string4 != null && !string4.equals(nodeInform.getPackageName())) {
                z = false;
            }
            if (string5 == null || string5.equals(nodeInform.getContentDesc())) {
                return z;
            }
            return false;
        }
        return false;
    }

    public static AccessibilityNodeInfo findNodeWhere(AccessibilityNodeInfo accessibilityNodeInfo, JSONObject jSONObject) {
        if (accessibilityNodeInfo == null) {
            accessibilityNodeInfo = AccService.mAccService.getRootInActiveWindow();
        }
        if (accessibilityNodeInfo != null) {
            int childCount = accessibilityNodeInfo.getChildCount();
            if (childCount <= 0) {
                if (NodeMatch(accessibilityNodeInfo, jSONObject)) {
                    return accessibilityNodeInfo;
                }
                return null;
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {
                    if (NodeMatch(child, jSONObject)) {
                        return child;
                    }
                    AccessibilityNodeInfo findNodeWhere = findNodeWhere(child, jSONObject);
                    if (findNodeWhere != null) {
                        return findNodeWhere;
                    }
                }
            }
            return null;
        }
        return null;
    }
}
