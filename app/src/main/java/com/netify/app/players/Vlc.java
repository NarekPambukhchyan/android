package com.netify.app.players;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.netify.app.Engine.Device;
import com.netify.app.Engine.WebEngine;

import org.json.JSONArray;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.util.VLCVideoLayout;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Vlc {

    private MediaPlayer vlcPlayer;

    private JSONArray tracks;
    private VLCVideoLayout vlcPlayerView;
    private LibVLC libVLC;
    private Activity activity;

    private  boolean isSeeked = false;

    private long duration = 0;
    private float position = 0;

    private boolean useCustomDuration = false;

    private long customDuration = 0;

    private FrameLayout parent;

    public Vlc(Activity activity,VLCVideoLayout vlcPlayerView,FrameLayout parent){
        this.vlcPlayerView = vlcPlayerView;
        this.activity = activity;
        this.parent = parent;
    }

    public void enableCustomDuration(long duration){
        this.useCustomDuration = true;
        this.customDuration  = duration;
    }

    public void disableCustomDuration(){
        this.useCustomDuration = false;
    }

    public String getDefaultFont() {
        System.out.println("getFontList(): entry");
        File configFilename = new File("/system/etc/system_fonts.xml");
        String defaultFontName = "";

        try {
            FileInputStream fontsIn = new FileInputStream(configFilename);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fontsIn, null);
            Boolean done = false;
            Boolean getTheText = false;
            int eventType;
            String defaultFont = "";
            while (!done) {
                eventType = parser.next();
                if (eventType == parser.START_TAG && parser.getName().equalsIgnoreCase("file")) {
                    // the text is next up -- pull it
                    getTheText = true;
                }
                if (eventType == parser.TEXT && getTheText == true) {
                    // first name
                    defaultFont = parser.getText();
                    System.out.println("text for file tag:" + defaultFont);
                    done = true;
                }
                if (eventType == parser.END_DOCUMENT) {
                    System.out.println("hit end of system_fonts.xml document");
                    done = true;
                }
            }

            if (defaultFont.length() > 0) {
                defaultFontName = "/system/fonts/" + defaultFont;
            }

        } catch (RuntimeException e) {
            System.err.println("Didn't create default family (most likely, non-Minikin build)");
            // TODO: normal in non-Minikin case, remove or make error when Minikin-only
        } catch (FileNotFoundException e) {
            System.err.println("GetDefaultFont: config file Not found");
        } catch (IOException e) {
            System.err.println("GetDefaultFont: IO exception: " + e.getMessage());
        } catch (XmlPullParserException e) {
            System.err.println("getDefaultFont: XML parse exception " + e.getMessage());
        }
        return defaultFontName;
    }
    public void init(String url, long ms){

        isSeeked = false;
        activity.runOnUiThread(() -> {

            ArrayList<String> options = new ArrayList<>();

            options.add("--no-drop-late-frames");
            options.add("--adaptive-logic=rate");
            options.add("--preferred-resolution=-1");

            options.add("--no-skip-frames");
            options.add("--rtsp-tcp");
            options.add("-vvv");
            options.add("--no-sout-x264-fast-pskip");
            options.add("--no-sout-x264-dct-decimate");
            options.add("--no-sout-x264-asm");
            options.add("--sout-x264-psnr");
            options.add("--sout-x264-keyint=-1");
            options.add("--sout-x264-opengop");
            options.add("--no-sout-x264-cabac");
            options.add("--no-sout-x264-weightb");
            options.add("--no-sout-x264-mbtree");
            options.add("--sout-rtp-proto=udp");
            options.add("--demuxdump-access=udp");
            options.add("--access==udp");
            //use this to disable hardware acceleration
            options.add("--avcodec-hw=none");


            if (vlcPlayer != null) {

                vlcPlayer.release();
                vlcPlayer.detachViews();
                vlcPlayer = null;

            }
            
            if (libVLC != null) {
                libVLC.release();
                libVLC = null;
            }
            libVLC = new LibVLC(activity.getApplicationContext(), options);
            vlcPlayer = new MediaPlayer(libVLC);

            vlcPlayer.attachViews(vlcPlayerView, null, false, false);

            vlcPlayer.setEventListener(event -> {
                switch (event.type) {
                    case MediaPlayer.Event.EndReached:
                        activity.runOnUiThread(() -> new WebEngine(activity).streamEnded());
                        break;
                    case MediaPlayer.Event.EncounteredError:
                        activity.runOnUiThread(() -> new WebEngine(activity).playerError(0,"Error","Unknown Player Error"));
                        break;
                    case MediaPlayer.Event.Buffering:
                        //TODO understand how to make buffering same way like exo
                        activity.runOnUiThread(() -> new WebEngine(activity).buffering(-1,-1));
                        break;
                    case MediaPlayer.Event.Playing:
                    case MediaPlayer.Event.Paused:

                        activity.runOnUiThread(() -> {
                            detectTracks();
                            new WebEngine(activity).isPlayingChanged(isPlaying());
                        });

                        break;
                    case MediaPlayer.Event.PositionChanged:


                        if(vlcPlayerView.getVisibility() == View.INVISIBLE && vlcPlayer != null &&  vlcPlayer.isPlaying()){
                           new Handler().postDelayed (() -> {
                                try {
                                    vlcPlayerView.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    new WebEngine(activity).log(e.getMessage());
                                }
                            }, 200);
                        }
                        if(useCustomDuration){
                            duration = this.customDuration;
                            position = vlcPlayer.getPosition() * this.customDuration;
                        }else{
                            duration = vlcPlayer.getLength() / 1000;
                            position = vlcPlayer.getPosition() * duration;
                        }

                        new WebEngine(activity).androidPlayerTimeUpdate();
                        break;
                    case MediaPlayer.Event.Vout:
                        activity.runOnUiThread(() -> new WebEngine(activity).vout());
                        if (!isSeeked && ms != 0) {
                            activity.runOnUiThread(() -> vlcPlayer.setPosition(ms * 1000.0f / vlcPlayer.getLength())); {
                                isSeeked = true;
                            }
                        }
                        break;
                }
            });

            vlcPlayer.play(Uri.parse(url));
        });



    }


    public String getVideoResolution(){
        String resolution = vlcPlayer.getCurrentVideoTrack().width +"x"+ vlcPlayer.getCurrentVideoTrack().height;
        return resolution;

    }
    public void setFractionalPosition(float position) {
        vlcPlayer.setPosition(position);
    }

    public void setViewPositions(int width,int height,int x, int y){

        activity.runOnUiThread(() -> {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,height);
            params.leftMargin = x;
            params.topMargin = y;

            parent.setLayoutParams(params);

        });



    }
    public void setPlayerFullScreen(){

        activity.runOnUiThread(() -> {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    new Device(activity).getScreenWidth(),
                    new Device(activity).getScreenHeight()
            );

            params.leftMargin = 0;
            params.topMargin = 0;

            parent.setLayoutParams(params);

            System.out.println("ssss");
        });

    }
    public boolean isPlaying(){
        boolean isPlaying = false;
        if(vlcPlayer != null && vlcPlayer.isPlaying()) {
            isPlaying = true;
        }
        return  isPlaying;
    }
    public int getSate(){
        return  vlcPlayer.getPlayerState();
    }
    public void setRepeatMode(String mode){}
    public void seekTo(long ms){

        float newPosition = ms  * 1000f / vlcPlayer.getLength();
        vlcPlayer.setPosition(newPosition);
    }
    public void play(){
        activity.runOnUiThread(() -> {
            if(vlcPlayer != null) vlcPlayer.play();

        });

    }
    public void pause(){
        if(vlcPlayer != null)  vlcPlayer.pause();
    }
    public long getDuration(){
        if(this.useCustomDuration){
            return  this.customDuration;
        }else{
            return duration;
        }
    }
    public long getPosition(){
        return (long) position;
    }
    public void setVolume(int volume){
        if(vlcPlayer != null)vlcPlayer.setVolume(volume * 100);
    }
    public float getVolume(){

        if (vlcPlayer.getVolume() != 0 && vlcPlayer != null){
            return vlcPlayer.getVolume() / 100f;
        }else{
            return 0;
        }

    }

    private void detectTracks(){
        if(vlcPlayer != null)
        {
            tracks = new JSONArray();

            MediaPlayer.TrackDescription[] audioTracks = vlcPlayer.getAudioTracks();
            MediaPlayer.TrackDescription[] videoTracks =vlcPlayer.getVideoTracks();
            MediaPlayer.TrackDescription[] spuTracks =vlcPlayer.getSpuTracks();

            int defaultAudio = vlcPlayer.getAudioTrack();
            int defaultSubtitle = vlcPlayer.getSpuTrack();
            int defaultVideo = vlcPlayer.getVideoTrack();

            int selectedAudio = vlcPlayer.getSpuTrack();
            int selectedSubtitle = vlcPlayer.getAudioTrack();
            int selectedVideo = vlcPlayer.getVideoTrack();

            createTrackObject("AUDIO", audioTracks,defaultAudio,selectedAudio);
            createTrackObject("VIDEO", videoTracks,defaultVideo,selectedVideo);
            createTrackObject("TEXT", spuTracks,defaultSubtitle,selectedSubtitle);


            new WebEngine(activity).tracksDetected();
            new WebEngine(activity).log(tracks.toString());
        }
    }
    public String getTracks(){

        try{
            return tracks.toString();
        }catch (Exception e){
            return  "";
        }


    }

    private void createTrackObject(String type, MediaPlayer.TrackDescription[] tracksList, int defaultId,int selectedId){

        try {

            for (int i = 0; i < tracksList.length;i++){
                JSONObject json = new JSONObject();
                MediaPlayer.TrackDescription track = tracksList[i];

                json.put("group_index",1);
                json.put("type", type);
                json.put("name", track.name);
                json.put("id", track.id);
                json.put("track_index",i);
                json.put("index",track.id);
                json.put("width", -1);
                json.put("height", -1);


                if (track.id == defaultId){
                    json.put("is_selected",true);
                }else{
                    json.put("is_selected",false);
                }

                if(track.id == selectedId) {
                    json.put("default", true);
                }else{
                    json.put("default",false);
                }

                tracks.put(json);
                Log.d("TAG", "createTrackObject: " + json);

            }

        }catch (Exception e){
            new WebEngine(activity).log(e.getMessage());
        }
    }

    public String getSelectedTrack(String  type){

        String selectedTrack = "{}";

        try {

            for (int i = 0; i < tracks.length();i++){

                JSONObject t = tracks.getJSONObject(i);

                if (t.getString("type").equals(type) && t.getBoolean("is_selected")){
                    selectedTrack = t.toString();
                    break;
                }

            }

        }catch (Exception e){
            new WebEngine(activity).log(e.getMessage());
        }


        return  selectedTrack;

    }
    public void setTrack(String type,int index){

        try {

            if (type.equals("AUDIO")) vlcPlayer.setAudioTrack(index);

            if (type.equals("VIDEO")) vlcPlayer.setVideoTrack(index);

            if (type.equals("TEXT")) vlcPlayer.setSpuTrack(index);

        } catch (Exception e) {
            new WebEngine(activity).log(e.getMessage());
        }

    }
    public void destroy(){

        activity.runOnUiThread(() -> {


            if (vlcPlayer != null) {


                vlcPlayer.stop();
                vlcPlayerView.setVisibility(View.INVISIBLE);
                vlcPlayer.detachViews();
                vlcPlayer.release();
                vlcPlayer = null;

            }
            if (libVLC != null) {
                libVLC.release();
                libVLC = null;
            }
        });

    }
    public void setAspectRatio(String type){

//        (null)
//        Default
//        16:9
//        4:3
//        1:1
//        16:10
//        21:1
//        35:1
//        39:1
//        5:4
        if(vlcPlayer != null)
            Toast.makeText(activity,type,Toast.LENGTH_SHORT).show();;
            vlcPlayer.setAspectRatio(type);
        }

    public void disableTextTrack() {
        try  {
            vlcPlayer.setSpuTrack(-1);
        } catch (Exception e) {
            new WebEngine(activity).log(e.getMessage());
        }
    }
}
