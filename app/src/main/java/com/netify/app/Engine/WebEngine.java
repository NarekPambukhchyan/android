package com.netify.app.Engine;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.netify.app.MainActivity;

import java.util.Date;


public class WebEngine {

    WebEngine webEngine;
    private String TAG = "inoRain Web View";

    private String HOST = null;
    private Activity activity;
    private Device device;
    public static WebView webView;
    private Player player;

    private String networkErrorHTML = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Network Error</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            background-color: #f0f0f0;\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "            height: 100vh;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .error-container {\n" +
            "            background-color: #fff;\n" +
            "            padding: 20px;\n" +
            "            border-radius: 10px;\n" +
            "            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        h1 {\n" +
            "            font-size: 2rem;\n" +
            "            margin-bottom: 10px;\n" +
            "        }\n" +
            "        p {\n" +
            "            font-size: 1rem;\n" +
            "            color: #777;\n" +
            "        }\n" +
            "        .icon {\n" +
            "            font-size: 3rem;\n" +
            "            margin-top: 10px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error-container\">\n" +
            "        <h1>Network Error</h1>\n" +
            "        <p>Oops! Something went wrong. Check your internet connection and try again.</p>\n" +
            "        <div class=\"icon\">\uD83C\uDF10</div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    private String html404 = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Page Not Found</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            background-color: #f0f0f0;\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "            height: 100vh;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .error-container {\n" +
            "            background-color: #fff;\n" +
            "            padding: 20px;\n" +
            "            border-radius: 10px;\n" +
            "            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\n" +
            "        }\n" +
            "        h1 {\n" +
            "            font-size: 2rem;\n" +
            "            margin-bottom: 10px;\n" +
            "        }\n" +
            "        p {\n" +
            "            font-size: 1rem;\n" +
            "            color: #777;\n" +
            "        }\n" +
            "        .emoji {\n" +
            "            font-size: 3rem;\n" +
            "            margin-top: 10px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error-container\">\n" +
            "        <h1>Oops! It's a Dead End</h1>\n" +
            "        <p>The page you were looking for seems to be on vacation.</p>\n" +
            "        <div class=\"emoji\">\uD83D\uDE48</div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";

    public WebEngine(WebView webView,Activity activity,Device device, Player player){
        this.activity = activity;
        this.device = device;
        this.player = player;
        this.webView = webView;

    }

    public WebEngine(Activity activity){
        this.activity = activity;
    }

    public void showNetworkErrorPage(){
        webView.loadDataWithBaseURL(null, getNetworkErrorPage(), "text/html", "utf-8", null);
    }

    private String getNetworkErrorPage(){
        return this.networkErrorHTML;
    }

    public String getHost(){
        return HOST;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initApp(String host){

        HOST = host;

        webView.loadUrl(host+"?"+new Date().getTime());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                log(consoleMessage.lineNumber() + " |  " + consoleMessage.message());
                return true;
            }
        });

        webView.setWebViewClient(new InoRainWebViewClient(activity,host));

        WebAppInterface jsInterface = new WebAppInterface((MainActivity) activity,this,player);

        webView.requestFocus();
        webView.addJavascriptInterface(jsInterface, "Android");
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setInitialScale(0);
        webView.setKeepScreenOn(true);

        WebSettings webSettings = webView.getSettings();

        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);


        // Enable cross origin requests
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);


    }

    public void setNetworkErrorHTMLPage(String html){
        this.networkErrorHTML = html;
    }
    public void set404Html(String html){
        this.html404 = html;
    }
    private String getCustom404Page() {
        return this.html404;
    }


    public void reloadApp(){
        try{
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.reload();
                }
            });

        }catch (Exception e){
            log(e.getMessage());
        }

    }

    public void netWorkLostEvent(boolean isConnected){
        String jsCode = "window.networkUpdates("+isConnected+")";
        webView.evaluateJavascript(jsCode, null);
    }

    public void isPlayingChanged(boolean isPlaying){
        String jsCode = "window.PLAYER.isPlayingChanged("+isPlaying+")";
        webView.evaluateJavascript(jsCode, null);
    }

    public void playerError(int code,String name, String message){

        String jsCode = "window.PLAYER.playerError("+code+",'"+name+"','"+message+"')";
        webView.evaluateJavascript(jsCode, null);

    }

    public void buffering(int percent, long position){

        String jsCode = "window.PLAYER.buffering('"+percent+"%',"+position+")";
        webView.evaluateJavascript(jsCode, null);

    }

    public void streamEnded(){

        String jsCode = "window.PLAYER.streamEnd()";
        webView.evaluateJavascript(jsCode, null);

    }

    public void tracksDetected(){

        String jsCode = "window.PLAYER.getAndroidTracks();";
        webView.evaluateJavascript(jsCode, null);

    }

    public void backPressed(){

        String jsCode = "window.keydown({keyName:'back'})";
        webView.evaluateJavascript(jsCode, null);

    }

    public void androidPlayerTimeUpdate(){

        String jsCode = "window.PLAYER.androidPlayerTimeUpdate();";
        webView.evaluateJavascript(jsCode,null);

    }

    public void vout() {
        String jsCode = "window.PLAYER.vout();";
        webView.evaluateJavascript(jsCode,null);
    }

    public void setRequestResult(String url, String status, String response){
        String jsCode = "window.setRequestResult(" + "'" + url+ "'," + status +",'" +response +"');";
        webView.evaluateJavascript(jsCode,null);
    }


    public void enableStartUpMode(){
        boolean hasBootPermission = PackageManager.PERMISSION_GRANTED ==
                activity.getPackageManager().checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", activity.getPackageName());

        if (!hasBootPermission) {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity.getApplicationContext());
            builder.setTitle("Permission Required");
            builder.setMessage("Please enable the BOOT_COMPLETED permission for this app in settings.");
            builder.setPositiveButton("Go to Settings", (dialog, which) -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        }


        SharedPreferences prefs = activity.getSharedPreferences("INO_WEB_ENGINE", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("startup_enabled", true);
        editor.apply();
    }

    public void disableStartUpMode(){
        SharedPreferences prefs = activity.getSharedPreferences("INO_WEB_ENGINE", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("startup_enabled", false);
        editor.apply();
    }


    public boolean isStartUpModeEnabled(){
        SharedPreferences prefs = activity.getSharedPreferences("INO_WEB_ENGINE", Context.MODE_PRIVATE);
        boolean isStartupEnabled = prefs.getBoolean("startup_enabled", false);
        return  isStartupEnabled;
    }


    public void log(String message){
        android.util.Log.d(TAG,message);
    }
}
