package cn.lincq.hmsscan;

import android.app.Activity;
import android.content.Context;

import com.taobao.weex.common.WXModule;

public class BaseModule extends WXModule {

    public Context getContext() {
        return this.mUniSDKInstance.getContext();
    }

    public Activity getActivity() {
        return (Activity) getContext();
    }
}
