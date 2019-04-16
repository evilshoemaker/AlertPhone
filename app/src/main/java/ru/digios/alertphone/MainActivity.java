package ru.digios.alertphone;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.digios.alertphone.core.Settings;
import ru.digios.alertphone.services.AccelerometerSensorService;
import ru.digios.alertphone.services.MainService;
import ru.digios.alertphone.services.SmsService;
import ru.digios.alertphone.services.SmsToSend;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    private int currentAlarmStatus;

    private Messenger mainService = null;

    private TextView statusText = null;

    private EditText serverPhoneEdit = null;
    private EditText alarmIntervalEdit = null;
    private EditText alarmCountEdit = null;
    private EditText shakeThresholdEdit = null;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainService.MSG_SET_ALARM_STATUS:
                    setStatus(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger messenger = new Messenger(new IncomingHandler());

    private ServiceConnection mainServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,  IBinder service) {

            mainService = new Messenger(service);

            try {
                Message msg = Message.obtain(null, MainService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                mainService.send(msg);

                // Give it some value as an example.
                /*msg = Message.obtain(null,
                        MessengerService.MSG_SET_VALUE, this.hashCode(), 0);
                mService.send(msg);*/
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInterface();
        requestPermissions();

        bindService(new Intent(MainActivity.this, MainService.class), mainServiceConnection, Context.BIND_AUTO_CREATE);

        /*Intent accelerometrSensorService = new Intent(this, AccelerometerSensorService.class);
        startService(accelerometrSensorService);*/
    }

    @Override
    public void onStart() {
        super.onStart();

        loadSettings();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void loadSettings() {
        serverPhoneEdit.setText(Settings.getInstance(this).getServerPhone());
        alarmIntervalEdit.setText(String.valueOf(Settings.getInstance(this).getAlarmInterval()));
        alarmCountEdit.setText(String.valueOf(Settings.getInstance(this).getAlarmCount()));
        shakeThresholdEdit.setText(String.valueOf(Settings.getInstance(this).getShakeThreshold()));
    }

    private void saveSettings() {
        try {
            Settings.getInstance(this).setServerPhone(serverPhoneEdit.getText().toString());

            String alarmItervalStr = alarmIntervalEdit.getText().toString();
            Settings.getInstance(this).setAlarmInterval(Long.parseLong(alarmItervalStr));

            String alarmCountStr = alarmCountEdit.getText().toString();
            Settings.getInstance(this).setAlarmCount(Integer.parseInt(alarmCountStr));

            String shakeThresholdStr = shakeThresholdEdit.getText().toString();
            Settings.getInstance(this).setShakeThreshold(Float.parseFloat(shakeThresholdStr));
        }
        catch (Exception ex) {
            Log.e("", ex.getMessage());
        }

        Settings.getInstance(this).save(this);
    }

    public void start(View view)
    {
        if (currentAlarmStatus != MainService.ALARM_STATUS_OFF) {
            Intent service = new Intent(this, MainService.class);
            service.putExtra("command", MainService.COMMAND_ALARM_START);
            startService(service);
        }
        else {
            Intent service = new Intent(this, MainService.class);
            service.putExtra("command", MainService.COMMAND_ALARM_STOP);
            startService(service);
        }

        //saveSettings();
    }

    private void initInterface() {
        serverPhoneEdit = findViewById(R.id.serverPhoneEdit);
        alarmIntervalEdit = findViewById(R.id.alarmIntervalEdit);
        alarmCountEdit = findViewById(R.id.alarmCountEdit);
        shakeThresholdEdit = findViewById(R.id.shakeThresholdEdit);

        statusText = findViewById(R.id.statusText);
    }

    private void requestPermissions()
    {
        requestPermissionsSendSms();
    }

    private void requestPermissionsSendSms()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    private void setStatus(int status) {
        currentAlarmStatus = status;

        switch (status) {
            case MainService.ALARM_STATUS_ALARM:
                statusText.setText("ALARM");
                break;
            case MainService.ALARM_STATUS_OFF:
                statusText.setText("OFF");
                break;
            case MainService.ALARM_STATUS_ON:
                statusText.setText("ON");
                break;
            case MainService.ALARM_STATUS_READY:
                statusText.setText("READY");
                break;
            default:
                statusText.setText("...");
                break;

        }
    }
}
