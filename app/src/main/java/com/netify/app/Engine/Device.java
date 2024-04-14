package com.netify.app.Engine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.netify.app.MainActivity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Device {

    private Activity activity;

    public Device(Activity activity){
        this.activity = activity;
    }

    public String getDeviceId(){

        String deviceId = Settings.Secure.getString(this.activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;

    }

    public void toastLong(String text){
        Toast.makeText(activity.getApplicationContext(),text,Toast.LENGTH_LONG).show();
    }

    public void toastShort(String text){
        Toast.makeText(activity.getApplicationContext(),text,Toast.LENGTH_SHORT).show();
    }

    public int getScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        return width;
    }

    public int getScreenHeight(){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        return height;

    }
    public String getIP(){
        String ip = "";
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ip = cm.getLinkProperties(cm.getActiveNetwork()).getLinkAddresses().toString();
                System.out.println("IP: " + ip);
                ip = ip.split(", ")[1].split("/")[0];
            }
        }
        return ip;
    }



    public String getDeviceName(){

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return Utils.capitalize(model);
        }
        return Utils.capitalize(manufacturer) + " " + model;
    }

    public void exitApp(){

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);

    }

    public void alertDialog(String title, String message,String btnText){

        new AlertDialog.Builder(this.activity.getApplicationContext())
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(btnText, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    public void setFullScreen(){
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public int getScale(){
        Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width) / new Double(5000);
        val = val * 100d;
        return val.intValue();
    }

    public boolean launchApp(String packageId) {
        try {
            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(packageId);
            if (launchIntent != null) {
                //null pointer check in case package name was not found
                activity.startActivity(launchIntent);
                return  true;

            }
        } catch (Exception e) {
            return  false;
        }
        return  false;
    }

    public void setReturnMode(boolean value) {
        MainActivity.reload = value;
    }

    //method for doing get requests and sending statuscode and responseBody

    public void executeHttpRequestAsync(String url, Callback callback) {

            OkHttpClient client = new OkHttpClient();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(callback);
    }


}
