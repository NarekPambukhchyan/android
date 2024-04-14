package com.netify.app.Engine;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class NetworkTimer {

    private Activity activity;
    private Timer timer;

    private boolean last_state;
    private boolean is_first_time = true;

    private WebEngine engine;

    public  NetworkTimer(Activity activity,WebEngine engine){
        this.activity = activity;
        this.engine = engine;
    }

    public void startNetworkCheckTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                checkNetworkConnectivity();
            }
        };
        timer.schedule(task, 0, 1000);
    }

    public void stopNetworkCheckTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void checkNetworkConnectivity() {


        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();


        // I HAVE ADDED is_first_time TO NO DOUBLE RELOAD THE PAGE WHEN APP IS STARTING..

        if(is_first_time){

            last_state = isConnected;
            is_first_time = false;

        } else{

            if (isConnected) {

                if(last_state != isConnected){

                    activity.runOnUiThread(() -> {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            engine.initApp(engine.getHost());
                        }

                        engine.netWorkLostEvent(isConnected);

                    });

                }

            } else {
                activity.runOnUiThread(() -> {

                    engine.netWorkLostEvent(isConnected);
                    engine.showNetworkErrorPage();

                });

            }

            last_state = isConnected;
        }

    }
}
