package com.example.lexicav1;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.myscript.atk.scw.SingleCharWidget;
import com.myscript.atk.scw.SingleCharWidgetApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class KinestheticActivity extends AppCompatActivity implements
        OnInitListener,
        SingleCharWidgetApi.OnConfiguredListener,
        SingleCharWidgetApi.OnTextChangedListener
{
    private static final String TAG = "SingleCharDemo";

    //This activity's toolbar
    Toolbar toolbar;

    //the character recognition widget
    private SingleCharWidgetApi widget;

    //words in the kinesthetic game
    ArrayList<String> words = new ArrayList<>();

    //list of integers. Each integer represents the number of mistakes the user makes
    //for a word in the list of words. The index of the "mistake" corresponds to the index of the
    //word in the list of words.
    ArrayList<Integer> tracingMistakes = new ArrayList<>();

    //Each index in this list represents which word in the word list the user spoke out loud
    //incorrectly in this test in the voice recognition part.
    ArrayList<Boolean> wordSpokenMistakes = new ArrayList<>();

    //TextView displaying the current word to trace and the score
    TextView currWord, score;
    //the two views: the first one is the one with the tracing widget, the second
    //is the result view that displays the score and average answer time
    LinearLayout kinestheticTestView;
    ScrollView kinestheticResultView;
    //Progress Circle
    ProgressBar kinestheticProgressCircle;

    //Text to speech object and its progress listener
    TextToSpeech tts;
    UtteranceProgressListener progressListener = new UtteranceProgressListener()
    {
        //We do this because the tts takes a while to load,
        //and we only want the user to start the kinesthetic game
        //after the tts is loaded.
        @Override
        public void onStart(String s)
        {
            if (!startingTest)
            {
                doNotWriteInTracingWidget = true;
                //changing the views must be run on UI thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //hide the progress bar
                        kinestheticProgressCircle.setVisibility(View.GONE);
                        kinestheticTestView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        //We use this code in onDone specifically for the voice recognition part of the app.
        //The user needs to be told via tts to speak into their device using voice recognition.
        //however, the voice recognition hears the tts speak. Thus, we can only start the voice
        //recognition after the tts is DONE speaking.
        @Override
        public void onDone(String s)
        {
            if (!startingTest)
            {
                startingTest = true;
                doNotWriteInTracingWidget = false;
            }

            if (waitForTextToSpeechToBeDone)
            {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        "Please read the whole word out loud into this device.");
                try {
                    //I use 100 to identify it is for voice recognition in onActivityResult
                    startActivityForResult(intent, 100);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Voice Recognition not supported.",
                            Toast.LENGTH_SHORT).show();
                }
                waitForTextToSpeechToBeDone = false;
            }
        }

        @Override
        public void onError(String s)
        {

        }
    };

    //Sound objects to make "correct" or "incorrect" sound each time the user traces
    MediaPlayer soundCorrect;
    MediaPlayer soundIncorrect;

    //boolean on whether or not a test is starting
    boolean startingTest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kinesthetic);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tts = new TextToSpeech(this, this);
        tts.setOnUtteranceProgressListener(progressListener);

        Intent intent = getIntent();
        setTitle("Kinesthetic: " + intent.getStringExtra("title") + " Lexicon");
        words = intent.getStringArrayListExtra("words");

        //binds xml objects to java objects
        currWord = (TextView) findViewById(R.id.tvKinestheticWordToHandwrite);
        score = (TextView) findViewById(R.id.tvKinestheticTestScore);
        kinestheticTestView = (LinearLayout) findViewById(R.id.llKinestheticTestView);
        kinestheticResultView = (ScrollView) findViewById(R.id.llKinestheticResultView);
        kinestheticProgressCircle = (ProgressBar) findViewById(R.id.kinestheticProgressCircle);
        setUpWidget();

        soundCorrect = MediaPlayer.create(KinestheticActivity.this, R.raw.correct_answer);
        soundIncorrect = MediaPlayer.create(KinestheticActivity.this, R.raw.wrong_answer);

        //initializing class-wide booleans to be false.
        startingTest = false;
        waitForTextToSpeechToBeDone = false;
        doNotWriteInTracingWidget = false;

        //initialize the mistake arraylist
        for (int i = 0; i < words.size(); i++)
        {
            tracingMistakes.add(0);
            wordSpokenMistakes.add(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

    }

    //Sets up the character recognition widget
    private void setUpWidget()
    {
        widget = (SingleCharWidget) findViewById(R.id.singleCharWidget);

        if (!widget.registerCertificate(MyCertificate.getBytes()))
        {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Please use a valid certificate.");
            dlgAlert.setTitle("Invalid certificate");
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    //dismiss the dialog
                }
            });
            dlgAlert.create().show();
            return;
        }
        widget.setOnConfiguredListener(this);
        widget.setOnTextChangedListener(this);
        widget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf");
        widget.configure("en_US", "si_text");
        LinearLayout.LayoutParams layoutParamsForWidget
                = new LinearLayout.LayoutParams(((FrameLayout)widget).getLayoutParams().height, ((FrameLayout)widget).getLayoutParams().height);
        ((FrameLayout)widget).setLayoutParams(layoutParamsForWidget);
    }

    private void playCorrectSound()
    {

        soundCorrect.start();
    }

    private void playIncorrectSound()
    {
        soundIncorrect.start();
    }

    int currIndex;  //which word in the lexicon we are at.
    int currWordCharIndex; //which index/character in the current word we are on

    private void startTest()
    {
        //we set this to negative one purely because we call updateTest, which increments these
        currIndex = 0;
        currWordCharIndex = 0;
        //set the current word, and use tts to tell user to start tracing.
        currWord.setText(words.get(currIndex));
        FontHelper.changeFont(this, currWord);
        SharedPreferences getPrefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int textSize = Integer.parseInt(getPrefs.getString("TextSize", "30"));
        currWord.setTextSize(textSize);
        speak("Please write letter by letter the following item in the grey box below.", TextToSpeech.QUEUE_FLUSH);
    }

    //We use this boolean specifically for promptSpeechInput because startActivityForResult
    //can only be done after the tts is finished prompting the user to use the voice recognition
    //or else the voice recognition will pick up the tts'displayedTimeOnTimer voice.
    boolean waitForTextToSpeechToBeDone;

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {

        //Using tts to say the prompt out loud since the user has difficulty reading.
        speak("Please read the whole item out loud.", TextToSpeech.QUEUE_FLUSH);
        waitForTextToSpeechToBeDone = true;
        //go to the UtteranceProgressListener object'displayedTimeOnTimer onDone method to see the rest of what
        //this method should do. (which is to start the intent for voice recognition)
    }

    /**
     * Receiving speech input from voice recognition intent
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {
                    //get the result word.
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    //say "correct" or "incorrect" if the spoken word matches the current word.
                    String speech;
                    //if the word is the same as the displayed word
                    if (words.get(currIndex-1).equalsIgnoreCase(result.get(0)))
                    {
                        playCorrectSound();
                        speech = "Correct!";
                    }
                    else //the word spoken out loud was not correct.
                    {
                        playIncorrectSound();
                        speech = "That was incorrect. The correct answer was "
                                        + words.get(currIndex-1) + ".";
                        wordSpokenMistakes.set(currIndex-1,true);
                    }
                    speak(speech, TextToSpeech.QUEUE_FLUSH);

                    //update to the next word in the lexicon list OR complete the test if done.
                    if (currIndex < words.size())
                    {
                        currWordCharIndex = 0;
                        //set the current word, and use tts to tell user to start tracing.
                        currWord.setText(words.get(currIndex));
                        speak("Onto the next item.", TextToSpeech.QUEUE_ADD);
                        //the user can proc the onTextChanged function now.
                        doNotWriteInTracingWidget = false;
                    }
                    else //test is completed.
                    {
                        //displaying the result view.
                        kinestheticTestView.setVisibility(View.GONE);
                        kinestheticResultView.setVisibility(View.VISIBLE);

                        updateScore();
                    }
                }
                // We want to prevent the user from cancelling the voice recognition pop-up
                // without restarting the voice recognition pop-up or else the exercise
                // cannot continue
                else if (resultCode == RESULT_CANCELED)
                {
                    promptSpeechInput();
                }
                break;
            }

        }
    }

    //wrapper for the tts.speak function
    private void speak(String speech, int QueueMode)
    {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(currIndex));
        tts.speak(speech, QueueMode,map);
    }

    // updates score at the very end of the kinesthetic test.
    private void updateScore()
    {
        String scoreText = "Tracing Mistakes:" + "\n";
        //getting the tracing mistake score for each word in the list:
        for (int i = 0; i < words.size(); i++)
        {
            scoreText += words.get(i) + ": " + tracingMistakes.get(i) + "\n";
        }
        scoreText += "Spoken Mistakes:" + "\n";
        //getting the spoken word mistakes
        for (int i = 0; i < words.size(); i++)
        {
            //if there was a  spoken mistake for the ith word
            if (wordSpokenMistakes.get(i))
            {
                //add it to the list of spoken word mistakes to be displayed
                scoreText += words.get(i) + "\n";
            }
        }
        score.setText(scoreText);
        logKinestheticSession();
    }

    private void logKinestheticSession()
    {
        Telemetry.editTelemetryLog(
            "---------------\n" +
            "KINESTHETIC:\n" +
            "Table: " +  ItemListActivity.tableName + "\n" +
            "List of Words: " + words + "\n" +
            "TracingMistakesForEachWord: " + tracingMistakes + "\n" +
            "SpokenMistakesForEachWord: " + wordSpokenMistakes);
    }

    //onInit function for the tts
    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                Toast.makeText(KinestheticActivity.this, "Game cannot be started. Text to speech unsupported.", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                startTest();
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
            Toast.makeText(KinestheticActivity.this, "Game cannot be started. Text to speech initialization failed.", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        widget.setOnTextChangedListener(null);
        widget.setOnConfiguredListener(null);
        tts.shutdown();
        soundCorrect.release();
        soundIncorrect.release();
        super.onDestroy();
    }

    //Method for Widget
    @Override
    public void onConfigured(SingleCharWidgetApi widget, boolean success)
    {
        if(!success)
        {
            Toast.makeText(getApplicationContext(), widget.getErrorString(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Unable to configure the Single Char Widget: " + widget.getErrorString());
            return;
        }
        if(BuildConfig.DEBUG)
            Log.d(TAG, "Single Char Widget configured!");
    }

    //bool that checks whether or not we should listen to onTextChanged or not
    //(we should not check the letter written into the handwriting widget if the user)
    //is supposed to be speaking into the device at the time.
    //the other time is when the test starts/a new word is displayed and the tts is still speaking.
    private boolean doNotWriteInTracingWidget;

    //Method for widget
    @Override
    public void onTextChanged(SingleCharWidgetApi widget, String s, boolean intermediate)
    {
        //this validates that the algorithm isn't still guessing at what character the user wrote
        if (!intermediate && !doNotWriteInTracingWidget)
        {
            String character = String.valueOf(s.charAt(s.length()-1)); //gets the character

            //copying the value for simplicity of reading the code below.
            String displayedWord = words.get(currIndex);

            //if the handwritten character equals to the current character of the current word
            if (character.equalsIgnoreCase(String.valueOf(displayedWord.charAt(currWordCharIndex))))
            {
                playCorrectSound();
                String text = "<font color=#186E1C>"
                        + displayedWord.substring(0,currWordCharIndex+1)
                        + "</font> <font color=#000000>"
                        + displayedWord.substring(currWordCharIndex+1,displayedWord.length())
                        + "</font>";
                currWord.setText(Html.fromHtml(text));
                currWordCharIndex++;

                if (currWordCharIndex == displayedWord.length())
                {
                    //prompt user to speak the completed traced word out loud, then go to next word.
                    currIndex++;
                    promptSpeechInput();
                    doNotWriteInTracingWidget = true;
                }
                //condition where the "item" might contain several words
                else if (String.valueOf(displayedWord.charAt(currWordCharIndex)).contentEquals(" "))
                {
                    currWordCharIndex++;
                }
            }
            else
            {
                playIncorrectSound();

                //increment number of tracingMistakes for this word
                tracingMistakes.set(currIndex, tracingMistakes.get(currIndex)+1);

                //TODO: Possibly make another list that represents # of tracingMistakes for each 26 letter?
            }
        }
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
