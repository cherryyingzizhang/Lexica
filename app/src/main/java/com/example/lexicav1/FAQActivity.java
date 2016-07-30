package com.example.lexicav1;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class FAQActivity extends AppCompatActivity implements TextToSpeech.OnInitListener
{
	ArrayList<TextView> tvFAQ;
	final int numTextViews = 13;
	ImageButton bTTSwhatIsThisApp, bTTSwhatDoesThisAppInclude;
	TextToSpeech tts;
	boolean isTTSReady = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faq_activity);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle("FAQ/Help");

		// condensed way of binding xml elements into Java code
		tvFAQ = new ArrayList<>();
		for (int i = 0; i < numTextViews; i++)
		{
			tvFAQ.add((TextView) findViewById(getResources().getIdentifier("tvFAQ" + (i+1), "id", getPackageName())));
		}

		bTTSwhatIsThisApp = (ImageButton) findViewById(R.id.bTTSwhatIsThisApp);
		bTTSwhatDoesThisAppInclude = (ImageButton) findViewById(R.id.bTTSwhatDoesThisAppInclude);

		bTTSwhatIsThisApp.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!isTTSReady)
				{
					Toast.makeText(FAQActivity.this, "TextToSpeech loading...", Toast.LENGTH_SHORT).show();
				}
				else
				{
					tts.speak(tvFAQ.get(0).getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
					tts.speak(tvFAQ.get(1).getText().toString(),TextToSpeech.QUEUE_ADD, null);
				}
			}
		});

		bTTSwhatDoesThisAppInclude.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (!isTTSReady)
				{
					Toast.makeText(FAQActivity.this, "TextToSpeech loading...", Toast.LENGTH_SHORT).show();
				}
				else
				{
					tts.speak(tvFAQ.get(2).getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
					for (int i = 3; i < 13; i++)
					{
						tts.speak(tvFAQ.get(i).getText().toString(),TextToSpeech.QUEUE_ADD, null);
					}
				}
			}
		});
	}

	@Override
	public void onInit(int status)
	{
		if (status == TextToSpeech.SUCCESS)
		{
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA|| result == TextToSpeech.LANG_NOT_SUPPORTED)
			{
				Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show();
			}
			else
			{
				isTTSReady = true;
			}
		}
		else
		{
			Toast.makeText(this, "Initilization Failed!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		isTTSReady = false;
		tts = new TextToSpeech(this, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		tts.stop();
		tts.shutdown();
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home)
		{
			NavUtils.navigateUpTo(this,
					new Intent(this, Splash.class));
		}
		return super.onOptionsItemSelected(item);
	}

}
