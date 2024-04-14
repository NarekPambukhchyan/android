package com.netify.app.Engine;

import android.app.Activity;
import android.content.Intent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class InoRainWebViewClient extends WebViewClient {

    private Activity activity;
    private String host;

    public InoRainWebViewClient(Activity activity, String host){
        this.activity = activity;
        this.host = host;
    }

}
