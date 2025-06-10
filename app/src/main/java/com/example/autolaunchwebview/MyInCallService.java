package com.example.autolaunchwebview;

import android.content.Intent;
import android.telecom.Call;
import android.telecom.InCallService;

public class MyInCallService extends InCallService {
    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

