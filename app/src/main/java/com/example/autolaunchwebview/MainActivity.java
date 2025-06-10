package com.example.autolaunchwebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.app.AlertDialog;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CALL_PHONE = 1001;
    private static final int REQUEST_OVERLAY_PERMISSION = 1002;
    private static final int REQUEST_IGNORE_BATTERY_OPTIMIZATION = 1003;
    private WebView webView;
    private final Handler handler = new Handler();
    private Runnable inactivityRunnable;
    private MiuiAutoPermissionManager miuiPermissionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity created");
        webView = findViewById(R.id.webView);

        // Initialize MIUI permission manager
        miuiPermissionManager = new MiuiAutoPermissionManager(this);

        setupWebView();
        startInactivityMonitor();
        createNotificationChannel();

        // Handle permissions based on device type
        if (MiuiAutoPermissionManager.isMiui()) {
            handleMiuiPermissions();
        } else {
            requestStandardPermissions();
        }

        startInactivityService();
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page loaded: " + url);
            }
        });

        webView.loadUrl("https://techstern.com");
    }
    @SuppressLint("ClickableViewAccessibility")
    private void startInactivityMonitor() {
        inactivityRunnable = () -> {
            Log.d(TAG, "Inactivity timeout - relaunching");
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        };

        resetTimer();
        webView.setOnTouchListener((v, event) -> {
            resetTimer();
            return false;
        });
    }
    private void resetTimer() {
        handler.removeCallbacks(inactivityRunnable);
        handler.postDelayed(inactivityRunnable, 30_000);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "kiosk_watchdog",
                    "Inactivity Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors app inactivity for auto-relaunch");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private void startInactivityService() {
        Intent serviceIntent = new Intent(this, InactivityMonitorService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d(TAG, "Inactivity service started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start inactivity service", e);
        }
    }
    /**
     * Handle MIUI-specific permissions automatically
     */
    private void handleMiuiPermissions() {
        Log.d(TAG, "MIUI device detected - Version: " + MiuiAutoPermissionManager.getMiuiVersion());

        // Show informative dialog first
        new AlertDialog.Builder(this)
                .setTitle("MIUI Auto-Launch Setup")
                .setMessage("This app will automatically configure MIUI permissions for reliable auto-launch. " +
                        "You may see several permission screens - please allow all permissions for proper functionality.")
                .setPositiveButton("Start Configuration", (dialog, which) -> {
                    // Start MIUI permission configuration
                    miuiPermissionManager.enableAllPermissionsWithGuidance();

                    // Also request standard Android permissions
                    requestStandardPermissions();
                })
                .setNegativeButton("Manual Setup", (dialog, which) -> {
                    // Just request standard permissions
                    requestStandardPermissions();
                    Toast.makeText(this, "You can run manual MIUI setup from Debug Activity", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
    /**
     * Request standard Android permissions
     */
    private void requestStandardPermissions() {
        requestPhonePermission();
        requestOverlayPermission();
        requestBatteryOptimizationExemption();
    }
    private void requestPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

                new AlertDialog.Builder(this)
                        .setTitle("Phone Permission Required")
                        .setMessage("This app requires CALL_PHONE permission for InCall service functionality.")
                        .setPositiveButton("Grant", (dialog, which) -> ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                REQUEST_CALL_PHONE))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                            Toast.makeText(this, "Phone permission denied", Toast.LENGTH_LONG).show();
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PHONE);
            }
        }
    }
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }
    }
    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivityForResult(intent, REQUEST_IGNORE_BATTERY_OPTIMIZATION);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to request battery optimization exemption", e);
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_OVERLAY_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Overlay permission denied - auto-launch may not work", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case REQUEST_IGNORE_BATTERY_OPTIMIZATION:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Battery optimization disabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Battery optimization still enabled - may affect background operation", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Phone permission granted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Phone permission granted");
            } else {
                Toast.makeText(this, "Phone permission denied - InCall service may not work", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Phone permission denied");
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity resumed");
        resetTimer();

        // Check permission status when app resumes
        if (MiuiAutoPermissionManager.isMiui()) {
            boolean allGranted = miuiPermissionManager.areAllCriticalPermissionsGranted();
            Log.d(TAG, "Critical permissions status: " + (allGranted ? "All granted" : "Some missing"));
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity paused");
    }
    @Override
    public void onBackPressed() {
        // Prevent back button from closing the app in kiosk mode
        // Comment out this override if you want normal back button behavior
        Log.d(TAG, "Back button pressed - ignoring in kiosk mode");
    }
}