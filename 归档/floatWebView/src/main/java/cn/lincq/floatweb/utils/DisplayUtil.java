package cn.lincq.floatweb.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class DisplayUtil {
    public static int pxToDp(Context context, int cssPixel) {
        float density = context.getResources().getDisplayMetrics().density;
//        int cssPixel = 100; // CSS像素
        int dip = (int) (cssPixel * density); // 转换为设备独立像素
        return dip;
    }

    public static void goToPermissionSetting(Context context) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        Intent intent = new Intent();

        switch (manufacturer) {
            case "huawei":
                intent.setComponent(new ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
                break;
            case "xiaomi":
                intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                break;
            case "oppo":
                intent.setComponent(new ComponentName("com.coloros.notificationmanager",
                        "com.coloros.notificationmanager.app.list.AppListActivity"));
                break;
            case "vivo":
                intent.setComponent(new ComponentName("com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.FloatWindowManager"));
                break;
            case "meizu":
                intent.setComponent(new ComponentName("com.meizu.safe",
                        "com.meizu.safe.permission.SmartBGActivity"));
                break;
            default:
                // 跳转到系统通用的应用信息页面
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                break;
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果失败，使用通用方法打开应用信息页面
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(intent);
        }
    }

}
