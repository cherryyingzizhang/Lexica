package net.cherryzhang.lexicav1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MORActivity extends AppCompatActivity implements View.OnClickListener
{
    //using the special leapfrog feature, we log the specific words that needed to be clicked
    //in order for the user to understand that word he or she was unable to read
    ArrayList<String> wordsClickedDueInabilityToRead;
    TextToSpeech tts;
    Button startTest, stopTest, finishMORtest;
    TextToSpeechTextView passage;
    TextView MORtestResult;
    LinearLayout MORResultLayout;
    ScrollView MORtestlayout;
    ProgressBar progressCircle;
    int seconds, minutes; //String.format("%d:%02d", minutes, seconds)
    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            seconds = (int) (millis / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mor);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("MOR Test");

        wordsClickedDueInabilityToRead = new ArrayList<>();

        //Bind UI elements
        progressCircle = (ProgressBar) findViewById(R.id.progressCircle);
        startTest = (Button) findViewById(R.id.bStartTest);
        stopTest = (Button) findViewById(R.id.bStopTest);
        passage = (TextToSpeechTextView) findViewById(R.id.tvMORText);
        MORtestlayout = (ScrollView) findViewById(R.id.svMORTest);
        MORtestResult = (TextView) findViewById(R.id.tvMORResult);
        MORResultLayout = (LinearLayout) findViewById(R.id.llMORResult);
        finishMORtest = (Button) findViewById(R.id.bFinishMORtest);

        //handlerForSlideshow for the buttons
        startTest.setOnClickListener(this);
        stopTest.setOnClickListener(this);
        finishMORtest.setOnClickListener(this);

        //When the user first goes to this activity, he should not be allowed
        //to immediately click the "stopTest" button without first clicking "startTest"
        stopTest.setVisibility(View.GONE);

        //displays the passage'displayedTimeOnTimer text in this activity.
        passage.setText(ItemDetailFragment.mItem.description);
        SharedPreferences getPrefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int textSize = Integer.parseInt(getPrefs.getString("TextSize", "30"));
        passage.setTextSize(textSize-textSize/2);
        passage.setVisibility(View.GONE);
        passage.setOnWordClickedListener(new TextToSpeechTextView.OnWordClickedListener()
        {
            @Override
            public void onWordClicked(String word)
            {
                /*TODO: currently 'word' also includes punctuation. Remove any punctuation in word
                before adding it to wordsClickedDueInabilityToRead, unless it is something like
                "speaker's" maybe?*/
                wordsClickedDueInabilityToRead.add(word);
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.bStartTest:
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
                startTest.setEnabled(false);
                startTest.getBackground().setColorFilter(Color.GRAY,
                        PorterDuff.Mode.MULTIPLY);
                passage.setVisibility(View.VISIBLE);
                stopTest.setVisibility(View.VISIBLE);
                break;
            case R.id.bStopTest:
                timerHandler.removeCallbacks(timerRunnable);
                MORtestlayout.setVisibility(View.GONE);
                MORResultLayout.setVisibility(View.VISIBLE);
                double numWords = getNumWords(passage.getText().toString());
                double wpm = numWords/(minutes/1.0+seconds/60.0);
                DecimalFormat df;
                if (wpm >= 10)
                {
                    df = new DecimalFormat("#");
                }
                else
                {
                    df = new DecimalFormat("#.0");
                }
                String result = String.format(Locale.US, "%d:%02d", minutes, seconds) + "\nor\n" +
                        df.format(wpm) + " wpm";
                MORtestResult.setText(result);
                stopTest.setEnabled(false);
                logMORSession(wpm, String.format(Locale.US, "%d:%02d", minutes, seconds));
                break;
            case R.id.bFinishMORtest:
                NavUtils.navigateUpTo(MORActivity.this,
                        new Intent(MORActivity.this, ItemListActivity.class));
                break;
        }
    }

    private int getNumWords(String s)
    {
        String trim = s.trim();
        if (trim.isEmpty())
            return 0;
        return trim.split("\\s+").length; // separate string around spaces
    }

    private void logMORSession(double wpm, String totalTime)
    {
        Telemetry.editTelemetryLog(
                "---------------\n" +
                        "MOR:\n" +
                        "Table: " +  ItemListActivity.tableName + "\n" +
                        "Title of Passage: " +  ItemDetailFragment.mItem.item + "\n" +
                        "Body: " + ItemDetailFragment.mItem.description + "\n" +
                        "Total Time: " + totalTime + "\n" +
                        "WPM: " + wpm + "\n" +
                        "wordsClickedDueInabilityToRead: " + wordsClickedDueInabilityToRead);
    }

    @Override
    protected void onResume()
    {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS) {

                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    else
                    {
                        //start MOR
                        MORtestlayout.setVisibility(View.VISIBLE);
                        progressCircle.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        tts.stop();
        tts.shutdown();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
