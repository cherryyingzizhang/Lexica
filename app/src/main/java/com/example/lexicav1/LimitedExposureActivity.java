package com.example.lexicav1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class LimitedExposureActivity extends AppCompatActivity implements OnClickListener, OnInitListener {

	LinearLayout layoutQuiz, layoutResults;
	TableLayout buttonLayout;
	Button b1, b2, b3, b4, next;
	Button[] buttons;
	TextView questionNum, correctness;
	LinearLayout Ltext;
	ArrayList<String> words = new ArrayList<String>(), buttonWords = new ArrayList<String>(),
			incorrectWords = new ArrayList<String>(), 
			individualResponseTime = new ArrayList<String>();
	String currWord;
	String tableName;
	double timePerQuestion, temppoints;
	int c = -1, numCorrectWords = 0, points;
	double numOfTime = 0, sumOfTime = 0;
	ProgressBar pbar, BS;
	HashMap<String, String> params = new HashMap<String, String>();
	TextToSpeech tts;
	CountDownTimer timer;
	boolean isTimerRunning, beginningOfQuiz;
	TextView score, averageAnswerTime, Points;
	Button finish;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quiz);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		beginningOfQuiz = true;

		Intent intent = getIntent();
		setTitle("L.E.T.: " + intent.getStringExtra("title") + " Lexicon");
		words = intent.getStringArrayListExtra("words");
		currWord = words.get(0);
		timePerQuestion = intent.getDoubleExtra("timePerQuestion", 1.0);
		tableName = intent.getStringExtra("tableName");
		points = 0;
		temppoints = timePerQuestion*1000;
		pbar = (ProgressBar) findViewById(R.id.pBar);
		BS = (ProgressBar) findViewById(R.id.bsBar);
		BS.setMax(3000);
		pbar.setIndeterminate(false);
		Log.w("",""+timePerQuestion);
		Log.w("pbar.setMax", "" + (int)(timePerQuestion*1000));
		pbar.setMax((int)(timePerQuestion*1000));
		pbar.setVisibility(View.INVISIBLE);

		b1 = (Button) findViewById(R.id.b1);
		b2 = (Button) findViewById(R.id.b2);
		b3 = (Button) findViewById(R.id.b3);
		b4 = (Button) findViewById(R.id.b4);
		b1.setVisibility(View.GONE);
		b2.setVisibility(View.GONE);
		b3.setVisibility(View.GONE);
		b4.setVisibility(View.GONE);

		layoutQuiz = (LinearLayout) findViewById(R.id.llLETquiz);
		buttonLayout = (TableLayout) findViewById(R.id.tlButtons);
		buttonLayout.setVisibility(View.GONE);

		layoutResults = (LinearLayout) findViewById(R.id.llLETresults);
		score = (TextView) findViewById(R.id.tvScore);
		averageAnswerTime = (TextView) findViewById(R.id.tvAverageAnswerTime);
		Points = (TextView) findViewById(R.id.tvPoints);
		finish = (Button) findViewById(R.id.bGoToListOfWords);

		Ltext = (LinearLayout) findViewById(R.id.textLayout);
		questionNum = (TextView) findViewById(R.id.tvQuestionNumber);
		correctness = (TextView) findViewById(R.id.tvCorInc);
		next = (Button) findViewById(R.id.bNextWord);
		questionNum.setText("Loading...");
		next.setVisibility(View.INVISIBLE);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		b3.setOnClickListener(this);
		b4.setOnClickListener(this);
		next.setOnClickListener(this);
		next.setText("Start");
		
		buttons = new Button[]{b1, b2, b3, b4};

		SharedPreferences getPrefs =
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		int textSize = Integer.parseInt(getPrefs.getString("TextSize", "30"));
		for (int i = 0; i < buttons.length; i++) {
			FontHelper.changeFont(this, buttons[i]);
			buttons[i].setTextSize(textSize);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		tts = new TextToSpeech(this, this);
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId())
		{
		case R.id.b1:
			showAnswer(b1);
			if (c+1 == words.size())
			{
				next.setText("Finish");
			}
			break;
		case R.id.b2:
			showAnswer(b2);
			if (c+1 == words.size())
			{
				next.setText("Finish");
			}
			break;
		case R.id.b3:
			showAnswer(b3);
			if (c+1 == words.size())
			{
				next.setText("Finish");
			}
			break;
		case R.id.b4:
			showAnswer(b4);
			if (c+1 == words.size())
			{
				next.setText("Finish");
			}
			break;
		case R.id.bNextWord:
			if (next.getText().toString().contentEquals("Start"))
			{
				correctness.setVisibility(View.VISIBLE);
				buttonLayout.setVisibility(View.VISIBLE);
				b1.setEnabled(false);
				b2.setEnabled(false);
				b3.setEnabled(false);
				b4.setEnabled(false);
				next.setText("Next");
				next.setVisibility(View.VISIBLE);
				pbar.setVisibility(View.VISIBLE);
				b1.setVisibility(View.VISIBLE);
				//questionNum.setText("Question 1");
				questionNum.setVisibility(View.GONE);
				Ltext.setVisibility(View.GONE);
				b2.setVisibility(View.VISIBLE);
				b3.setVisibility(View.VISIBLE);
				b4.setVisibility(View.VISIBLE);
			}
			nextQuestion();
			break;
		}
	}

	//TODO: add a variable and log it, that keeps track of whether or not the user clicked
	//a wrong button (log the specific button/word) or if the user just didn't click anything at all
	//while the timer finished
	private void showAnswer(Button b) {
		next.setVisibility(View.VISIBLE);
		int temp = numCorrectWords;
		int correctOne = -1;
		for (int i = 0; i < 4; i++)
		{
			if (buttons[i].getText().toString().contentEquals(currWord))
			{
				buttons[i].setEnabled(false);
				buttons[i].getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
				if (b.equals(buttons[i]))
				{
					numCorrectWords++;
					points += temppoints;
					correctness.setText("Correct! Points: " + temppoints + "\nTotal Points: "
							+ points);
					
				}
				correctOne = i;
			}
		}
		if (temp == numCorrectWords)
		{
			for (int i = 0; i < 4; i++)
			{
				if (i != correctOne)
				{
					buttons[i].getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
					correctness.setText("Incorrect. Points: " + points);
					
				}
			}
			incorrectWords.add(currWord);
		}
		individualResponseTime.add(String.valueOf(
				timePerQuestion-(pbar.getProgress()/1000.0)));
		sumOfTime += timePerQuestion-(pbar.getProgress()/1000.0);
		temppoints = timePerQuestion*1000;//reset for next question
		timer.cancel();
		b1.setEnabled(false);
		b2.setEnabled(false);
		b3.setEnabled(false);
		b4.setEnabled(false);
		isTimerRunning = false;
	}

	private void nextQuestion() {
		correctness.setText("");
		if (c+1 == words.size()) //end quiz
		{
			layoutQuiz.setVisibility(View.GONE);
			layoutResults.setVisibility(View.VISIBLE);

			score.setText("Score: " + numCorrectWords + "/" + words.size());
			Points.setText("Points: " + points);
			DecimalFormat df = new DecimalFormat("0.000");
			averageAnswerTime.setText("Average Answer Time: " + df.format(sumOfTime/numOfTime) + " sec.");

			finish.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					NavUtils.navigateUpTo(LimitedExposureActivity.this,
							new Intent(LimitedExposureActivity.this, ItemListActivity.class));
				}});

			logLimitedExposureSession();
		}
		else //actual next question function
		{
			numOfTime++;
			currWord = words.get(++c);
			int temp = c+1; //weird formatting unless I add this
			setTitle("Question " + temp);
			params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,""+c);
			tts.speak(currWord,TextToSpeech.QUEUE_FLUSH, params);
			pbar.setProgress(pbar.getMax());
			
			//questionNum.setText("Question " + temp);
			buttonWords = (Functions.scrambleAndShuffle(currWord));
			next.setVisibility(View.INVISIBLE);
			for (int i = 0; i < 4; i++)
			{
				buttons[i].setText(buttonWords.get(i));
				buttons[i].getBackground().clearColorFilter();
			}
		}

		timer = new CountDownTimer((long)(timePerQuestion*1000), 25) {

		     public void onTick(long millisUntilFinished) {
		    	 pbar.setProgress((int) millisUntilFinished);
		    	 temppoints -=25;
		    	 isTimerRunning = true;
		    	 buttons[0].setEnabled(true);
				 buttons[1].setEnabled(true);
				 buttons[2].setEnabled(true);
				 buttons[3].setEnabled(true);
		     }

		     public void onFinish() {
				 runOnUiThread(new Runnable()
				 {
					 @Override
					 public void run()
					 {
						 showAnswer(next); //no answer correct
						 next.setEnabled(true);
					 }
				 });
		     }
		  };
	}

	private void logLimitedExposureSession()
	{
		DecimalFormat df = new DecimalFormat("0.000");
		Telemetry.editTelemetryLog(
				"---------------\n" +
						"Limited Exposure:\n" +
						"Table: " +  ItemListActivity.tableName + "\n" +
						"List of Words: " +  words + "\n" +
						"Incorrect Words: " + incorrectWords + "\n" +
						"ResponseTimeForEachWord: " + individualResponseTime + "\n" +
						"Score: " + numCorrectWords + "/" + words.size() + "\n" +
						"AverageAnswerTime: " + df.format(sumOfTime/numOfTime) + " sec.");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (isTimerRunning)
		{
			timer.cancel();
		}
		tts.stop();
		tts.shutdown();
		finish();
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
	        int result = tts.setLanguage(Locale.US);
	        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	            Log.e("TTS", "This Language is not supported");
	        }
			else
			{
				this.tts.setOnUtteranceProgressListener(new UtteranceProgressListener()
					{
						@Override
						public void onStart(String utteranceId)
						{
							if (BS.getVisibility() == View.VISIBLE)
							{
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										startQuiz();
									}
								});
							}
						}

						@Override
						public void onDone(String utteranceId)
						{
							if (!utteranceId.equals("beginning"))
							{
								timer.start();
							}
						}

						@Override
						public void onError(String utteranceId)
						{

						}
					});

				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "beginning");
				tts.speak("This limited exposure exercise is a multiple choice quiz. A letter or word " +
								"will be spoken out loud and you must touch the corresponding correct button.",
						TextToSpeech.QUEUE_FLUSH, params);
			}
	    } else {
	        Log.e("TTS", "Initilization Failed!");
	    }
	}

	private void startQuiz() {
		questionNum.setText("Press Start!");
		next.setVisibility(View.VISIBLE);
		BS.setVisibility(View.GONE);
		buttons[0].setEnabled(false);
		buttons[1].setEnabled(false);
		buttons[2].setEnabled(false);
		buttons[3].setEnabled(false);
	}

	//Make it so that the "home" button lets the user to go the previous activity
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				this.onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
