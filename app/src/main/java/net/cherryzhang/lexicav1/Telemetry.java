package net.cherryzhang.lexicav1;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Cherry_Zhang on 16-06-02.
 * Class that includes methods to log telemetry.
 */
public class Telemetry
{
    public static void editTelemetryLog(String textToAppend)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
        Date now = new Date();
        String fileName = formatter.format(now) + ".txt"; // e.g. 2016_01_12.txt

        try {
            File folder = new File(Environment.getExternalStorageDirectory(), "Lexica");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File sessionFile = new File(folder, fileName);
            FileOutputStream fOut = new FileOutputStream(sessionFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(textToAppend);
            myOutWriter.append("\n");
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
