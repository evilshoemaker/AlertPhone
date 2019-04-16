package ru.digios.alertphone.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import ru.digios.alertphone.SettingsActivity;

public class AccelerometerSensorService extends Service implements SensorEventListener
{
    float xAccel;
    float yAccel;
    float zAccel;
    float xPreviousAccel;
    float yPreviousAccel;
    float zPreviousAccel;

    boolean firstUpdate = true;
    boolean shakeInitiated = false;
    float shakeThreshold = 2.7f;

    Sensor accelerometer = null;
    SensorManager sm = null;



    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "21867216-040b-4672-8019-5ff9e0c39a05");
        wakeLock.acquire();

        /*sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //wakeLock.release();
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (sm == null || accelerometer == null)
        {
            sm = (SensorManager)getSystemService(SENSOR_SERVICE);
            accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        updateAccelParameters(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

        if (!shakeInitiated && isAccelerationChanged())
        {
            shakeInitiated = true;
        }
        else if (shakeInitiated && isAccelerationChanged())
        {
            executeShackeAction();
        }
        else if (shakeInitiated && !isAccelerationChanged())
        {
            shakeInitiated = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateAccelParameters(float xNewAcccel, float yNewAccel, float zNewAccell)
    {
        if (firstUpdate)
        {
            xPreviousAccel = xNewAcccel;
            yPreviousAccel = yNewAccel;
            zPreviousAccel = zNewAccell;
            firstUpdate = false;
        }
        else
        {
            xPreviousAccel = xAccel;
            yPreviousAccel = yAccel;
            zPreviousAccel = zAccel;
        }

        xAccel = xNewAcccel;
        yAccel = yNewAccel;
        zAccel = zNewAccell;
    }

    private boolean isAccelerationChanged()
    {
        float deltaX = Math.abs(xPreviousAccel - xAccel);
        float deltaY = Math.abs(yPreviousAccel - yAccel);
        float deltaZ = Math.abs(zPreviousAccel - zAccel);

        return (deltaX > shakeThreshold && deltaY > shakeThreshold)
                || (deltaX > shakeThreshold && deltaZ > shakeThreshold)
                || (deltaY > shakeThreshold && deltaZ > shakeThreshold);
    }

    private void executeShackeAction()
    {
        /*Intent settingsActivity = new Intent(this, SettingsActivity.class);
        settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(settingsActivity);*/
        try {
            String val = readFromFile(this);
            int v = Integer.parseInt(val);
            v++;

            writeToFile(String.valueOf(v), this);
        }
        catch (Exception ex)
        {
            writeToFile("1", this);
        }


    }

    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
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
