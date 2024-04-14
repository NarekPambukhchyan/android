package com.netify.app.Engine;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import com.netify.app.MainActivity;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WebAppInterface {
    Context mContext;
    MainActivity activity;
    Device device;

    WebEngine engine;
    Player player;

    /** Instantiate the interface and set the context */
    public WebAppInterface(

            MainActivity activity,
            WebEngine engine,
            Player player
    ) {

        mContext = activity.getApplicationContext();

        device = new Device(activity);

        this.player = player;
        this.engine = engine;
        this.activity = activity;
    }


    @JavascriptInterface
    public String getVersion(){
        return "1.0.0";
    }

    @JavascriptInterface
    public String getAppName(){ return "Family 4k"; }

    @JavascriptInterface
    public String getSelectedTrack(String type){
        return player.getSelectedTrack(type);
    }

    @JavascriptInterface
    public void repeatMode(String mode){ player.setRepeatMode(mode); }

    @JavascriptInterface
    public void selectTrack(String type,int track_index,int group_index){
        player.selectTrack(type,track_index,group_index);
    }

    @JavascriptInterface
    public void simpleAlert(String title,String message,String btnText){
        device.alertDialog(title,message,btnText);
    }

    @JavascriptInterface
    public boolean isPlaying(){
        return player.isPlaying();
    }

    @JavascriptInterface
    public int getSate(){
        return player.getState();
    }

    @JavascriptInterface
    public String getVideoResolution(){
        return player.getVideoResolution();
    }

    @JavascriptInterface
    public void setAspectRatio(String ratio){
        player.setAspectRatio(ratio);
    }

    @JavascriptInterface
    public void initPlayer(String url,long ms){
        player.init(url,ms);
    }

    @JavascriptInterface
    public void initPlayer(String url){
        player.init(url,0);
    }

    @JavascriptInterface
    public void enableCustomDuration(long duration){
        player.enableCustomDuration(duration);
    }

    @JavascriptInterface
    public void disableCustomDuration(){
        player.disableCustomDuration();
    }

    @JavascriptInterface
    public void reload(){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                engine.reloadApp();
            }
        });

    }

    @JavascriptInterface
    public void play(){

        player.play();

    }

    @JavascriptInterface
    public void pause(){

        player.pause();

    }

    @JavascriptInterface
    public void destroyPlayer(){

        player.destroy();

    }

    @JavascriptInterface
    public void seekTo(long ms){
        player.seekTo(ms);
    }
    @JavascriptInterface
    public void seekToVlc(float newPos) {
        player.setVlcFractionalPosition(newPos);
    }

    @JavascriptInterface
    public long getVideoDuration(){

        return player.getDuration();

    }

    @JavascriptInterface
    public long  getCurrentTime() {

        return player.getPosition();

    }

    @JavascriptInterface
    public float getWidth(){
        return device.getScreenWidth();
    }

    @JavascriptInterface
    public float getHeight(){
        return device.getScreenHeight();
    }

    @JavascriptInterface
    public String getIP(){
        return  device.getIP();
    }

    @JavascriptInterface
    public String getModel(){
        return  device.getDeviceName();
    }

    @JavascriptInterface
    public void setPlayer(String pl){
        player.setVideoPlayer(pl);
    }

    @JavascriptInterface
    public void setPlayerPositions(int w,int  h, int  x, int y, int webWidth, int webHeight){
        player.setPlayerViewPositions(w,h,x,y,webWidth,webHeight);
    }

    @JavascriptInterface
    public String getDeviceId(){

        return device.getDeviceId();

    }

    @JavascriptInterface
    public String getTracks(){
        return player.getTracks();
    }

    @JavascriptInterface
    public void setVolumeOfThePlayer(int volume){
        player.setVolume(volume);
    }

    @JavascriptInterface
    public float getVolumeOfThePlayer(){
        return player.getVolume();
    }

    @JavascriptInterface
    public void setPlayerFullScreen(){
        activity.runOnUiThread(() -> player.setFullScreen());
    }

    @JavascriptInterface
    public void toastShot(String text){ device.toastShort(text); }

    @JavascriptInterface
    public void toastLong(String text){ device.toastLong(text); }

    @JavascriptInterface
    public void exitApp(){ device.exitApp(); }

    @JavascriptInterface
    public void disableTextTrack(){ player.disableTextTrack(); }

    @JavascriptInterface
    public void set404Html(String html){
        engine.set404Html(html);
    }
    @JavascriptInterface
    public void setNetworkErrorHTMLPage(String html){
        engine.setNetworkErrorHTMLPage(html);
    }

    @JavascriptInterface
    public  boolean launchApp(String packageId) {
        return  device.launchApp(packageId);
    }

    @JavascriptInterface
    public void enableStartUpMode(){
        engine.enableStartUpMode();
    }
    @JavascriptInterface
    public void disableStartUpMode(){
        engine.disableStartUpMode();
    }

    @JavascriptInterface
    public boolean isStartUpModeEnabled(){

       return engine.isStartUpModeEnabled();
    }


    @JavascriptInterface
    public void setSubtitleStyle(String col,String col1, int x1,int x2){}
    @JavascriptInterface
    public void setReturnMode(boolean reload){
        device.setReturnMode(reload);

    }

    @JavascriptInterface
    public void getFromUrl(String url){

        device.executeHttpRequestAsync(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Error: "+e.getMessage());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        new WebEngine(activity).setRequestResult(url, "500", "Error: "+e.getMessage());

                                    }
                                }
                        );
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String responseData = response.body().string();
                            String escapedString = responseData
                                    .replace("\\", "\\\\")
                                    .replace("'", "\\'")
                                    .replace("\n", "\\n")
                                    .replace("\"", "\\\"");

                            new WebEngine(activity).setRequestResult(url, String.valueOf(response.code()), escapedString);
                        } catch (IOException e) {
                            Log.d("TAG", "run: " + e.getMessage());
                        }

                    }
                });
            }
        });
    }
}