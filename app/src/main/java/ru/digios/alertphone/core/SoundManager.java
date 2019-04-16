package ru.digios.alertphone.core;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SoundManager {

    private long interval;
    private int alarmCount;

    private Timer timer;

    public SoundManager() {
        timer = new Timer();
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setAlarmCount(int alarmCount) {
        this.alarmCount = alarmCount;
    }

    public void playAlarmSound() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                        "dd:MMMM:yyyy HH:mm:ss a", Locale.getDefault());
                final String strDate = simpleDateFormat.format(calendar.getTime());
                Log.i("TEST_TIMER", strDate);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, interval);
    }

    public void stopAlarmSound() {

    }
}
