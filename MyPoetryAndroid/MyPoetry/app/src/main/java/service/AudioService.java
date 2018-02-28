package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Administrator on 2018/1/11.
 */

public class AudioService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{
    private static String tag = "AudioService";
    private MediaPlayer mediaPlayer;
    private MyBinder myBinder = new MyBinder();
    protected Context context;
    private Handler handler;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(tag, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        initPlayer();
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(tag, "onBind");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(tag, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(tag, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {//播放完时
        Log.e(tag,"播放完");
        if (handler != null) {
            handler.sendEmptyMessage(0);
        }
        //stopSelf();
    }
/****/
public void initPlayer(){
    try {
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //mediaPlayer.setOnBufferingUpdateListener(this);
            //mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void setPlayUrl(String url) {
        try {
            destoryMediaPlayer();
            initPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException|SecurityException|IllegalStateException|IOException e) {
            e.printStackTrace();
        }
    }

    public void destoryMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(null);
                //mediaPlayer.setOnPreparedListener(null);
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(tag, "出错XXXXX");
        }
    }

    public void pause() {
        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //mediaPlayer.setOnBufferingUpdateListener(this);
            //mediaPlayer.setOnPreparedListener(this);
        }
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(tag, "出错XXXXX");
        return true;
    }

    /****/
    public class MyBinder extends Binder {
        public AudioService getService(){
            Log.e(tag,"return service "+AudioService.this);
            return AudioService.this;
        }
    }

    public void play() {
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
/************/
    public int getMusicDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void setMusicCurrentPosition(int currentPosition) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(currentPosition);
        }
    }
    public int getMusicCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
}

