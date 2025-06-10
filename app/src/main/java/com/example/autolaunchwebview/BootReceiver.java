package com.example.autolaunchwebview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
                "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {

            // Optional: delay launch by manufacturer if needed
            int delayMs = getBootDelayForManufacturer();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "Launching LaunchActivity after delay");

                Intent launchIntent = new Intent(context, LaunchActivity.class);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(launchIntent);

            }, delayMs);
        }
    }

    private int getBootDelayForManufacturer() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
            return 15000;
        } else if (manufacturer.contains("oneplus")) {
            return 12000;
        } else if (manufacturer.contains("oppo") || manufacturer.contains("vivo")) {
            return 10000;
        } else {
            return 8000; // default fallback
        }
    }
}