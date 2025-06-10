package com.example.autolaunchwebview;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Method;

public class MiuiAutoPermissionManager {
    private static final String TAG = "MiuiAutoPermissionManager";
    private final Context context;
    private final Handler handler;

    public MiuiAutoPermissionManager(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Main method to automatically configure all MIUI permissions
     */
    public void enableAllMiuiPermissions() {
        if (!isMiui()) {
            Log.d(TAG, "Not a MIUI device, skipping MIUI-specific permissions");
            return;
        }

        Log.d(TAG, "Starting MIUI auto-permission configuration...");

        // Execute permissions in sequence with delays
        handler.post(this::enableAutostartPermission);
        handler.postDelayed(this::disableBatteryOptimization, 1000);
        handler.postDelayed(this::enableBackgroundAppRefresh, 2000);
        handler.postDelayed(this::enablePopupPermission, 3000);
        handler.postDelayed(this::enableModifySystemSettings, 4000);
        handler.postDelayed(this::enableOverlayPermission, 5000);
        handler.postDelayed(this::disableMiuiOptimization, 6000);
        handler.postDelayed(this::enableDisplayOverOtherApps, 7000);
        handler.postDelayed(this::setAppAsProtected, 8000);
    }

    /**
     * Enable autostart permission (Most Critical for boot launch)
     */
    private void enableAutostartPermission() {
        try {
            Log.d(TAG, "Attempting to enable autostart permission...");

            // Method 1: Direct autostart activity
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Try to auto-enable via reflection
            try {
                enablePermissionViaReflection("autostart", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for autostart", e);
            }

            context.startActivity(intent);
            Log.d(TAG, "Autostart settings opened");

        } catch (Exception e) {
            Log.e(TAG, "Failed to open autostart settings", e);
            // Fallback: Open general app settings
            openAppSettings();
        }
    }

    /**
     * Disable battery optimization
     */
    private void disableBatteryOptimization() {
        try {
            Log.d(TAG, "Attempting to disable battery optimization...");

            // Method 1: MIUI specific battery settings
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Try reflection method
            try {
                enablePermissionViaReflection("battery_optimization", false);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for battery optimization", e);
            }

            context.startActivity(intent);
            Log.d(TAG, "Battery optimization settings opened");

        } catch (Exception e) {
            Log.e(TAG, "Failed to open battery optimization settings", e);
            // Fallback: Standard Android battery optimization
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Fallback battery optimization also failed", ex);
            }
        }
    }

    /**
     * Enable background app refresh
     */
    private void enableBackgroundAppRefresh() {
        try {
            Log.d(TAG, "Attempting to enable background app refresh...");

            // Try reflection method
            try {
                enablePermissionViaReflection("background_app_refresh", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for background app refresh", e);
            }

            // Open app-specific settings
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Failed to configure background app refresh", e);
        }
    }

    /**
     * Enable popup permission
     */
    private void enablePopupPermission() {
        try {
            Log.d(TAG, "Attempting to enable popup permission...");

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"));
            intent.putExtra("extra_pkgname", context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Try reflection method
            try {
                enablePermissionViaReflection("display_popup", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for popup permission", e);
            }

            context.startActivity(intent);
            Log.d(TAG, "Popup permission settings opened");

        } catch (Exception e) {
            Log.e(TAG, "Failed to open popup permission settings", e);
        }
    }

    /**
     * Enable modify system settings
     */
    private void enableModifySystemSettings() {
        try {
            Log.d(TAG, "Attempting to enable modify system settings...");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }

            // Try reflection method
            try {
                enablePermissionViaReflection("modify_system_settings", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for modify system settings", e);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to enable modify system settings", e);
        }
    }

    /**
     * Enable overlay permission
     */
    private void enableOverlayPermission() {
        try {
            Log.d(TAG, "Attempting to enable overlay permission...");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }

            // Try reflection method
            try {
                enablePermissionViaReflection("system_alert_window", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for overlay permission", e);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to enable overlay permission", e);
        }
    }

    /**
     * Disable MIUI optimization (Advanced - affects system behavior)
     */
    private void disableMiuiOptimization() {
        try {
            Log.d(TAG, "Attempting to disable MIUI optimization...");

            // Try reflection method to disable MIUI optimization
            try {
                Class<?> systemProperties = Class.forName("android.os.SystemProperties");
                Method set = systemProperties.getMethod("set", String.class, String.class);
                set.invoke(null, "persist.sys.miui_optimization", "false");
                Log.d(TAG, "MIUI optimization disabled via reflection");
            } catch (Exception e) {
                Log.w(TAG, "Failed to disable MIUI optimization via reflection", e);
            }

            // Open developer options
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Failed to open developer options", e);
        }
    }

    /**
     * Enable display over other apps permission
     */
    private void enableDisplayOverOtherApps() {
        try {
            Log.d(TAG, "Attempting to enable display over other apps...");

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity"));
            intent.putExtra("extra_pkgname", context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Try reflection method
            try {
                enablePermissionViaReflection("display_over_other_apps", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for display over other apps", e);
            }

            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Failed to configure display over other apps", e);
        }
    }

    /**
     * Set app as protected (prevents it from being killed)
     */
    private void setAppAsProtected() {
        try {
            Log.d(TAG, "Attempting to set app as protected...");

            // Try to mark app as protected via reflection
            try {
                enablePermissionViaReflection("protected_app", true);
            } catch (Exception e) {
                Log.w(TAG, "Reflection method failed for protected app", e);
            }

            // Open security center
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.securitycenter.MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Failed to open security center", e);
        }
    }

    /**
     * Generic reflection method to enable/disable permissions
     */
    private void enablePermissionViaReflection(String permissionType, boolean enable) throws Exception {
        try {
            // Try to access MIUI's internal APIs
            Class<?> miuiSettings = Class.forName("miui.util.SettingsHelper");
            Method setPermission = miuiSettings.getMethod("setPermission",
                    Context.class, String.class, String.class, boolean.class);
            setPermission.invoke(null, context, context.getPackageName(), permissionType, enable);

            Log.d(TAG, "Permission " + permissionType + " set to " + enable + " via reflection");
        } catch (Exception e) {
            // Try alternative MIUI API
            try {
                Class<?> appOpsManager = Class.forName("android.app.AppOpsManager");
                @SuppressLint("WrongConstant") Object appOps = context.getSystemService("appops");
                Method setMode = appOpsManager.getMethod("setMode", int.class, int.class, String.class, int.class);

                // Map permission types to AppOps constants
                int opCode = getAppOpsCode(permissionType);
                if (opCode != -1) {
                    setMode.invoke(appOps, opCode, android.os.Process.myUid(), context.getPackageName(),
                            enable ? 0 : 1); // 0 = allowed, 1 = denied
                    Log.d(TAG, "Permission " + permissionType + " set via AppOps");
                }
            } catch (Exception ex) {
                throw new Exception("All reflection methods failed", ex);
            }
        }
    }

    /**
     * Map permission types to AppOps codes
     */
    private int getAppOpsCode(String permissionType) {
        switch (permissionType) {
            case "autostart":
                return 10021; // MIUI specific
            case "system_alert_window":
                return 24;
            case "modify_system_settings":
                return 23;
            case "display_popup":
                return 10022; // MIUI specific
            case "background_app_refresh":
                return 63;
            case "battery_optimization":
                return 47;
            default:
                return -1;
        }
    }

    /**
     * Open general app settings as fallback
     */
    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "App settings opened");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open app settings", e);
        }
    }

    /**
     * Check if device is MIUI
     */
    public static boolean isMiui() {
        return Build.MANUFACTURER.equalsIgnoreCase("xiaomi") ||
                Build.BRAND.equalsIgnoreCase("xiaomi") ||
                Build.BRAND.equalsIgnoreCase("redmi") ||
                hasProperty("ro.miui.ui.version.name");
    }

    /**
     * Check if system property exists
     */
    private static boolean hasProperty(String property) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            String value = (String) get.invoke(null, property);
            return value != null && !value.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get MIUI version
     */
    public static String getMiuiVersion() {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            return (String) get.invoke(null, "ro.miui.ui.version.name");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Check if all critical permissions are granted
     */
    public boolean areAllCriticalPermissionsGranted() {
        boolean allGranted = true;

        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                Log.d(TAG, "Overlay permission not granted");
                allGranted = false;
            }
        }

        // Check modify system settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Log.d(TAG, "Modify system settings permission not granted");
                allGranted = false;
            }
        }

        // Check if app is in battery optimization whitelist
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
                Log.d(TAG, "Battery optimization not disabled");
                allGranted = false;
            }
        }

        Log.d(TAG, "All critical permissions granted: " + allGranted);
        return allGranted;
    }

    /**
     * Show toast message to user
     */
    private void showToast(String message) {
        handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
    }

    /**
     * Enable all permissions with user-friendly messages
     */
    public void enableAllPermissionsWithGuidance() {
        if (!isMiui()) {
            showToast("Standard Android device - MIUI optimizations not needed");
            return;
        }

        showToast("Configuring MIUI permissions for auto-launch...");

        // Start the permission configuration process
        enableAllMiuiPermissions();

        // Show completion message after all permissions are processed
        handler.postDelayed(() -> {
            showToast("MIUI permission configuration completed. Please restart your device to test auto-launch.");
            Log.d(TAG, "MIUI permission configuration process completed");
        }, 10000);
    }
}
