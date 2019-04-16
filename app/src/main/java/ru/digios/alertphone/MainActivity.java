package ru.digios.alertphone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import ru.digios.alertphone.services.SmsService;
import ru.digios.alertphone.services.SmsToSend;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    private EditText serverPhoneEdit = null;
    private EditText alarmIntervalEdit = null;
    private EditText alarmCountEdit = null;
    private EditText shakeThresholdEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initInterface();
        requestPermissions();

        Intent accelerometrSensorService = new Intent(this, AccelerometerSensorService.class);
        startService(accelerometrSensorService);
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
        saveSettings();
        /*String value = readFromFile(this);

        TextView text = findViewById(R.id.statusText);
        text.setText(value);*/
    }

    private void initInterface() {
        serverPhoneEdit = findViewById(R.id.serverPhoneEdit);
        alarmIntervalEdit = findViewById(R.id.alarmIntervalEdit);
        alarmCountEdit = findViewById(R.id.alarmCountEdit);
        shakeThresholdEdit = findViewById(R.id.shakeThresholdEdit);
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

    private String readFromFile(Context context)
    {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
