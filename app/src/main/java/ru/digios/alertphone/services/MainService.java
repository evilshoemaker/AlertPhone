package ru.digios.alertphone.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

import ru.digios.alertphone.core.AlarmStatus;

public class MainService extends Service {

    public static final int ALARM_STATUS_ALARM = 10;
    public static final int ALARM_STATUS_READY = 11;
    public static final int ALARM_STATUS_OFF = 12;
    public static final int ALARM_STATUS_ON = 13;

    public static final String COMMAND_ALARM_START = "ALARM_ON";
    public static final String COMMAND_ALARM_STOP = "ALARM_OFF";

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    //public static final int MSG_SET_VALUE = 3;
    public static final int MSG_SET_ALARM_STATUS = 4;

    private String serverPhoneNumber;
    private int currentStatus;

    ArrayList<Messenger> nClients = new ArrayList<Messenger>();

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    nClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    nClients.remove(msg.replyTo);
                    break;
                case MSG_SET_ALARM_STATUS:
                    for (int i=nClients.size()-1; i>=0; i--) {
                        try {
                            nClients.get(i).send(Message.obtain(null, MSG_SET_ALARM_STATUS, msg.arg1, 0));
                        } catch (RemoteException e) {
                            nClients.remove(i);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger messenger = new Messenger(new IncomingHandler());

    /*private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return binder;
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null) {
            return Service.START_STICKY;
        }

        String method = intent.getStringExtra("command");
        if (method.equals(COMMAND_ALARM_START))
        {
            alarmOn();
            //serverPhoneNumber = intent.getStringExtra("serverPhoneNumber");
        }
        else if (method.equals(COMMAND_ALARM_STOP))
        {

        }

        return Service.START_STICKY;
    }

    private void alarmOn() {
        setStatus(ALARM_STATUS_READY);
        //currentStatus = AlarmStatus.ALARM_READY;
    }

    private void alarmOff() {
        setStatus(ALARM_STATUS_OFF);
        //currentStatus = AlarmStatus.ALARM_OFF;
    }

    private void setStatus(int status) {
        currentStatus = status;

        try {
            Message msg = Message.obtain(null, MainService.MSG_SET_ALARM_STATUS, currentStatus, 0);
            messenger.send(msg);
        }
        catch (Exception ex) {

        }
    }

    /*private void sendMessageAsync(String phoneNumber, String message) {
        new Thread(new Runnable() {
            public void run () {
                sendMessage(phoneNumber, message);
            }
        }).start();
    }*/

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
