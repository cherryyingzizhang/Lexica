package com.example.lexicav1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Cherry_Zhang on 16-06-04.
 */
public class ImageSaver {
    final private static Set<Target> protectedFromGarbageCollectorTargets = new HashSet<>();
    final private String directoryName = "Lexica";
    private String fileName = "";
    private Context context;
    public interface OnFileSavedListener
    {
        void OnFileSaved();
    }

    public ImageSaver(Context context) {
        this.context = context;
    }

    //return the name of the new saved file
    public void save(Uri originalFileUri, String fileName, final OnFileSavedListener onFileSavedListener)
    {
        int dp200 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, context.getResources().getDisplayMetrics());
        this.fileName = fileName;
        Target bitmapTarget = new BitmapTarget(onFileSavedListener);
        protectedFromGarbageCollectorTargets.add(bitmapTarget);
        Picasso.with(context)
                .load(originalFileUri)
                .resize(dp200, dp200).centerInside()
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(bitmapTarget);
    }

    private class BitmapTarget implements Target
    {
        OnFileSavedListener onFileSavedListener;

        public BitmapTarget(OnFileSavedListener onFileSavedListener)
        {
            this.onFileSavedListener = onFileSavedListener;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            try {
                File myDir = createFile();
                FileOutputStream out = new FileOutputStream(myDir);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                onFileSavedListener.OnFileSaved();
            } catch(Exception e){
                // some action
                e.printStackTrace();
                Toast.makeText(context, "Error. You probably refused EXTERNAL STORAGE permissions.",
                        Toast.LENGTH_SHORT).show();
            }
            protectedFromGarbageCollectorTargets.remove(this);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.w("ImageSaver","onBitmapFailed");
            protectedFromGarbageCollectorTargets.remove(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.w("ImageSaver","onPrepareLoad");
        }
    }

    @NonNull
    private File createFile() {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + directoryName);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        return new File(myDir, fileName);
    }

    public File load(String fileName) {
        this.fileName = fileName;
        return createFile();
    }

    //TODO: call this function when application closes?
    public boolean deleteFile(String oldImageFileName)
    {
        this.fileName = oldImageFileName;
        File file = createFile();
        return file.delete();
    }

    public void createNoMediaFile()
    {
        this.fileName = "lexica.nomedia";
        File nomediaFile = createFile();
        if(!nomediaFile.exists())
        {
            try
            {
                nomediaFile.createNewFile();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}