package ru.digios.alertphone.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import ru.digios.alertphone.core.Settings;
import ru.digios.alertphone.core.SoundManager;
import ru.digios.alertphone.core.Util;

public class MainService extends Service {

    private static final String SERVER_MESSAGE_ALARM_ON = "alarm_on";
    private static final String SERVER_MESSAGE_ALARM_OFF = "alarm_off";
    private static final String SERVER_MESSAGE_OPEN = "open";
    private static final String DEVICE_MESSAGE_ALARM_READY = "alarm_ready";
    private static final String DEVICE_MESSAGE_ALARM_ON = "alarm_on";
    private static final String DEVICE_MESSAGE_ALARM = "alarm";

    public static final int ALARM_STATUS_ALARM = 10;
    public static final int ALARM_STATUS_READY = 11;
    public static final int ALARM_STATUS_OFF = 12;
    public static final int ALARM_STATUS_ON = 13;

    public static final String COMMAND_ALARM_START = "ALARM_ON";
    public static final String COMMAND_ALARM_STOP = "ALARM_OFF";
    public static final String COMMAND_EXEC_MESSAGE = "INCOME_MESSAGE";
    public static final String COMMAND_THRESHOLD_TRIGGER = "THRESHOLD_TRIGGER";

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_ALARM_STATUS = 4;
    public static final int MSG_GET_ALARM_STATUS = 5;

    private String serverPhoneNumber;
    private int alarmCount;
    private long alarmInterval;
    private float shakeThreshold;
    private int currentStatus = ALARM_STATUS_OFF;
    private SoundManager soundManager;

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
                case MSG_GET_ALARM_STATUS:
                    for (int i=nClients.size()-1; i>=0; i--) {
                        try {
                            nClients.get(i).send(Message.obtain(null, MSG_SET_ALARM_STATUS, currentStatus, 0));
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        soundManager = new SoundManager(this);
        loadSettings();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY;
        }

        String method = intent.getStringExtra("command");
        if (method == null) {
            return Service.START_STICKY;
        }

        if (method.equals(COMMAND_ALARM_START))
        {
            serverPhoneNumber = Util.clearPhone(intent.getStringExtra("serverPhoneNumber"));
            alarmInterval = intent.getLongExtra("alarmInterval", 3000);
            alarmCount = intent.getIntExtra("alarmCount", 5);
            shakeThreshold = intent.getFloatExtra("shakeThreshold", 2.7f);

            soundManager.setAlarmCount(alarmCount);
            soundManager.setInterval(alarmInterval);

            alarmReady();
        }
        else if (method.equals(COMMAND_ALARM_STOP))
        {
            alarmOff();
        }
        else if (method.equals(COMMAND_EXEC_MESSAGE))
        {
            String phoneNumber = intent.getStringExtra("phoneNumber");
            String message = intent.getStringExtra("message");

            handlingMessageAsync(phoneNumber, message);
        }
        else if (method.equals(COMMAND_THRESHOLD_TRIGGER))
        {
            alarm();
        }

        return Service.START_STICKY;
    }

    private void loadSettings() {
        serverPhoneNumber = Settings.getInstance(this).getServerPhone();
        alarmInterval = Settings.getInstance(this).getAlarmInterval();
        alarmCount = Settings.getInstance(this).getAlarmCount();
        shakeThreshold = Settings.getInstance(this).getShakeThreshold();
        currentStatus = Settings.getInstance(this).getAlarmStatus();

        soundManager.setAlarmCount(alarmCount);
        soundManager.setInterval(alarmInterval);
    }

    private void alarmReady() {
        setStatus(ALARM_STATUS_READY);

        sendMessageAsync(serverPhoneNumber, DEVICE_MESSAGE_ALARM_READY);

        Intent accelerometrSensorService = new Intent(this, AccelerometerSensorService.class);
        accelerometrSensorService.putExtra("shakeThreshold", shakeThreshold);
        startService(accelerometrSensorService);
    }

    private void alarmOn() {
        setStatus(ALARM_STATUS_ON);
        sendMessageAsync(serverPhoneNumber, DEVICE_MESSAGE_ALARM_ON);
    }

    private void alarm() {
        if (currentStatus != ALARM_STATUS_ON)
            return;

        setStatus(ALARM_STATUS_ALARM);
        sendMessageAsync(serverPhoneNumber, DEVICE_MESSAGE_ALARM);

        soundManager.playSirenaSound();
    }

    private void alarmReset() {
        soundManager.stopSirenaSound();
    }

    private void alarmOff() {
        alarmReset();
        setStatus(ALARM_STATUS_OFF);
    }

    private void setStatus(int status) {
        currentStatus = status;
        Settings.getInstance(this).setAlarmStatus(currentStatus);
        Settings.getInstance(this).save(this);

        try {
            Message msg = Message.obtain(null, MainService.MSG_SET_ALARM_STATUS, currentStatus, 0);
            messenger.send(msg);
        }
        catch (Exception ex) { }
    }

    private void sendMessageAsync(final String phoneNumber, final String message) {
        new Thread(new Runnable() {
            public void run () {
                sendMessage(phoneNumber, message);
            }
        }).start();
    }

    private void sendMessage(final String phoneNumber, final String message) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
        else {
            showMessage("SEND_SMS_PERMISSION_DENIED");
            Log.w("","SEND_SMS_PERMISSION_DENIED");
        }
    }

    private void handlingMessageAsync(final String phoneNumber, final String message) {
        handlingMessage(phoneNumber, message);
        /*new Thread(new Runnable() {
            public void run () {
                handlingMessage(phoneNumber, message);
            }
        }).start();*/
    }

    private void handlingMessage(final String phoneNumber, final String message) {
        String simplePhone = Util.clearPhone(phoneNumber);
        String simpleMessage = message.toLowerCase();

        if (serverPhoneNumber != null && !simplePhone.equals(serverPhoneNumber)
                || currentStatus == ALARM_STATUS_OFF)
            return;

        if (currentStatus == ALARM_STATUS_READY
                && simpleMessage.indexOf(SERVER_MESSAGE_ALARM_ON) > -1) {
            alarmOn();
        }
        else if (currentStatus == ALARM_STATUS_ON
                && simpleMessage.indexOf(SERVER_MESSAGE_ALARM_OFF) > -1) {
            alarmReady();
        }
        else if (currentStatus == ALARM_STATUS_ALARM
                && simpleMessage.indexOf(SERVER_MESSAGE_ALARM_OFF) > -1) {
            alarmReset();
            alarmReady();
        }
        else if ((currentStatus == ALARM_STATUS_ON || currentStatus == ALARM_STATUS_ALARM)
                && simpleMessage.indexOf(SERVER_MESSAGE_OPEN) > -1) {
            alarmReset();
            alarmReady();
            soundManager.playDrwSound();
        }
    }

    private void showMessage(String text) {
        Toast toast = Toast.makeText(MainService.this, text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
