package cn.lincq.hmsscan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import cn.lincq.hmsscan.utils.ResultJsonObject;
import cn.lincq.hmsscan.utils.Tools;

public class ScanModule extends BaseModule {

    private static final int PERMISSION_REQUEST_CODE = 1008; // 使用同一个请求码
    private UniJSCallback callback;
    private HmsScanAnalyzerOptions options;

    @UniJSMethod(uiThread = true)
    public void scanCode(UniJSCallback callback, JSONObject options) {
        this.callback = callback;
        if (options != null) {
            // 可以在这里处理传入的 options 参数
            this.options = toHmsScanAnalyzerOptions(options);
        }
        // 检查并请求相机和媒体文件访问权限
         requestPermissions();
    }

    public HmsScanAnalyzerOptions toHmsScanAnalyzerOptions(JSONObject options) {
        // 假设HmsScanAnalyzerOptions和HmsScan类已经定义
        int[] hmsScanTypes = options.getJSONArray("hmsScanTypes") != null ? Tools.toIntArray(options.getJSONArray("hmsScanTypes")) : null;
        if(hmsScanTypes == null) {
            hmsScanTypes = new int[]{HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE};
        }
        HmsScanAnalyzerOptions hmsOptions = new HmsScanAnalyzerOptions.Creator()
                // 根据传入的options设置HmsScanAnalyzerOptions，如果键不存在则使用默认值
                .setShowGuide(options.getBoolean("showGuide") != null ? options.getBoolean("showGuide") : true) // 默认为true
                .setHmsScanTypes(0, hmsScanTypes)
                .setViewType(options.getInteger("viewType") != null ? options.getInteger("viewType") : 0) // 默认为0
                .setPhotoMode(options.getBoolean("photoMode") != null ? options.getBoolean("photoMode") : true) // 默认为true
                .setErrorCheck(options.getBoolean("errorCheck") != null ? options.getBoolean("errorCheck") : true) // 默认为true
                .create();
        return hmsOptions;
    }

    // 请求相机和媒体文件访问权限
    private void requestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        boolean shouldRequestPermissions = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                shouldRequestPermissions = true;
                break;
            }
        }

        if (shouldRequestPermissions) {
            // 如果没有权限，则请求权限
            ActivityCompat.requestPermissions(getActivity(),
                    permissions,
                    PERMISSION_REQUEST_CODE);
        } else {
            // 如果已经有所有需要的权限，直接打开扫描页面
            openScanPage();
        }
    }

    // 打开扫描页面
    private void openScanPage() {
        if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            ScanActivity.callback = callback;
            ScanActivity.options = options;
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            getActivity().startActivity(intent);
        }
    }

    // 处理权限请求结果
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 检查请求是否被授予
            if (grantResults.length > 0) {
                boolean allPermissionsGranted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

                if (allPermissionsGranted) {
                    // 所有权限已授予，可以打开扫描页面
                    openScanPage();
                } else {
                    // 权限被拒绝，可以给用户提示或者禁用相关功能
                    // 可以在这里处理权限被拒绝的情况
                    if (callback != null) {
                        ResultJsonObject resultJsonObject = new ResultJsonObject();
                        callback.invoke(resultJsonObject.returnFailed("相机和媒体文件访问权限被拒绝，无法使用该功能，请开启！"));
                    }
                }
            }
        }
    }
}
