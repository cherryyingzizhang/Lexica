package net.cherryzhang.lexicav1;

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Cherry_Zhang on 16-06-02.
 */
public class ApplicationOverrideMethods extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        String Datetime;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm:ss aa");
        Datetime = dateformat.format(c.getTime());
        Telemetry.editTelemetryLog("---------------\n---------------\nSession Starts: " + Datetime);

        //This must be called so that all images in our Lexica folder will not be visible
        //by the gallery/image picker of the Android OS.
        new ImageSaver(getApplicationContext()).createNoMediaFile();
    }
}
