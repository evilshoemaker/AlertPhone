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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.digios.alertphone.services.AccelerometerSensorService;
import ru.digios.alertphone.services.SmsService;
import ru.digios.alertphone.services.SmsToSend;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        Intent accelerometrSensorService = new Intent(this, AccelerometerSensorService.class);
        startService(accelerometrSensorService);
    }

    public void start(View view)
    {
        String value = readFromFile(this);

        TextView text = findViewById(R.id.statusText);
        text.setText(value);
    }

    public void showSettings(View view)
    {
        /*Intent settingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingsActivity);*/
        SmsToSend message = new SmsToSend("+79234292019", getString(R.string.device_message_alarm_ready));

        Intent mIntent = new Intent(this, SmsService.class);
        mIntent.putExtra("message", message);
        startService(mIntent);
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
