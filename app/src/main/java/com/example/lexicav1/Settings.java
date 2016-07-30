package com.example.lexicav1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class Settings extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle("Settings");
		getFragmentManager().beginTransaction().replace(R.id.content_frame, new MyPreferenceFragment()).commit();
    }
	
	public static class MyPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.prefs);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home)
		{
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	//public static methods for quick/easy access to modifying orderBy and first time tutorial options//
	final static String SHARED_PREF_KEY = "Lexica";
	final static String SP_KEY_LIST_OF_LEXICA_ORDER = "SP_KEY_LISTOFLEXICA_ORDER";
	public static final int ORDER_DATE_CREATED = 0, ORDER_ALPHABETICAL = 1, ORDER_RANDOM = 3;

	public static boolean getBooleanFromSP(Context context, String key) {
		SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, android.content.Context.MODE_PRIVATE);
		return preferences.getBoolean(key, false);
	}

	public static void saveBooleanInSP(Context context, String key, boolean value){
		SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public static int getOrderFromSP(Context context, String listName) {
		SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, android.content.Context.MODE_PRIVATE);
		return preferences.getInt("ORDER_" + listName, ORDER_DATE_CREATED);
	}

	public static void saveOrderInSP(Context context, String listName, int value){
		SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("ORDER_" + listName, value);
		editor.apply();
	}

	public static void clearOrderFromSP(Context context, String listName)
	{
		SharedPreferences preferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, android.content.Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("ORDER_" + listName);
		editor.apply();
	}
}
	
