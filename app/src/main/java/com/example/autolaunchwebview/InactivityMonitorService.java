package com.example.autolaunchwebview;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import android.util.Log;
import androidx.annotation.Nullable;

import java.util.List;

public class InactivityMonitorService extends Service implements LifecycleObserver {
    private static final String TAG = "InactivityMonitor";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable relaunchRunnable = this::triggerRelaunch;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        Notification notification = new NotificationCompat.Builder(this, "kiosk_watchdog")
                .setContentTitle("Kiosk Monitor Active")
                .setContentText("Monitoring app activity for auto-relaunch")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        // Start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1001, notification);
        }

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        Log.d(TAG, "Service started and observing app lifecycle");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        Log.d(TAG, "App went to background. Starting 30s timer...");
        handler.removeCallbacks(relaunchRunnable);
        handler.postDelayed(relaunchRunnable, 30_000);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        Log.d(TAG, "App came to foreground. Cancelling timer.");
        handler.removeCallbacks(relaunchRunnable);
    }

    private void triggerRelaunch() {
        Log.d(TAG, "30s timer expired. Checking if app needs to be relaunched...");

        // Check if MainActivity is already in foreground
        if (isAppInForeground()) {
            Log.d(TAG, "App is already in foreground, no relaunch needed");
            return;
        }

        Log.d(TAG, "App is in background, triggering relaunch...");

        // Try multiple relaunch strategies
        boolean success = false;

        // Strategy 1: Use overlay launcher
        try {
            OverlayLauncher.launchWithOverlay(this);
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Overlay launcher failed", e);
        }

        // Strategy 2: Direct intent launch
        if (!success) {
            try {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                success = true;
                Log.d(TAG, "Direct intent launch successful");
            } catch (Exception e) {
                Log.e(TAG, "Direct intent launch failed", e);
            }
        }

        // Strategy 3: Launch via LaunchActivity
        if (!success) {
            try {
                Intent intent = new Intent(this, LaunchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                success = true;
                Log.d(TAG, "LaunchActivity launch successful");
            } catch (Exception e) {
                Log.e(TAG, "LaunchActivity launch failed", e);
            }
        }

        if (success) {
            Log.d(TAG, "Relaunch triggered successfully");
        } else {
            Log.e(TAG, "All relaunch strategies failed");
        }
    }

    private boolean isAppInForeground() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (activityManager == null) return false;

            // Method 1: Check running tasks (works on older Android versions)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
                if (!tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (topActivity != null) {
                        boolean isForeground = getPackageName().equals(topActivity.getPackageName());
                        Log.d(TAG, "Foreground check (RunningTasks): " + isForeground);
                        return isForeground;
                    }
                }
            }

            // Method 2: Check app processes
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo process : processes) {
                    if (getPackageName().equals(process.processName)) {
                        boolean isForeground = process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                        Log.d(TAG, "Foreground check (Process importance): " + isForeground + " (importance: " + process.importance + ")");
                        return isForeground;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking foreground status", e);
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        return START_STICKY; // Restart service if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        handler.removeCallbacks(relaunchRunnable);

        // Try to restart the service
        Intent restartIntent = new Intent(this, InactivityMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}