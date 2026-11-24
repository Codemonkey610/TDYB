package cn.lincq.accessibility.bean;

import java.util.HashMap;

public class UniMessage {
    public AccNode accNode;
    private String accessibilityNodeInfo;
    private boolean state;
    private String type;
    public HashMap<Object, Object> notificationDesc;

    public UniMessage() {
    }

    public UniMessage(String str, String str2) {
        this.accessibilityNodeInfo = str2;
        this.type = str;
        this.state = this.state;
    }

    public UniMessage(String str, boolean z) {
        this.type = str;
        this.state = z;
    }

    public AccNode getAccNode() {
        return this.accNode;
    }

    public String getAccessibilityNodeInfo() {
        return this.accessibilityNodeInfo;
    }

    public String getType() {
        return this.type;
    }

    public boolean isState() {
        return this.state;
    }

    public void setAccNode(AccNode accNode) {
        this.accNode = accNode;
    }

    public void setAccessibilityNodeInfo(String str) {
        this.accessibilityNodeInfo = str;
    }

    public void setState(boolean z) {
        this.state = z;
    }

    public void setType(String str) {
        this.type = str;
    }

    public void setNotificationDesc(HashMap<Object, Object> hash) {
        this.notificationDesc = hash;
    }
}
