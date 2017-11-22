package com.kritinelavelli.swipeapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        System.out.println(intent.getAction());
        Log.d("ScreenStart", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            Intent intent1 = new Intent(context,FullscreenActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
