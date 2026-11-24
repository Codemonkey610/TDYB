package cn.lincq.hmsscan.utils;

import com.alibaba.fastjson.JSONArray;

public class Tools {
    // 辅助方法，将JSONArray转换为int数组
    public static int[] toIntArray(JSONArray jsonArray) {
        int[] intArray = new int[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            intArray[i] = jsonArray.getInteger(i).intValue();
        }
        return intArray;
    }
}
