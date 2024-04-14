package com.netify.app.Tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.netify.app.MainActivity;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            SharedPreferences prefs = context.getSharedPreferences("INO_WEB_ENGINE", Context.MODE_PRIVATE);
            boolean isStartupEnabled = prefs.getBoolean("startup_enabled", false);

            if(isStartupEnabled){
                Intent i = new Intent(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }else{
                System.out.println("STARTUP MODE IS DISABLED...");
            }

        }
    }
}
