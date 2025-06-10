package com.example.autolaunchwebview;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DebugActivity extends Activity {
    private static final String TAG = "DebugActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        // Status display
        TextView statusText = new TextView(this);
        statusText.setText(getStatusReport());
        statusText.setTextSize(14);
        layout.addView(statusText);

        // Test buttons
        Button testOverlayBtn = new Button(this);
        testOverlayBtn.setText("Test Overlay Launch");
        testOverlayBtn.setOnClickListener(v -> testOverlayLaunch());
        layout.addView(testOverlayBtn);

        Button testServiceBtn = new Button(this);
        testServiceBtn.setText("Start Inactivity Service");
        testServiceBtn.setOnClickListener(v -> testInactivityService());
        layout.addView(testServiceBtn);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("Refresh Status");
        refreshBtn.setOnClickListener(v -> {
            statusText.setText(getStatusReport());
            Toast.makeText(this, "Status refreshed", Toast.LENGTH_SHORT).show();
        });
        layout.addView(refreshBtn);

        setContentView(layout);
    }

    private String getStatusReport() {
        StringBuilder status = new StringBuilder();
        status.append("=== AUTO-LAUNCH DEBUG STATUS ===\n\n");

        // Device info
        status.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        status.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n");

        // Permissions
        status.append("--- PERMISSIONS ---\n");

        // Overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasOverlay = Settings.canDrawOverlays(this);
            status.append("Overlay Permission: ").append(hasOverlay ? "✓ GRANTED" : "✗ DENIED").append("\n");
        }

        // Battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            boolean batteryOptimized = pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
            status.append("Battery Optimization: ").append(batteryOptimized ? "✓ DISABLED" : "✗ ENABLED").append("\n");
        }

        status.append("\n--- MANUAL CHECKS NEEDED ---\n");
        status.append("1. Auto-start permission in device settings\n");
        status.append("2. Background app refresh enabled\n");
        status.append("3. Set as default launcher (optional)\n");
        status.append("4. Disable 'Put app to sleep'\n\n");

        // Device-specific notes
        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            status.append("MIUI NOTES:\n");
            status.append("- Enable Autostart in Security app\n");
            status.append("- Disable MIUI Optimization\n");
            status.append("- Lock app in recent apps\n");
        } else if (Build.MANUFACTURER.equalsIgnoreCase("oneplus")) {
            status.append("OnePlus NOTES:\n");
            status.append("- Enable Auto-launch in Battery settings\n");
            status.append("- Disable Battery optimization\n");
        } else if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            status.append("Samsung NOTES:\n");
            status.append("- Disable 'Put app to sleep'\n");
            status.append("- Add to 'Never sleeping apps'\n");
        }

        return status.toString();
    }

    private void testOverlayLaunch() {
        Log.d(TAG, "Testing overlay launch...");
        try {
            OverlayLauncher.launchWithOverlay(this);
            Toast.makeText(this, "Overlay launch triggered", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Overlay launch failed", e);
            Toast.makeText(this, "Overlay launch failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void testInactivityService() {
        Log.d(TAG, "Testing inactivity service...");
        try {
            Intent serviceIntent = new Intent(this, InactivityMonitorService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(this, "Inactivity service started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Service start failed", e);
            Toast.makeText(this, "Service start failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}