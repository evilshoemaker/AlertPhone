package ru.digios.alertphone.core;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ru.digios.alertphone.R;

public class SoundManager {

    private long interval;
    private int alarmCount;
    private int currentAlarmCount;

    MediaPlayer sirenMediaPlayer;
    MediaPlayer drwMediaPlayer;

    Handler handler = new Handler();


    public SoundManager(Context context) {

        drwMediaPlayer = MediaPlayer.create(context, R.raw.drw);
        drwMediaPlayer.setLooping(false);

        sirenMediaPlayer = MediaPlayer.create(context, R.raw.sirena);
        sirenMediaPlayer.setLooping(false);
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setAlarmCount(int alarmCount) {
        this.alarmCount = alarmCount;
    }

    public void playSirenaSound() {
        currentAlarmCount = 0;

        sirenMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                currentAlarmCount++;

                if (currentAlarmCount < alarmCount) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sirenMediaPlayer.start();
                        }

                    }, interval);
                }
                else {
                    stopSirenaSound();
                }
            }
        });

        sirenMediaPlayer.start();
    }

    public void stopSirenaSound() {
        if (drwMediaPlayer.isPlaying())
            drwMediaPlayer.stop();

        handler.removeCallbacksAndMessages(null);
    }

    public void playDrwSound() {
        if (drwMediaPlayer.isPlaying())
            drwMediaPlayer.stop();

        drwMediaPlayer.start();
    }
}
