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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.digios.alertphone.core.Settings;
import ru.digios.alertphone.services.AccelerometerSensorService;
import ru.digios.alertphone.services.MainService;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 0;

    private int currentAlarmStatus = MainService.ALARM_STATUS_OFF;

    private Messenger mainService = null;
    private boolean mBound;
    private Intent mainServiceItent = null;

    private Button startButton = null;
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
                case MainService.MSG_GET_ALARM_STATUS:
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
            mBound = true;

            try {
                Message msg = Message.obtain(null, MainService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                mainService.send(msg);

                msg = Message.obtain(null, MainService.MSG_GET_ALARM_STATUS, 0, 0);
                mainService.send(msg);
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInterface();
        requestPermissions();

        Intent accelerometrSensorService = new Intent(this, AccelerometerSensorService.class);
        startService(accelerometrSensorService);

        mainServiceItent = new Intent(this, MainService.class);
        startService(mainServiceItent);

    }

    @Override
    public void onStart() {
        super.onStart();

        loadSettings();
        //bindService(mainServiceItent, mainServiceConnection, Context.BIND_AUTO_CREATE);
        //bindService(new Intent(MainActivity.this, MainService.class), mainServiceConnection, /*Context.BIND_AUTO_CREATE*/0);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            //unbindService(mainServiceConnection);
            mBound = false;
        }
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
        if (currentAlarmStatus == MainService.ALARM_STATUS_OFF) {
            if (!checkSettings())
                return;

            saveSettings();

            Intent service = new Intent(this, MainService.class);
            service.putExtra("command", MainService.COMMAND_ALARM_START);
            service.putExtra("serverPhoneNumber", Settings.getInstance(this).getServerPhone());
            service.putExtra("alarmInterval", Settings.getInstance(this).getAlarmInterval());
            service.putExtra("alarmCount", Settings.getInstance(this).getAlarmCount());
            service.putExtra("shakeThreshold", Settings.getInstance(this).getShakeThreshold());
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
        startButton = findViewById(R.id.startButton);
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
                        PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    private void setStatus(int status) {
        currentAlarmStatus = status;

        if (status != MainService.ALARM_STATUS_OFF) {
            setEnableSettings(false);
            startButton.setText("Stop");
        }
        else {
            setEnableSettings(true);
            startButton.setText("Start");
        }

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

    private boolean checkSettings() {
        if (serverPhoneEdit.getText().toString().isEmpty()) {
            showMessage("Server phone is empty");
            return false;
        }

        return true;
    }

    private void showMessage(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setEnableSettings(boolean isEnable) {
        serverPhoneEdit.setEnabled(isEnable);
        alarmIntervalEdit.setEnabled(isEnable);
        alarmCountEdit.setEnabled(isEnable);
        shakeThresholdEdit.setEnabled(isEnable);
    }
}
