package ru.digios.alertphone.core;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Settings {
    private static final String CONFIG_FILE_NAME = "config.json";

    private static Settings instance;

    public static synchronized Settings getInstance(Context context) {
        if (instance == null) {
            load(context);
            //instance = new Settings(context.getApplicationContext());
        }
        return instance;
    }

    private String serverPhone = "";
    private long alarmInterval = 3000;
    private int alarmCount = 5;
    private float shakeThreshold = 2.7f;

    public void save(Context context)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            OutputStream outputStream = context.openFileOutput(CONFIG_FILE_NAME, Context.MODE_PRIVATE);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write(gson.toJson(this));
            outputStreamWriter.close();

            Log.i("","Save settings success");
        }
        catch (Exception e) {
            Log.e("","File settings write failed: " + e.toString());
        }
    }

    public static void load(Context context)
    {
        try {
            //File file = new File(CONFIG_FILE_NAME);
            //FileInputStream inputStream = new FileInputStream(file);
            InputStream inputStream = context.openFileInput(CONFIG_FILE_NAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                stringBuilder.toString();

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                instance = gson.fromJson(stringBuilder.toString(), Settings.class);

                if (instance == null) {
                    instance = new Settings();
                }

                Log.i("","Load settings success");
            }
        }
        catch (Exception e) {
            Log.e("","Can not load settings file. Load default settings");
            instance = new Settings();
        }
    }

    public String getServerPhone() {
        return serverPhone;
    }

    public void setServerPhone(String serverPhone) {
        this.serverPhone = serverPhone;
    }

    public long getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(long alarmInterval) {
        this.alarmInterval = alarmInterval;
    }

    public int getAlarmCount() {
        return alarmCount;
    }

    public void setAlarmCount(int alarmCount) {
        this.alarmCount = alarmCount;
    }

    public float getShakeThreshold() {
        return shakeThreshold;
    }

    public void setShakeThreshold(float shakeThreshold) {
        this.shakeThreshold = shakeThreshold;
    }
}
