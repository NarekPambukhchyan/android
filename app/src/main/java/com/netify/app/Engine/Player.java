package com.netify.app.Engine;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.ui.PlayerView;
import com.netify.app.players.Exo;
import com.netify.app.players.Vlc;

import org.videolan.libvlc.util.VLCVideoLayout;

import java.util.Objects;

public class Player {
    private Activity activity;
    private Vlc vlc;
    private VLCVideoLayout vlcVideoLayout;
    private Exo exo;
    private String currentPlayer = null;
    private PlayerView exoPlayerView;

    private FrameLayout vlcParent;

    public Player(Activity activity, PlayerView exoPlayerView, VLCVideoLayout vlcVideoLayout, FrameLayout vlcParent){

        this.activity = activity;
        this.exoPlayerView = exoPlayerView;
        this.vlcVideoLayout = vlcVideoLayout;
        this.vlcParent = vlcParent;

    }
    public void init(String url,long ms){
        if (currentPlayer == null) setVideoPlayer("EXO");

        new WebEngine(activity).log("Selected player " + currentPlayer);
        new WebEngine(activity).log("Current url " + url);


        if (isExo()) exo.initPlayer(url, ms);

        if (isVlc()) vlc.init(url, ms);

    }


    public String getVideoResolution(){

        if (isVlc()) return vlc.getVideoResolution();

        if (isExo()) return "Resolution not detected";

        return "Resolution not detected";
    }

    public void disableTextTrack() {
        if (isExo()) {
            exo.disableTextTrack();
        }
        if(isVlc()) {
            vlc.disableTextTrack();

        }
    }
    public String getTracks(){

        if (isExo()) return  exo.getTracks();

        if (isVlc()) return vlc.getTracks();

        return "{}";
    }
    public void selectTrack(String type,int track_index,int group_index){

        if (isExo()) exo.selectTrack(type, track_index,group_index);

        if (isVlc()) vlc.setTrack(type,track_index);

    }

    public String getSelectedTrack(String type){

        if (isVlc()) return vlc.getSelectedTrack(type);

        if (isExo()) return exo.getSelectedTrack(type);

        return "{}";
    }

    public void setPlayerViewPositions(int w,int h,int x, int y, int webWidth,int webHeight){

        int androidWidth = new Device(activity).getScreenWidth();
        int androidHeight = new Device(activity).getScreenHeight();

        double width = (double) androidWidth / webWidth;
        double height = (double) androidHeight / webHeight;

        if (isExo()) exo.setViewPositions(
                (int) (w * width),
                (int) (h * height),
                (int) (x * width),
                (int) (y * height)
        );

        if (isVlc()) vlc.setViewPositions(
                (int) (w * width),
                (int) (h * height),
                (int) (x * width),
                (int) (y * height)
        );
    }
    public void setFullScreen(){

        if (isVlc()) vlc.setPlayerFullScreen();

        if (isExo()) exo.setPlayerFullScreen();

    }
    public boolean isPlaying(){

        if (isExo()) return exo.isPlaying();

        if (isVlc()) return vlc.isPlaying();

        return false;
    }
    public int getState(){

        if (isVlc()) vlc.getSate();

        if (isExo()) exo.getSate();

        return 0;
    }
    public void seekTo(long position){

        if (isExo()) exo.seekTo(position);

        if (isVlc()) vlc.seekTo(position);

    }

    public void setVlcFractionalPosition(float pos) {
        if(isVlc()) {
            vlc.setFractionalPosition(pos);
        }
    }
    public void setRepeatMode(String mode){

        if(isExo()) exo.setRepeatMode(mode);

        if(isVlc()) vlc.setRepeatMode(mode);

    }
    public void setAspectRatio(String mode){

        if (isVlc()) vlc.setAspectRatio(mode);

        if (isExo()) exo.setAspectRatio(mode);

    }
    public void setVideoPlayer(String player){

        this.currentPlayer = player;

        new WebEngine(activity).log("Current Player IS " + player);

        if (isExo()) {

            activity.runOnUiThread(() -> {
                exoPlayerView.setVisibility(View.VISIBLE);
                vlcParent.setVisibility(View.GONE);
            });

            exo = new Exo(activity,this.exoPlayerView);
        }

        if (isVlc()) {

            activity.runOnUiThread(() -> {
                exoPlayerView.setVisibility(View.GONE);
                vlcParent.setVisibility(View.VISIBLE);
            });

            vlc = new Vlc(activity,this.vlcVideoLayout,vlcParent);
        }

        setFullScreen();

    }
    public long getDuration(){

        if (isExo()) return exo.getDuration();

        if (isVlc()) return  vlc.getDuration();

        return 0;
    }
    public long getPosition(){

        if (isExo()) return exo.getPosition();


        if (isVlc()) return vlc.getPosition();

        return 0;

    }
    public void setVolume(int volume){

        if (isExo()) exo.setVolume(volume);

        if (isVlc()) vlc.setVolume(volume);

    }
    public float getVolume(){

        if (isExo()) return exo.getVolume();

        if (isVlc()) return vlc.getVolume();

        return 0;
    }
    public void pause(){

        if (isExo()) exo.pause();

        if (isVlc()) vlc.pause();

    }
    public void play(){

        if (isVlc()) vlc.play();

        if (isExo()) exo.play();

    }
    public void destroy(){

        if (isExo()) exo.destroy();

        if (isVlc()) vlc.destroy();
    }
    public boolean isExo(){

        if (Objects.equals(this.currentPlayer, "EXO")){
            return  true;
        }else{
            return  false;
        }

    }
    public boolean isVlc(){
        if (Objects.equals(this.currentPlayer, "VLC")){
            return  true;
        }else{
            return  false;
        }
    }

    public void  setSubtitleStyle(){

    }


    public void disableCustomDuration(){

        if(isExo()) new WebEngine(activity).log("Custom duration is not working for EXO");

        if(isVlc()) vlc.disableCustomDuration();

    }

    public void enableCustomDuration(long duration){

        if(isExo()) new WebEngine(activity).log("Custom duration is now working for EXO");

        if(isVlc()) vlc.enableCustomDuration(duration);

    }

}
