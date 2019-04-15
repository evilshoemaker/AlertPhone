package ru.digios.alertphone.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SmsService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY;
        }

        /*Serializable obj = intent.getSerializableExtra("message");
        if (obj == null) {
            return Service.START_STICKY;
        }

        executeAsync((Message)obj);*/

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
