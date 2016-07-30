package com.example.lexicav1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 * Created by Cherry_Zhang on 2016-07-28.
 */
public class FontHelper
{
    static public void changeFont(Context activity, TextView textview)
    {
        SharedPreferences getPrefs =
                PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String font = getPrefs.getString("Font", "Default");
        if (font.contentEquals("Default"))
        {
            textview.setTypeface(Typeface.DEFAULT);
        }
        else if (font.contentEquals("Handwriting"))
        {
            textview.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/handwriting.ttf"));
        }
        else if (font.contentEquals("3d"))
        {
            textview.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/3d.ttf"));
        }
    }
}
