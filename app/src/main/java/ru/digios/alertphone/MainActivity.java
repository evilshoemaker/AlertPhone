package ru.digios.alertphone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ru.digios.alertphone.services.AccelerometerSensorService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent accelerometrSensorService = new Intent(this, AccelerometerSensorService.class);
        startService(accelerometrSensorService);
    }

    public void showSettings(View view) {
        Intent settingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingsActivity);
    }
}
