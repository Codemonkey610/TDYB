package cn.lincq.floatweb.plugindata;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResultJsonObject {
    private int code;
    private Object data;
    private String message;

    public JSONObject returnSuccess(Object obj, String str) {
        this.code = 0;
        this.data = obj;
        this.message = str;
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("code", (Object) Integer.valueOf(this.code));
        jSONObject.put("data", this.data);
        jSONObject.put("msg", (Object) this.message);
        return jSONObject;
    }

    public JSONObject returnSuccess() {
        return returnSuccess(new HashMap(), "");
    }

    public JSONObject returnSuccess(String str) {
        return returnSuccess(new HashMap(), str);
    }

    public JSONObject returnFailed(int i, Object obj, String str) {
        this.code = i;
        this.data = obj;
        this.message = str;
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("code", (Object) Integer.valueOf(this.code));
        jSONObject.put("data", this.data);
        jSONObject.put("msg", (Object) this.message);

        return jSONObject;
    }

    public JSONObject returnFailed(int i, String str) {
        return returnFailed(i, new HashMap(), str);
    }

    public JSONObject returnFailed(String str) {
        return returnFailed(-1, new HashMap(), str);
    }

    public Map<String, Object> returnEventData(Object obj) {
        HashMap hashMap = new HashMap();
        hashMap.put("detail", obj);
        return hashMap;
    }
}
