package com.example.lexicav1;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public class Splash extends AppCompatActivity implements OnClickListener {

	public static String tableOrWord;
	static int musicCount = 0; //the splash screen sound should only be playing
	//when application launches. after, it should not play it.
	MediaPlayer splashSound;
	Button LofLexica;
	Intent openActivity, openActionBarActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);

		splashSound = MediaPlayer.create(Splash.this, R.raw.cute_kirby_button_sound);
		setTitle("Lexica");

		LofLexica = (Button) findViewById(R.id.bListofLexicaButton);
		LofLexica.setOnClickListener(this);

		if (musicCount == 0)
		{
			splashSound.start();
			musicCount++;
		}

		// Start first-time guide if necessary
		boolean displayedTutorialOnceBefore = Settings.getBooleanFromSP(this, "SPLASHACTIVITYKEY");
		if (!displayedTutorialOnceBefore)
		{
			//First time install. Display the external storage run-time permission along with
			//the tool tip.
			//TODO: fully implement this once we change targetSdkVersion to >= 23.
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
				String[] perms = {"android.permission. WRITE_EXTERNAL_STORAGE"};
				int permsRequestCode = 200;
				requestPermissions(perms, permsRequestCode);
			}

			Settings.saveBooleanInSP(this, "SPLASHACTIVITYKEY", true);
			SimpleTooltip simpleToolTip = new SimpleTooltip.Builder(this)
					.anchorView(LofLexica)
					.text("First time user? Touch this button to go to the main part of this application.")
					.gravity(Gravity.TOP)
					.animated(true)
					.dismissOnOutsideTouch(true)
					.dismissOnInsideTouch(true)
					.transparentOverlay(true)
					.backgroundColor(getResources().getColor(R.color.blue_tooltip_background_color))
					.arrowColor(getResources().getColor(R.color.blue_tooltip_background_color))
					.build();
			simpleToolTip.show();
		}
	}

	//To read more go here: http://stackoverflow.com/a/33980679/3324013
	//TODO: fully implement this once we change targetSdkVersion to >= 23.
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch(requestCode){
			case 200:
				//boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
				break;

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		splashSound.release();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.bListofLexicaButton:
				openActivity = new Intent("android.intent.action.listlexica");
				startActivity(openActivity);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mif = getMenuInflater();
		mif.inflate(R.menu.splash_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.FAQ_item:
			openActionBarActivity = new Intent("android.intent.action.faq");
			startActivity(openActionBarActivity);
			break;
//		case R.id.PROFILE_item:
//			openActionBarActivity = new Intent("android.intent.action.profile");
//			startActivity(openActionBarActivity);
//			break;
		case R.id.SETTINGS_item:
			openActionBarActivity = new Intent("android.intent.action.prefs");
			startActivity(openActionBarActivity);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
}
