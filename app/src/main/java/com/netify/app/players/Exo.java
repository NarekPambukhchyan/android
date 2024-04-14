package com.netify.app.players;

import android.app.Activity;
import android.net.Uri;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.netify.app.Engine.Device;
import com.netify.app.Engine.WebEngine;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class Exo {
    private JSONArray tracks;
    private  Tracks tracksList;
    private Activity activity;
    private ExoPlayer exoPlayer;
    private PlayerView exoPlayerView;
    private long playerPosition = 0;
    private long playerDuration = 0;
    private boolean isVideoPlaying = false;
    private Timer timer = null;
    private TimerTask task = null;

    private int state = 0;

    private float volume = 1;
    public Exo(Activity activity, PlayerView exoPlayerView){
        this.activity = activity;
        this.exoPlayerView = exoPlayerView;
    }
    public void initPlayer(String url,long ms){

            new WebEngine(activity).log("PLAYING");

            activity.runOnUiThread(() -> {

                try{

                    if(exoPlayer == null) {

                        HttpDataSource.Factory httpDataSourceFactory =
                                new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);

                        DefaultDataSource.Factory dataSourceFactory =
                                new DefaultDataSource.Factory(activity.getApplicationContext(), httpDataSourceFactory);

                        exoPlayer = new ExoPlayer.Builder(activity)
                                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                                .build();
                        
                    }
                    exoPlayerView.setPlayer(exoPlayer);
                    new WebEngine(activity).log(url);
                    MediaItem mediaItem = MediaItem.fromUri( Uri.parse(url));
                    exoPlayer.setMediaItem(mediaItem,ms*1000);
                    exoPlayer.prepare();
                    exoPlayer.play();

                    new WebEngine(activity).log("PLAYING");

                }catch(Exception e){
                    System.out.println(e);
                    new WebEngine(activity).log(e.getMessage());

                }

                exoPlayer.addListener(new Player.Listener() {

                    @Override
                    public void onTracksChanged(Tracks tracks) {
                        Player.Listener.super.onTracksChanged(tracks);
                        tracksList = tracks;
                        detectTracks(tracks);
                        new WebEngine(activity).tracksDetected();
                    }

                    @Override
                    public void onPlaybackStateChanged(int playbackState) {

                        Player.Listener.super.onPlaybackStateChanged(playbackState);

                        if(playbackState == ExoPlayer.STATE_ENDED) new WebEngine(activity).streamEnded();

                        if(playbackState == ExoPlayer.STATE_BUFFERING) new WebEngine(activity).buffering(
                                exoPlayer.getBufferedPercentage(),
                                exoPlayer.getCurrentPosition()
                        );

                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        Player.Listener.super.onIsPlayingChanged(isPlaying);
                        new WebEngine(activity).isPlayingChanged(isPlaying);
                    }

                    @Override
                    public void onPlayerError(PlaybackException error) {
                        Player.Listener.super.onPlayerError(error);

                        new WebEngine(activity).playerError(error.errorCode,error.getErrorCodeName(),error.getMessage());
                    }

                    @Override
                    public void onRenderedFirstFrame() {
                        new WebEngine(activity).vout();

                        Player.Listener.super.onRenderedFirstFrame();
                    }
                });


                try{
                    timeUpdater();
                }catch (Exception e){
                    System.out.println(e);
                }


            });

    }
    private void timeUpdater(){

        if(timer != null){
            timer.cancel();
            task = null;
        }

        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {

                activity.runOnUiThread(Exo.this::updatePlayerData);

            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    private void updatePlayerData(){

        if(exoPlayer != null){


            if(exoPlayer.isPlaying()){

                playerDuration = exoPlayer.getDuration() / 1000;
                playerPosition = exoPlayer.getCurrentPosition() / 1000;
                volume =  exoPlayer.getVolume();
                state = exoPlayer.getPlaybackState();
                isVideoPlaying =  exoPlayer.getPlayWhenReady();

                activity.runOnUiThread(() -> {
                    try{

                        new WebEngine(activity).androidPlayerTimeUpdate();
                    }catch (Exception e){
                        System.out.println(e);
                        new WebEngine(activity).log(e.getMessage());
                    }
                });
            }
        }

    }
    public String getSelectedTrack(String type){
        String selectedTrack = "";

        for (int i = 0; i < tracks.length();i++){

            try {
                JSONObject obj = tracks.getJSONObject(i);
                if(obj.getString("type").equals(type) && obj.getBoolean("is_selected")){

                    selectedTrack = obj.toString();

                    break;
                }

            }catch (Exception e){
                new WebEngine(activity).log(e.getMessage());
            }

        }

        return selectedTrack;
    }
    public String getTracks(){
        return  tracks.toString();
    }
    public void selectTrack(String type, int track_index,int group_index){

        if(track_index == -1 && type == "TEXT") {
            activity.runOnUiThread(() -> exoPlayer.setTrackSelectionParameters(exoPlayer.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build()));
            return;
        }
        Tracks.Group group = null;
        int index = 0;

        for (Tracks.Group trackGroup : tracksList.getGroups()) {

            if(index == group_index){
                group = trackGroup;
                break;
            }

            index++;

        }

        if(group != null){
            Tracks.Group finalGroup = group;
            activity.runOnUiThread(() -> {
                if(type == "TEXT") {
                    exoPlayer.setTrackSelectionParameters(exoPlayer.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).build());
                }
                exoPlayer.setTrackSelectionParameters(exoPlayer.getTrackSelectionParameters().buildUpon().setOverrideForType(new TrackSelectionOverride(finalGroup.getMediaTrackGroup(), track_index)).build());
            });

        }else{
            System.out.println("Enable to find Group");
        }

    }
    private void detectTracks(Tracks trackList){

        tracks = new JSONArray();

        int index = 0;

        for (Tracks.Group trackGroup : trackList.getGroups()) {

            @C.TrackType int trackType = trackGroup.getType();

            JSONObject object = new JSONObject();

            try {

                object.put("group_index",index);

                String typeText = "UNKNOWN";

                if(trackType == 1) typeText = "AUDIO";

                if(trackType == 2) typeText = "VIDEO";

                if(trackType == 3) typeText = "TEXT";

                object.put("type",typeText);

            }catch (Exception e){
                new WebEngine(activity).log(e.getMessage());
            }

            for (int i = 0; i < trackGroup.length; i++) {

                boolean isSelected = trackGroup.isTrackSelected(i);
                Format trackFormat = trackGroup.getTrackFormat(i);

                try{

                    object.put("index",index);
                    object.put("track_index",i);
                    object.put("is_selected",isSelected);
                    object.put("id", trackFormat.id);
                    object.put("width", trackFormat.width);
                    object.put("height", trackFormat.height);

                    if(trackType == 2){
                        object.put("name", trackFormat.label + "("+trackFormat.width+"x"+trackFormat.height+")");
                    }else{
                        object.put("name", trackFormat.label);
                    }

                }catch (Exception e){
                    new WebEngine(activity).log(e.getMessage());
                }
            }

            index++;

            try{
                tracks.put(object);
                new WebEngine(activity).tracksDetected();
            }catch (Exception e){
                new WebEngine(activity).log(e.getMessage());
            }


        }
    }
    public void play(){

        if(exoPlayer != null) {
            activity.runOnUiThread(() -> exoPlayer.play());
        }

    }
    public void pause(){
        if(exoPlayer != null) {
            activity.runOnUiThread(() -> exoPlayer.pause());
        }
    }
    public long getPosition(){

        if (exoPlayer != null)  {
            activity.runOnUiThread(() -> playerPosition = exoPlayer.getCurrentPosition() / 1000);
        }

        if (playerPosition < 0) playerPosition = 0;

        return playerPosition;
    }
    public void setViewPositions(int width,int height,int x, int y){

        activity.runOnUiThread(() -> {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,height);
            params.leftMargin = x;
            params.topMargin = y;
            exoPlayerView.setLayoutParams(params);

        });

    }
    public long getDuration(){

        activity.runOnUiThread(() -> {
            if(exoPlayer != null) playerDuration = exoPlayer.getDuration() / 1000;
        });

        if (playerDuration < 0) playerDuration = 0;

        return playerDuration;
    }
    public void setAspectRatio(String type){
        if(exoPlayer != null){

            if(type.equals("zoom")){
                activity.runOnUiThread(() -> exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM));
            }

            if(type.equals("original")){
                activity.runOnUiThread(() -> exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT));
            }

            if(type.equals("fixed_width")){
                activity.runOnUiThread(() -> exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH));
            }

            if(type.equals("fixed_height")){
                activity.runOnUiThread(() -> exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT));
            }

            if(type.equals("fill")){
                activity.runOnUiThread(() -> exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL));
            }

        }
    }
    public float getVolume(){
        return volume;
    }
    public void setVolume(float volume){
        activity.runOnUiThread(() -> exoPlayer.setVolume(volume));
    }
    public void setRepeatMode(String repeat_mode){

        if(repeat_mode.equals("one")) exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);

        if(repeat_mode.equals("off")) exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);

        if(repeat_mode.equals("all")) exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);

    }
    public void seekTo(long position){
        if(exoPlayer != null){
            activity.runOnUiThread(() -> exoPlayer.seekTo(position * 1000));
        }
    }
    public boolean isPlaying(){
        return isVideoPlaying;
    }
    public int getSate(){
       return  state;
    }
    public void setPlayerFullScreen(){

        activity.runOnUiThread(() -> {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    new Device(activity).getScreenWidth(),
                    new Device(activity).getScreenHeight()
            );
            exoPlayerView.setLayoutParams(params);
        });

    }
    public void destroy(){

        try{

            activity.runOnUiThread(() -> {

                if(exoPlayer != null) {
                    exoPlayer.release();
                    exoPlayer = null;
                }

            });

        }catch (Exception e){
            new WebEngine(this.activity).log(e.getMessage());
        }

    }

    public void disableTextTrack() {
        activity.runOnUiThread(() -> exoPlayer.setTrackSelectionParameters(exoPlayer.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build()));
    }
}
