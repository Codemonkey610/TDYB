package cn.lincq.accessibility.tool;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import cn.lincq.accessibility.bean.AccNode;
import cn.lincq.accessibility.bean.ScreenProperty;

import com.taobao.weex.WXEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTool {
    public static String char2string(CharSequence charSequence) {
        return charSequence != null ? charSequence.toString() : "";
    }

    public static boolean checkFloatPermission(Context context) {
        boolean canDrawOverlays;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                canDrawOverlays = Settings.canDrawOverlays(context);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            canDrawOverlays = false;
        }
        return canDrawOverlays;
    }

    public static AccNode getNodeInform(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            AccNode accNode = new AccNode();
            accNode.setChildCount(accessibilityNodeInfo.getChildCount());
            accNode.setId(String.format("%s", accessibilityNodeInfo.getViewIdResourceName()));
            accNode.setClassName(char2string(accessibilityNodeInfo.getClassName()));
            accNode.setText(char2string(accessibilityNodeInfo.getText()));
            accNode.setPackageName(char2string(accessibilityNodeInfo.getPackageName()));
            accNode.setContentDesc(char2string(accessibilityNodeInfo.getContentDescription()));
            accNode.setCheckable(Boolean.valueOf(accessibilityNodeInfo.isCheckable()));
            accNode.setChecked(Boolean.valueOf(accessibilityNodeInfo.isChecked()));
            accNode.setClickable(Boolean.valueOf(accessibilityNodeInfo.isClickable()));
            accNode.setEnable(Boolean.valueOf(accessibilityNodeInfo.isEnabled()));
            accNode.setFocusable(Boolean.valueOf(accessibilityNodeInfo.isFocusable()));
            accNode.setFocused(Boolean.valueOf(accessibilityNodeInfo.isFocused()));
            accNode.setScrollable(Boolean.valueOf(accessibilityNodeInfo.isScrollable()));
            accNode.setLongClickable(Boolean.valueOf(accessibilityNodeInfo.isLongClickable()));
            accNode.setPassword(Boolean.valueOf(accessibilityNodeInfo.isPassword()));
            accNode.setSelected(Boolean.valueOf(accessibilityNodeInfo.isSelected()));
            Rect rect = new Rect();
            accessibilityNodeInfo.getBoundsInScreen(rect);
            accNode.setBounds(rect);
            return accNode;
        }
        return null;
    }

    public static ScreenProperty getAndroiodScreenProperty(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        return new ScreenProperty(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    public static String getAppPackageName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        return resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", WXEnvironment.OS));
    }

    public static boolean isAppRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
                if (runningAppProcessInfo.importance == 100 && runningAppProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
            return false;
        }
        throw new AssertionError();
    }

    public static boolean isStartAccessibilityService(Context context, String str) {
        for (AccessibilityServiceInfo accessibilityServiceInfo : ((AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE)).getEnabledAccessibilityServiceList(16)) {
            if (accessibilityServiceInfo.getId().contains(str)) {
                return true;
            }
        }
        return false;
    }

    public static void moveAppToFront(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningTaskInfo runningTaskInfo : activityManager.getRunningTasks(100)) {
                if (runningTaskInfo.topActivity != null) {
                    if (runningTaskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            activityManager.moveTaskToFront(runningTaskInfo.taskId, 0);
                        }
                        return;
                    }
                } else {
                    throw new AssertionError();
                }
            }
            return;
        }
        throw new AssertionError();
    }

    public static void requestFloatPermission(Context context) {
        int i = Build.VERSION.SDK_INT;
        if (i >= 26) {
            Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else if (i >= 23) {
            Intent intent2 = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
            intent2.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent2);
        }
    }

    /**
     * 获取指定包名应用的版本号。
     *
     * @param context     当前应用的上下文。
     * @param packageName 目标应用的包名。
     * @return 返回一个包含版本名称和版本代码的字符串数组，如果应用不存在则返回 null。
     */
    public static Map<String, String> getAppVersion(Context context, String packageName) {
        Map<String, String> version = new HashMap<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            version.put("versionName", versionName);
            version.put("versionCode", String.valueOf(versionCode));
        } catch (Exception e) {
            // 应用不存在或者包名错误
            version.put("error", e.getMessage());
        }
        return version;
    }

    public static boolean openSpecifiedApp(Context context, String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            context.startActivity(launchIntent);
            return true;
        } else {
            // 应用未安装，提示用户安装或处理错误

        }
        return false;
    }

    /**
     * 打开指定应用的详情页面
     *
     * @param context     上下文
     * @param packageName 应用的包名
     */
    public static void openAppDetails(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("包名不能为空");
        }

        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Log.e("AppUtils", "无法打开应用详情页面");
        }
    }

    public static void closeAppByPackageName(Context context, String packageName, AccessibilityService service) {
        // 1. 使用 Intent 跳转到指定应用的设置页面
        openAppDetails(context, packageName);

        // 2. 等待页面加载完成
        try {
            Thread.sleep(1000); // 等待1秒钟以确保设置页面加载完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. 使用无障碍服务查找“强制停止”按钮
        if (service == null || service.getRootInActiveWindow() == null) {
            Log.e("AccessibilityService", "无障碍服务未开启或无活动窗口");
            return;
        }

        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        List<AccessibilityNodeInfo> stopButtons = findStopButton(rootNode);

        // 4. 如果找到了“强制停止”按钮
        if (!stopButtons.isEmpty()) {
            AccessibilityNodeInfo stopButton = stopButtons.get(0);
            stopButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            Log.i("AccessibilityService", "已点击强制停止按钮");

            // 5. 等待确认对话框出现
            try {
                Thread.sleep(1000); // 等待500毫秒以确保确认对话框出现
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 6. 查找确认按钮并点击
            if (!clickConfirmButton(service)) {
                Log.e("AccessibilityService", "未找到确认按钮");
            } else {
                Log.i("AccessibilityService", "已点击确认按钮");
            }
        } else {
            Log.e("AccessibilityService", "未找到强制停止按钮");
        }
    }

    // 辅助方法：查找“强制停止”或近义词按钮
    private static List<AccessibilityNodeInfo> findStopButton(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> buttons = new ArrayList<>();

        // 定义可能的按钮描述列表
        String[] stopButtonTexts = {"强制停止", "结束运行", "停止", "close", "关闭", "结束"};

        // 查找指定文本的按钮
        for (String text : stopButtonTexts) {
            buttons.addAll(rootNode.findAccessibilityNodeInfosByText(text));
        }

        return buttons;
    }

    // 辅助方法：查找并点击确认按钮，遍历所有窗口
    private static boolean clickConfirmButton(AccessibilityService service) {
        // 获取当前所有的窗口
        List<AccessibilityWindowInfo> windows = service.getWindows();
        if (windows == null || windows.isEmpty()) {
            Log.e("AccessibilityService", "没有找到任何窗口");
            return false;
        }

        // 定义可能的确认按钮描述列表
        String[] confirmButtonTexts = {"确定", "确认", "OK", "确定停止", "停止"};

        // 遍历窗口
        for (AccessibilityWindowInfo window : windows) {
            AccessibilityNodeInfo rootNode = window.getRoot();
            if (rootNode == null) continue;

            // 在窗口中查找确认按钮
            for (String text : confirmButtonTexts) {
                List<AccessibilityNodeInfo> confirmButtons = rootNode.findAccessibilityNodeInfosByText(text);
                if (!confirmButtons.isEmpty()) {
                    AccessibilityNodeInfo confirmButton = confirmButtons.get(0);
                    confirmButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;  // 成功点击后返回
                }
            }
        }

        // 如果未找到按钮，则返回 false
        return false;
    }

}
