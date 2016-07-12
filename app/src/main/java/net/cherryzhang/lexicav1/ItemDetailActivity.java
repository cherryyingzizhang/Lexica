package net.cherryzhang.lexicav1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import net.cherryzhang.lexicav1.WordList.WordListBank;

import java.util.Locale;

/**
 * An activity representing a single Book detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link ItemListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ItemDetailFragment}.
 */

public class ItemDetailActivity extends AppCompatActivity
{
	public static TextToSpeech tts;
	public static boolean isTTSReady;
	private Toolbar toolbar;
	private ShowcaseView showcaseView;

	private OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
		@Override
		public void onInit(int status) {

			if (status == TextToSpeech.SUCCESS) {

				int result = tts.setLanguage(Locale.US);
				isTTSReady = true;
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					Log.e("TTS", "This Language is not supported");
				}

			} else {
				Log.e("TTS", "Initilization Failed!");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_detail);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.

			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
			arguments.putDouble("time", getIntent().getDoubleExtra("time", 3));
			arguments.putBoolean("repeat",
					getIntent().getBooleanExtra("repeat", false));

			ItemDetailFragment.mItem = WordListBank.ITEM_MAP.get(getIntent()
					.getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
			setTitle(ItemDetailFragment.mItem.item.substring(0, 1).toUpperCase(
					Locale.ENGLISH)
					+ ItemDetailFragment.mItem.item.substring(1));

			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.book_detail_container_activity, fragment).commit();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		boolean displayedTutorialOnceBefore = Settings.getBooleanFromSP(this, "ItemDetailActivity_KEY");
		if (!displayedTutorialOnceBefore)
		{
			Settings.saveBooleanInSP(ItemDetailActivity.this, "ItemDetailActivity_KEY", true);
			showcaseView = new ShowcaseView.Builder(this)
					.setTarget(new ViewTarget(R.id.expandable, this))
					.setContentTitle("Expandable Hint")
					.setContentText("If you click on this text, the hint of the item appears/disappears.  The 'hint' is a word, or an optional image as well that you can upload.")
					.blockAllTouches()
					.setStyle(R.style.showcaseTheme)
					.withMaterialShowcase()
					.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							showcaseView.hide();
							showcaseView = null;
							if (ItemDetailFragment.mItem.isPassage == 1)
							{
								ItemListActivity.showMORtutorialIfNotSeenBefore(ItemDetailActivity.this, toolbar);
							}
						}
					})
					.build();
			showcaseView.setButtonText("Got it");
			showcaseView.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mif = getMenuInflater();
		mif.inflate(R.menu.item_detail_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem MOR_item = menu.findItem(R.id.MOR_item);
		if (ItemDetailFragment.mItem.isPassage == 1)
		{
			MOR_item.setVisible(true);
			boolean displayedTutorialOnceBefore = Settings.getBooleanFromSP(this, "ItemDetailActivity_KEY");
			if (displayedTutorialOnceBefore && !Settings.getBooleanFromSP(this, "MOR_KEY"))
			{
				ItemListActivity.showMORtutorialIfNotSeenBefore(this, toolbar);
			}
		}
		else
		{
			MOR_item.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this,
					new Intent(this, ItemListActivity.class));
			break;
		case R.id.MOR_item:
			Intent openMORExercise = new Intent("android.intent.action.mor");
			startActivity(openMORExercise);
			break;
		case R.id.SETTINGS_item:
			Intent openActionBarActivity = new Intent(
					"android.intent.action.prefs");
			startActivity(openActionBarActivity);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		tts = new TextToSpeech(this, onInitListener);
		//from user preferences, we set the expandable layout in itemdetailfragment to be visible or gone
		SharedPreferences getPrefs =
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean expandDescByDefault = Boolean.parseBoolean(getPrefs.getString("ExpandDescByDefault", "true"));
		((LinearLayout) findViewById(R.id.layoutToExpandOrHide)).setVisibility(expandDescByDefault ? View.VISIBLE : View.GONE);
		ItemDetailFragment.showOrHideExpandableView = expandDescByDefault ? "Hide " : "Show";
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		ItemListActivity.isSlideShowRunning = false;
		if (tts != null)
		{
			tts.stop();
			tts.shutdown();
		}
	}

	//called in the ItemDetailFragment
	public static void speakOut(Context context, String stringToSpeak) {
		if (!isTTSReady) {
			Toast.makeText(context,
					"Text-to-speech still loading... (wait for a few sec.)",
					Toast.LENGTH_SHORT).show();
		}
		tts.speak(stringToSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}
}
