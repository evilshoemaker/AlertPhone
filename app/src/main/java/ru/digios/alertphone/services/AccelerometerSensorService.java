package ru.digios.alertphone.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

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

    Sensor accelerometer;
    SensorManager sm;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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
        Intent settingsActivity = new Intent(this, SettingsActivity.class);
        settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(settingsActivity);
    }
}
