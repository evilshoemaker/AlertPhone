package ru.digios.alertphone.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.Serializable;

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

        Serializable obj = intent.getSerializableExtra("message");
        if (obj != null)
        {
            if (obj instanceof SmsToSend)
                sendMessageAsync((SmsToSend)obj);

            if (obj instanceof SmsToReceive)
                executeMessageAsync((SmsToReceive)obj);
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void executeMessageAsync(SmsToReceive message) {

    }

    private void sendMessageAsync(final SmsToSend message) {
        new Thread(new Runnable() {
            public void run () {
                sendMessage(message.getPhoneNumber(), message.getMessage());
            }
        }).start();
    }

    private void sendMessage(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
        else {
            Log.w("","SEND_SMS_PERMISSION_DENIED");
        }
    }
}
