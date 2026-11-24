package cn.lincq.hmsscan;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import cn.lincq.hmsscan.utils.ResultJsonObject;

public class ScanActivity extends AppCompatActivity {
    public static UniJSCallback callback = null;
    public static HmsScanAnalyzerOptions options = null;
    public static final int REQUEST_CODE_SCAN = 1008;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startQrCodeScanner();
    }

    private void startQrCodeScanner() {
        // 调用HmsScan API启动二维码扫描
        ScanUtil.startScan(this, REQUEST_CODE_SCAN, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && requestCode == REQUEST_CODE_SCAN) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null && obj.getOriginalValue() != null) {
                String scanResult = obj.getOriginalValue();
                // 获取扫码类型
                int scanType = obj.getScanType();
                // 在这里处理扫描结果
                if (callback != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("result", scanResult);
                    jsonObject.put("scanType", scanType);
                    ResultJsonObject resultJsonObject = new ResultJsonObject();
                    callback.invoke(resultJsonObject.returnSuccess(jsonObject, "扫码成功"));
                }
            }
        }
        finish();
    }

}
