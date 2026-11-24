package cn.lincq.accessibility.tool;

import android.content.BroadcastReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class ScreenOnReceiver extends BroadcastReceiver {

    private PowerManager.WakeLock wakeLock;
    private static PowerManager.WakeLock wakeLock1;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::WakeLock");
        wakeLock.acquire(10000); // 唤醒屏幕10秒

        // 在这里执行唤醒屏幕后的操作，例如启动一个Activity或Service

        // 释放唤醒锁
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public static void scheduleWakeUp(Context context, long delayMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScreenOnReceiver.class);
        intent.setAction("cn.lincq.accessibility.WAKE_UP_SCREEN");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 设置定时任务
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMillis, pendingIntent);
    }
}
