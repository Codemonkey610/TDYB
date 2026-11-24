package cn.lincq.accessibility.bean;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import cn.lincq.accessibility.service.AccService;

public class AccNode {
    private Rect bounds;
    private Boolean checkable;
    private Boolean checked;
    private int childCount;
    private String className;
    private Boolean clickable;
    private String contentDesc;
    private Boolean enable;
    private Boolean focusable;
    private Boolean focused;
    private Boolean longClickable;
    private String packageName;
    private Boolean password;
    private Boolean scrollable;
    private Boolean selected;
    private String text;
    private String id;

    public Rect getBounds() {
        return this.bounds;
    }

    public Boolean getCheckable() {
        return this.checkable;
    }

    public Boolean getChecked() {
        return this.checked;
    }

    public int getChildCount() {
        return this.childCount;
    }

    public String getClassName() {
        return this.className;
    }

    public Boolean getClickable() {
        return this.clickable;
    }

    public String getContentDesc() {
        return this.contentDesc;
    }

    public Boolean getEnable() {
        return this.enable;
    }

    public Boolean getFocusable() {
        return this.focusable;
    }

    public Boolean getFocused() {
        return this.focused;
    }

    public Boolean getLongClickable() {
        return this.longClickable;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Boolean getPassword() {
        return this.password;
    }

    public Boolean getScrollable() {
        return this.scrollable;
    }

    public Boolean getSelected() {
        return this.selected;
    }

    public String getText() {
        return this.text;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String str) {
        String trim = str.trim();
        if (trim.equals("null")) {
            trim = "";
        }
        this.id = trim;
    }

    public void setBounds(Rect rect) {
        this.bounds = rect;
    }

    public void setCheckable(Boolean bool) {
        this.checkable = bool;
    }

    public void setChecked(Boolean bool) {
        this.checked = bool;
    }

    public void setChildCount(int i) {
        this.childCount = i;
    }

    public void setClassName(String str) {
        this.className = str;
    }

    public void setClickable(Boolean bool) {
        this.clickable = bool;
    }

    public void setContentDesc(String str) {
        this.contentDesc = str;
    }

    public void setEnable(Boolean bool) {
        this.enable = bool;
    }

    public void setFocusable(Boolean bool) {
        this.focusable = bool;
    }

    public void setFocused(Boolean bool) {
        this.focused = bool;
    }

    public void setLongClickable(Boolean bool) {
        this.longClickable = bool;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public void setPassword(Boolean bool) {
        this.password = bool;
    }

    public void setScrollable(Boolean bool) {
        this.scrollable = bool;
    }

    public void setSelected(Boolean bool) {
        this.selected = bool;
    }

    public void setText(String str) {
        this.text = str;
    }

    public boolean edit(String str) {
        String id = getId();
        AccessibilityNodeInfo rootInActiveWindow = AccService.mAccService.getRootInActiveWindow();
        if (rootInActiveWindow != null) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : rootInActiveWindow.findAccessibilityNodeInfosByViewId(id)) {
                if (accessibilityNodeInfo.getClassName().equals("android.widget.EditText")) {
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", str);
                    accessibilityNodeInfo.performAction(1);
                    return accessibilityNodeInfo.performAction(2097152, bundle);
                }
            }
            return false;
        }
        return false;
    }
}
