package com.example.autolaunchwebview;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class OverlayLauncher {
    private static final String TAG = "OverlayLauncher";

    public static void launchWithOverlay(Context context) {
        Log.d(TAG, "Starting overlay launch process...");

        // Try multiple launch strategies
        boolean success = false;

        // Strategy 1: Direct launch (works on most devices)
        if (!success) {
            success = tryDirectLaunch(context);
        }

        // Strategy 2: Overlay launch (for system restrictions)
        if (!success && canUseOverlay(context)) {
            success = tryOverlayLaunch(context);
        }

        // Strategy 3: Bring existing task to front
        if (!success) {
            success = tryBringToFront(context);
        }

        // Strategy 4: Force launch with different flags
        if (!success) {
            success = tryForceLaunch(context);
        }

        Log.d(TAG, "Launch attempt completed. Success: " + success);
    }

    private static boolean tryDirectLaunch(Context context) {
        try {
            Log.d(TAG, "Attempting direct launch...");
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
            Log.d(TAG, "Direct launch successful");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Direct launch failed", e);
            return false;
        }
    }

    private static boolean tryOverlayLaunch(Context context) {
        try {
            Log.d(TAG, "Attempting overlay launch...");
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) {
                Log.e(TAG, "WindowManager is null");
                return false;
            }

            // Create minimal overlay view
            View overlayView = new View(context);

            // Use appropriate window type based on Android version
            int windowType;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                windowType = WindowManager.LayoutParams.TYPE_PHONE;
            }

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    1, 1, // Minimal size
                    windowType,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;

            // Add overlay with delay for MIUI
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                try {
                    wm.addView(overlayView, params);
                    Log.d(TAG, "Overlay added successfully");

                    // Launch activity after overlay is added
                    handler.postDelayed(() -> {
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        try {
                            context.startActivity(intent);
                            Log.d(TAG, "Activity launched from overlay");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to launch activity from overlay", e);
                        }

                        // Remove overlay after launch
                        handler.postDelayed(() -> {
                            try {
                                wm.removeView(overlayView);
                                Log.d(TAG, "Overlay removed");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to remove overlay", e);
                            }
                        }, 1000);
                    }, 100);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to add overlay view", e);
                    // This is where the MIUI NPE might occur - catch and ignore
                    if (e.getMessage() != null && e.getMessage().contains("null object reference")) {
                        Log.w(TAG, "MIUI system error detected - continuing with fallback");
                    }
                }
            }, isMIUI() ? 500 : 100); // Longer delay for MIUI

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Overlay launch failed", e);
            return false;
        }
    }

    private static boolean tryBringToFront(Context context) {
        try {
            Log.d(TAG, "Attempting to bring existing task to front...");
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return false;

            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            for (ActivityManager.AppTask task : tasks) {
                try {
                    ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();
                    if (taskInfo != null && taskInfo.baseIntent != null) {
                        String packageName = taskInfo.baseIntent.getComponent().getPackageName();
                        if (context.getPackageName().equals(packageName)) {
                            task.moveToFront();
                            Log.d(TAG, "Task moved to front successfully");
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to move task to front", e);
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Bring to front failed", e);
            return false;
        }
    }

    private static boolean tryForceLaunch(Context context) {
        try {
            Log.d(TAG, "Attempting force launch...");
            Intent intent = new Intent(context, LaunchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intent);
            Log.d(TAG, "Force launch successful");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Force launch failed", e);
            return false;
        }
    }

    private static boolean canUseOverlay(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canDraw = Settings.canDrawOverlays(context);
            Log.d(TAG, "Overlay permission: " + canDraw);
            return canDraw;
        }
        return true;
    }

    private static boolean isMIUI() {
        return Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.BRAND.equalsIgnoreCase("xiaomi") ||
                Build.BRAND.equalsIgnoreCase("redmi");
    }
}