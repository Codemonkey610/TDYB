package cn.lincq.hmsscan.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraPermissionHelper {

    // 请求相机权限的请求码，可以自定义
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // 检查是否已经授予相机权限
    public static boolean hasCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // 请求相机权限
    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    // 处理权限请求结果
    public static boolean handleCameraPermissionResult(int requestCode, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 相机权限已授予
                return true;
            } else {
                // 相机权限被拒绝
                return false;
            }
        }
        return false;
    }
}
