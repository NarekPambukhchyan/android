package com.netify.app;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ui.PlayerView;
import com.netify.app.Engine.Device;
import com.netify.app.Engine.NetworkTimer;
import com.netify.app.Engine.Player;
import com.netify.app.Engine.WebEngine;

import org.videolan.libvlc.util.VLCVideoLayout;


public class MainActivity extends AppCompatActivity {

    /*****
     *  WEB APPLICATION HOST
     *  Warning : Make sure the host is not local host before creating apk for
     *  sending to clients or for sending app to production
     *  -------------------------------------------------------------------------------------
     */
    private String defaultHost = "http://192.168.8.222:5500/";

    /**
     *  -------------------------------------------------------------------------------------
     *  WEB APPLICATION HOST
     */

    public PlayerView exoPlayerView;
    private WebView myWebView;
    private Device device = null;
    private FrameLayout vlc_parent;
    private WebEngine webEngine = null;
    private Player player = null;
    public VLCVideoLayout vlcVideoLayout;
    private  NetworkTimer timer = null;
    public static boolean reload = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init();



    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init(){

        device = new Device(this);

        device.setFullScreen();

        myWebView = findViewById(R.id.webview);
        exoPlayerView = findViewById(R.id.idExoPlayerVIew);
        vlcVideoLayout = findViewById(R.id.vlc_player_view);
        vlc_parent = findViewById(R.id.vlc_parent);

        player = new Player(this, exoPlayerView, vlcVideoLayout,vlc_parent);

        webEngine = new WebEngine(myWebView,this,device,player);

        webEngine.initApp(defaultHost);


        timer = new NetworkTimer(this,webEngine);
        timer.startNetworkCheckTimer();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {

        if(reload) {
            webEngine.initApp(defaultHost);
        }
        timer = new NetworkTimer(this,webEngine);
        timer.startNetworkCheckTimer();
        super.onResume();
    }

    @Override
    protected void onPause() {
        player.destroy();
        timer.stopNetworkCheckTimer();
        super.onPause();
    }

    @Override
    public void onBackPressed() { webEngine.backPressed(); }

    @Override
    protected void onDestroy() {
        player.destroy();
        timer.stopNetworkCheckTimer();
        super.onDestroy();
    }

}