package com.example.lexicav1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lexicav1.WordList.WordListBank;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

/**
 * An activity representing a list of Books. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
@SuppressLint("CutPasteId")
public class ItemListActivity extends AppCompatActivity implements
		ItemListFragment.Callbacks {
	public static Context context;
	public static boolean mTwoPane, isSlideShowRunning = false, activityResultFromSearching = false;
	public static String tableName;
	Button addWord;
	public static Button invisButtonForSearch;
	Dialog dialog;
	public static AddorModifyItemDialog addorModifyItemDialog;
	private ShowcaseView showcaseView;
	int showcaseCounter = 0;
	boolean showMORmenuItem = false;

	//Note that toolbarBottom does not exist in mTwoPane mode, so instead toolbarBottom = toolbar
	View.OnClickListener showcaseOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			switch(showcaseCounter)
			{
				case 0:
					showcaseView.setShowcase(new ToolbarActionItemTarget(toolbarBottom, R.id.SEARCH_item), true);
					showcaseView.setContentTitle("Search Feature");
					showcaseView.setContentText("This menu item allows you to search individual items from this current list. Click 'next' to continue.");
					showcaseView.show();
					break;
				case 1:
					showcaseView.setShowcase(new ToolbarActionItemTarget(toolbarBottom, R.id.OrderBy_item), true);
					showcaseView.setContentTitle("Re-order Feature");
					showcaseView.setContentText("This menu item starts the re-ordering feature for this current list (you can order by alphabetical, random, or date created). Click 'next' to continue.");
					showcaseView.show();
					break;
				case 2:
					showcaseView.setShowcase(new ToolbarActionItemTarget(toolbarBottom, R.id.SLIDESHOW_item), true);
					showcaseView.setContentTitle("Slideshow Feature");
					showcaseView.setContentText("This menu item starts the slideshow feature for this current list. The slideshow feature automatically goes through the list of items, xxx seconds per item. You can also make it loop through the items continuously. Click 'next' to continue.");
					showcaseView.show();
					break;
				case 3:
					showcaseView.setShowcase(new ToolbarActionItemTarget(toolbarBottom, R.id.KINESTHETIC_item), true);
					showcaseView.setContentTitle("Kinesthetic Feature");
					showcaseView.setContentText("This menu item starts the kinesthetic exercise/test for this current list. In this test, for each item, you must trace each letter of the item. Then, you must read/speak the traced item out loud into this device. Click 'next' to continue.");
					showcaseView.show();
					break;
				case 4:
					showcaseView.setShowcase(new ToolbarActionItemTarget(toolbarBottom, R.id.QUIZ_item), true);
					showcaseView.setContentTitle("Limited Exposure Feature");
					showcaseView.setContentText("This menu item starts the limited exposure exercise/test for this current list. It is essentially a multiple choice test. For each question in this multiple choice test, the game speaks an item out loud and you must correctly choose out of four possible options, the correct spelling of that spoken word. Click 'next' to continue.");
					showcaseView.setButtonText("Got it");
					showcaseView.show();
					break;
				case 5:
					if (mTwoPane && WordListBank.ITEMS.size() != 0)
					{
						ViewTarget target = new ViewTarget(R.id.expandable, ItemListActivity.this);
						showcaseView.setShowcase(target, true);
						showcaseView.setContentTitle("Expandable Hint");
						showcaseView.setContentText("If you click on this text, the hint of the item appears/disappears. The 'hint' is a word, or an optional image as well that you can upload.");
						showcaseView.setButtonText("Got it");
						showcaseView.show();
					}
					else
					{
						showcaseView.hide();
						showcaseView = null;
					}
					break;
				case 6:
					if (mTwoPane)
					{
						showcaseView.hide();
						showcaseView = null;
						if (showMORmenuItem)
						{
							showMORtutorialIfNotSeenBefore(ItemListActivity.this, toolbarBottom);
						}
					}
					break;
			}
			showcaseCounter++;
		}
	};

	public static TextToSpeech tts;
	public static boolean isTTSReady;
	TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
		@Override
		public void onInit(int status) {

			if (status == TextToSpeech.SUCCESS) {
				int result = tts.setLanguage(Locale.US);
				Log.w("", "DOES IT BECOME TRUE");
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					Log.e("TTS", "This Language is not supported");
				}
				isTTSReady = true;
			} else {
				Log.e("TTS", "Initilization Failed!");
			}
		}
	};

	Database db;
	double timePerQuestion = 0.5;
	Handler handler;
	Runnable runnable;
	Toolbar toolbar, toolbarBottom;
	LinearLayout llListFragment, llshowHideListFragment;
	TextView tvShowHideListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_list);
		Splash.tableOrWord = "word";
		db = new Database(ItemListActivity.this);
		db.open();
		addorModifyItemDialog = new AddorModifyItemDialog(ItemListActivity.this, db, null);
		context = this;

		invisButtonForSearch = (Button) findViewById(R.id.invisbutton);
		invisButtonForSearch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				String search = SearchableActivity.data;
				SearchableActivity.data = "";
				
				if (mTwoPane) {
					ItemListActivity.this.onItemSelected(search);
					WordListBank.WordItem mItem = WordListBank.ITEM_MAP.get(search);
					int indexOfItem = WordListBank.ITEMS.indexOf(mItem);
					((ItemListFragment) getSupportFragmentManager()
							.findFragmentById(R.id.book_list))
							.getListView().
							setItemChecked(indexOfItem, true);
				} else {
					activityResultFromSearching = false;
					Intent detailIntent = new Intent(ItemListActivity.this,
							ItemDetailActivity.class);
					detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID,
							search);
					detailIntent.putExtra("slideshow", false);
					startActivity(detailIntent);
				}
			}});

		addWord = (Button) findViewById(R.id.bAddWord);
		addWord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ItemListActivity.addorModifyItemDialog = new AddorModifyItemDialog(ItemListActivity.this, db, null);
				addorModifyItemDialog.startDialog(new AddorModifyItemDialog.OnItemSuccessfullyAddedListener()
				{
					@Override
					public void OnItemSuccessfullyAdded(String word, String meaning, boolean isPassageChecked)
					{
						if (mTwoPane)
						{
							//if there used to be zero items, but now we added one,
							// set itemdetailfragment container to be visible
							if (WordListBank.ITEMS.size() == 1)
							{
								LinearLayout layoutInsideScrollViewInsideItemDetailFragmentContainer =
										(LinearLayout) ItemListActivity.this.findViewById(R.id.layoutInsideScrollView);
								layoutInsideScrollViewInsideItemDetailFragmentContainer.setVisibility(View.VISIBLE);
							}

							//programmatically display newly added word in itemdetailfragment if in two-pane mode.
							ListView itemListFragmentListviewRef = ((ItemListFragment) ItemListActivity.this.getSupportFragmentManager()
									.findFragmentById(R.id.book_list))
									.getListView();
							WordListBank.WordItem mItem = WordListBank.ITEM_MAP.get(word);
							int position = WordListBank.ITEMS.indexOf(mItem);
							itemListFragmentListviewRef
									.performItemClick(itemListFragmentListviewRef.getAdapter().getView(position, null, null),
											position,
											itemListFragmentListviewRef.getAdapter().getItemId(position));
						}

						Toast.makeText(context,
								"Lexicon Updated", Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		});

		toolbar = (Toolbar) findViewById(R.id.toolbar);

		if (findViewById(R.id.book_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.book_list)).setActivateOnItemClick(true);

			//since this is on a tablet, we do not need the bottom toolbar. Configure the top one only.
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(true);

			//since in twoPaneMode, toolbarBottom does not exist, for sake of simplicity in code,
			//we write toolbarBottom = toolbar
			toolbarBottom = toolbar;

			setUpShowHideListFragment();
		}
		else //not two-pane mode
		{
			//separate toolbarBottom only exists in non-two-pane mode
			toolbarBottom = (Toolbar) findViewById(R.id.toolbarBottom);

			//Note that in the xml code of this activity, toolbar has a "home" navigation button
			//and we are handling the onClick event of it here
			toolbar.setNavigationOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					NavUtils.navigateUpTo(
							ItemListActivity.this,
							new Intent(ItemListActivity.this, ListLexica.class));
				}
			});

			//make toolbarBottom the actual toolbar to handle all the menu item events
			setSupportActionBar(toolbarBottom);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	//Display showcaseview tutorial for this activity here. Need to be called in onWindowFocusChanged
	//or else ActionMenuItemView hasn't loaded yet
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		boolean displayedTutorialOnceBefore = Settings.getBooleanFromSP(this, "ITEMLISTACTIVITYKEY");
		if (!displayedTutorialOnceBefore)
		{
			Settings.saveBooleanInSP(this, "ITEMLISTACTIVITYKEY", true);
			ActionMenuItemView actionMenuView = (ActionMenuItemView) findViewById(R.id.TUTORIAL_item);
			int gravity;
			if (mTwoPane)
				gravity = Gravity.BOTTOM;
			else
				gravity = Gravity.TOP;
			SimpleTooltip simpleToolTip = new SimpleTooltip.Builder(this)
					.anchorView(actionMenuView)
					.text("First time being on this screen? Touch this menu item to begin the tutorial for this screen.")
					.gravity(gravity)
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

	private void setUpShowHideListFragment()
	{
		llListFragment = (LinearLayout) findViewById(R.id.llListFragment);
		tvShowHideListFragment = (TextView) findViewById(R.id.tvShowHideListFragment);
		//Get component components paint and text
		String text = (String) tvShowHideListFragment.getText();
		Paint paint = tvShowHideListFragment.getPaint();
		//Calculate width of the string given the paint
		int width = (int) paint.measureText(text);
		//Set the minimum width of the component to the width of text contained
		tvShowHideListFragment.setMinimumHeight(width);

		llshowHideListFragment = (LinearLayout) findViewById(R.id.llshowHideListFragment);
		llshowHideListFragment.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Animation a;
				LinearLayout.LayoutParams listFragmentParams = (LinearLayout.LayoutParams) llListFragment.getLayoutParams();
				if (listFragmentParams.weight == 0f) //if the list fragment isn't visible, make it visible
				{
					a = new ExpandAnimation(0, 1);

					tvShowHideListFragment.setText("Hide");
				}
				else //if the list fragment is visible, make it invisible
				{
					a = new ExpandAnimation(1, 0);

					tvShowHideListFragment.setText("Show");
				}

				a.setDuration(200);
				llListFragment.startAnimation(a);
			}
		});
	}

	// This class is the animation for the expand/collapse of the list fragment view in twoPane mode.
	private class ExpandAnimation extends Animation
	{

		private final float mStartWeight;
		private final float mDeltaWeight;

		public ExpandAnimation(float startWeight, float endWeight) {
			mStartWeight = startWeight;
			mDeltaWeight = endWeight - startWeight;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) llListFragment.getLayoutParams();
			lp.weight = (mStartWeight + (mDeltaWeight * interpolatedTime));
			llListFragment.setLayoutParams(lp);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		startItemDetailFragmentOrActivity(id, null, null);
	}

	private void startItemDetailFragmentOrActivity(String id, Double timePerSlideshowItem, Boolean slideshowLoops)
	{
		if (mTwoPane) {
			beginItemDetailFragmentTransaction(id, timePerSlideshowItem, slideshowLoops);
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);

			if (timePerSlideshowItem != null)
				detailIntent.putExtra("time", timePerSlideshowItem);
			if (slideshowLoops != null)
				detailIntent.putExtra("repeat",slideshowLoops);

			startActivityForResult(detailIntent, 1);
		}
	}

	private void beginItemDetailFragmentTransaction(String id, Double timePerSlideshowItem, Boolean slideshowLoops)
	{
		// In two-pane mode, show the detail view in this activity by
		// adding or replacing the detail fragment using a
		// fragment transaction.
		Bundle arguments = new Bundle();
		arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);

		if (timePerSlideshowItem != null)
			arguments.putDouble("time", timePerSlideshowItem);
		if (slideshowLoops != null)
			arguments.putBoolean("repeat", slideshowLoops);

		ItemDetailFragment fragment = new ItemDetailFragment();
		fragment.setArguments(arguments);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.book_detail_container, fragment).commit();

		//Anytime the item is changed if it is twoPane, and if the
		//item isPassage, MORmenuItem turns visible.
		WordListBank.WordItem tempItemRef = WordListBank.ITEM_MAP.get(id);
		if (tempItemRef.isPassage == 1)
		{
			showMORmenuItem = true;
			invalidateOptionsMenu();
		}
		else
		{
			showMORmenuItem = false;
			invalidateOptionsMenu();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		db.close();
		if (mTwoPane && tts != null)
		{
			tts.stop();
			tts.shutdown();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Splash.tableOrWord = "word";
		Bundle extras = getIntent().getExtras();
		if (getIntent().hasExtra("TABLE_NAME")) {
			tableName = extras.getString("TABLE_NAME");
		}
		toolbar.setTitle(tableName);
		db.open();
		addorModifyItemDialog.setDatabase(db);
		//since this is on tablet, then we need text to speech on for the itemdetailfragment.
		if (mTwoPane)
		{
			isTTSReady = false;
			tts = new TextToSpeech(this, onInitListener);
		}

		if (activityResultFromSearching)
		{
			ItemListActivity.invisButtonForSearch.callOnClick();
		}

		if (mTwoPane)
		{
			//from user preferences, we set the expandable layout in itemdetailfragment to be visible or gone
			SharedPreferences getPrefs =
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			boolean expandDescByDefault = Boolean.parseBoolean(getPrefs.getString("ExpandDescByDefault", "true"));
			((LinearLayout) findViewById(R.id.layoutToExpandOrHide)).setVisibility(expandDescByDefault ? View.VISIBLE : View.GONE);
			ItemDetailFragment.showOrHideExpandableView = expandDescByDefault ? "Hide " : "Show";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mif = getMenuInflater();
		mif.inflate(R.menu.list_of_items_menu, menu);
		if (!mTwoPane)
		{
			toolbarBottom.setTitle("");
		}
		return true;
	}

	public static void showMORtutorialIfNotSeenBefore(Activity activityContext, Toolbar toolbar)
	{
		Settings.saveBooleanInSP(activityContext, "MOR_KEY", true);
		ShowcaseView showcaseView = new ShowcaseView.Builder(activityContext)
				.setTarget(new ToolbarActionItemTarget(toolbar, R.id.MOR_item))
				.setContentTitle("MOR Exercise/Test")
				.setContentText("This button only appears for passages/paragraph items." +
						" This button allows you to take a MOR test. MOR is Multiple Oral" +
						" Re-reading and tests your ability to read on a paragraph level. This" +
						" feature helps calculate your words per minute. If you don't know a" +
						" word in the passage you are reading, you can click on the word and that" +
						" word will be spoken out loud to you.")
				.blockAllTouches()
				.setStyle(R.style.showcaseTheme)
				.withMaterialShowcase()
				.build();
		showcaseView.setButtonText("Got it");
		showcaseView.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (mTwoPane)
		{
			MenuItem MOR_item = menu.findItem(R.id.MOR_item);
			if (showMORmenuItem)
			{
				MOR_item.setVisible(true);
				boolean displayedTutorialOnceBefore = Settings.getBooleanFromSP(this, "ITEMLISTACTIVITYKEY");
				if (displayedTutorialOnceBefore && !Settings.getBooleanFromSP(this, "MOR_KEY"))
				{
					showMORtutorialIfNotSeenBefore(this, toolbarBottom);
				}
			}
			else
			{
				MOR_item.setVisible(false);
			}

			MenuItem slideshow_item = menu.findItem(R.id.SLIDESHOW_item);
			if (isSlideShowRunning)
			{
				slideshow_item.setIcon(R.drawable.ic_action_stop);
			}
			else
			{
				slideshow_item.setIcon(R.drawable.ic_action_slideshow);
			}
		}
		else
		{
			MenuItem MOR_item = menu.findItem(R.id.MOR_item);
			MOR_item.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case android.R.id.home:
			NavUtils.navigateUpTo(this,
					new Intent(this, ListLexica.class));
			break;
		case R.id.SEARCH_item:
			this.onSearchRequested();
			break;
			case R.id.OrderBy_item:
			orderItems();
			break;
		case R.id.SLIDESHOW_item:
			startOrStopSlideShow();
			break;
		case R.id.KINESTHETIC_item:
			startKinestheticGame();
			break;
		case R.id.QUIZ_item:
			startLimitedExposureGame();
			break;
		case R.id.MOR_item:
			startMORexercise();
			break;
		case R.id.TUTORIAL_item:
			showcaseCounter = 0;
			showTutorial();
			break;
		case R.id.IMPORT_item:
			//Get available xls or xlsx files on system.
			Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
			fileIntent.setType("gagt/sdf");
			try {
				startActivityForResult(fileIntent, 2);
			} catch (ActivityNotFoundException e) {
				Log.e("Lexica","No activity can handle picking a file. Showing alternatives.");
				Toast.makeText(this, "Error: You need to install a file-chooser app to import " +
						"Excel files into Lexica.", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.SETTINGS_item:
			Intent openActionBarActivity = new Intent("android.intent.action.prefs");
			startActivity(openActionBarActivity);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showTutorial()
	{
		showcaseView = new ShowcaseView.Builder(ItemListActivity.this)
				.setTarget(Target.NONE)
				.setContentTitle("Item List")
				.setContentText("This page displays the list of items and the exercises you can do with them. Click the 'next' button to continue.")
				.blockAllTouches()
				.setOnClickListener(showcaseOnClickListener)
				.setStyle(R.style.showcaseTheme)
				.withMaterialShowcase()
				.build();
		showcaseView.setButtonText("Next");
		RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		int margin = ((Number) (getResources().getDisplayMetrics().density * 16)).intValue();
		int marginBottom = mTwoPane ?  0 : toolbarBottom.getHeight();
		lps.setMargins(margin, margin, margin, margin + marginBottom);
		showcaseView.setButtonPosition(lps);
		showcaseView.show();
	}

	private void startKinestheticGame()
	{
		Intent intent = new Intent("android.intent.action.kinesthetic");
		ArrayList<String> a = new ArrayList<String>();
		db.open();
		a.addAll(WordListBank.getItemListAsString());
		intent.putStringArrayListExtra("words", a);
		intent.putExtra("title", tableName);
		intent.putExtra("tableName", tableName);
		startActivity(intent);
	}

	private void startLimitedExposureGame()
	{
		final Intent intent = new Intent("android.intent.action.quiz");
		ArrayList<String> a = new ArrayList<String>();
		db.open();
		a.addAll(WordListBank.getItemListAsString());
		intent.putStringArrayListExtra("words", a);
		intent.putExtra("title", tableName);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			dialog = new Dialog(ItemListActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
		} else {
			dialog = new Dialog(ItemListActivity.this);
		}
		timePerQuestion = 0.5;
		dialog.setTitle("Limited Exposure: Choose Time");
		dialog.setContentView(R.layout.set_time_dialog);
		Button OKButton = (Button) dialog.findViewById(R.id.okButton);
		SeekBar bar = (SeekBar) dialog.findViewById(R.id.seekBar);
		final TextView value = (TextView) dialog.findViewById(R.id.tvBarValue);
		dialog.show();

		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				timePerQuestion = 0.5*progress+0.5;
				value.setText(timePerQuestion +" sec.");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		OKButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				intent.putExtra("timePerQuestion", timePerQuestion);
				intent.putExtra("tableName", tableName);
				dialog.dismiss();
				startActivity(intent);
			}});
	}

	private void startMORexercise()
	{
		Intent openMORExercise = new Intent("android.intent.action.mor");
		startActivity(openMORExercise);
	}

	//Function that orders the items in various orders
	private void orderItems()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			dialog = new Dialog(ItemListActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
		} else {
			dialog = new Dialog(ItemListActivity.this);
		}
		dialog.setTitle("Re-order List");
		dialog.setContentView(R.layout.order_by_dialog);
		Button okButton = (Button) dialog.findViewById(R.id.bConfirmOrderByDialog);
		final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.rgOrderBy);

		switch (Settings.getOrderFromSP(this, ItemListActivity.tableName))
		{
			case Settings.ORDER_DATE_CREATED:
				radioGroup.check(R.id.rbDateCreated);
				break;
			case Settings.ORDER_ALPHABETICAL:
				radioGroup.check(R.id.rbAlphabetical);
				break;
			case Settings.ORDER_RANDOM:
				radioGroup.check(R.id.rbRandom);
				break;
		}

		dialog.show();

		okButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int selectedId = radioGroup.getCheckedRadioButtonId();
				db.open();
				WordListBank.ITEMS.removeAll(WordListBank.ITEMS);
				if(selectedId == R.id.rbAlphabetical)
				{
					Settings.saveOrderInSP(ItemListActivity.this, ItemListActivity.tableName, Settings.ORDER_ALPHABETICAL);
					ItemListFragment.aa.notifyDataSetChanged();
					Toast.makeText(getApplicationContext(), "List of Words Alphabetically Sorted", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
				else if(selectedId == R.id.rbRandom)
				{
					Settings.saveOrderInSP(ItemListActivity.this, ItemListActivity.tableName, Settings.ORDER_RANDOM);
					ItemListFragment.aa.notifyDataSetChanged();
					Toast.makeText(getApplicationContext(), "List of Words Shuffled", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					Log.w("ItemListActivity","" + WordListBank.ITEMS);
				}
				else if (selectedId == R.id.rbDateCreated)
				{
					Settings.saveOrderInSP(ItemListActivity.this, ItemListActivity.tableName, Settings.ORDER_DATE_CREATED);
					Toast.makeText(getApplicationContext(), "List of Words Sorted by Date", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
				else if (selectedId == -1) //nothing was checked
				{
					Toast.makeText(getApplicationContext(), "Nothing selected.", Toast.LENGTH_SHORT).show();
				}
				int order = Settings.getOrderFromSP(ItemListActivity.this, ItemListActivity.tableName);
				WordListBank.setContext(db.getDataItems(ItemListActivity.tableName, order));
			}});
	}

	//Function that starts or stops the slideShow
	public void startOrStopSlideShow()
	{
		if (ItemListFragment.aa.isEmpty())
		{
			Toast.makeText(this, "Lexicon is empty", Toast.LENGTH_SHORT).show();
			return;
		}

		final ItemDetailFragment itemDetailFragment =
				(ItemDetailFragment) getSupportFragmentManager().findFragmentById(R.id.book_detail_container);
		if (!isSlideShowRunning)
		{
			final SlideshowFeatureDialog slideshowFeatureDialog = new SlideshowFeatureDialog(ItemListActivity.this);
			View.OnClickListener onUserConfirmSlideshowListener = new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					slideshowFeatureDialog.dismissDialog();
					boolean loop = slideshowFeatureDialog.userClickedSlideshowShouldLoop();
					timePerQuestion = slideshowFeatureDialog.getExposureDurationPerWord();
					isSlideShowRunning = true;
					invalidateOptionsMenu(); //change the slideshow icon to a pause button in twopane mode.

					runnable = new Runnable(){
						@Override
						public void run() {
							if (!isSlideShowRunning)
							{
								if (mTwoPane)
								{
									((ItemListFragment) getSupportFragmentManager()
											.findFragmentById(R.id.book_list))
											.getListView()
											.setClickable(true);
									isSlideShowRunning = false;
									invalidateOptionsMenu();
									handler.removeCallbacks(this);
								}
							}
							else
							{
								handler.postDelayed(this, 100);
							}
						}

					};

					handler = new Handler();
					handler.post(runnable);

					startItemDetailFragmentOrActivity(
							ItemListFragment.aa.getItem(0).toString(),
							timePerQuestion,
							loop);
				}
			};
			slideshowFeatureDialog.startSlideshowFeature(onUserConfirmSlideshowListener);
		}
		else
		{
			isSlideShowRunning = false;
			invalidateOptionsMenu();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2)  //imported excel file
		{
			if (data == null) {
				return;
			}
			System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
			System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
			System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
			String filePath = data.getData().getPath();
			int numImportErrors = 0;
			try {
				if (resultCode == RESULT_OK) {
					//TODO: figure out exactly why there is a NoClassDefFoundError for ThemeDocuments$Factory On API < 21
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						InputStream inStream;
						Workbook wb;
						try {
							inStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
							if (inStream == null) {
								Toast.makeText(this, "File corrupted", Toast.LENGTH_SHORT).show();
								return;
							}
							wb = WorkbookFactory.create(inStream);
							//wb = Workbook(inStream);
							inStream.close();
						} catch (Exception e) {
							Log.e("ItemListAct", "error", e);
							Toast.makeText(this, "I/O stream Error.", Toast.LENGTH_SHORT).show();
							return;
						}

						Sheet sheet1 = wb.getSheetAt(0);
						if (sheet1 == null) {
							Toast.makeText(this, "Excel file does not have any sheets inside.", Toast.LENGTH_SHORT).show();
							return;
						}

						db.open();
						numImportErrors = ExcelReader.insertExcelToSqlite(db, sheet1);
						ItemListFragment.aa.notifyDataSetChanged();
						wb.close();
					} else {
						InputStream inStream;
						XSSFWorkbook wb;
						try {
							inStream = getApplicationContext().getContentResolver().openInputStream(data.getData());
							if (inStream == null) {
								Toast.makeText(this, "File corrupted", Toast.LENGTH_SHORT).show();
								return;
							}
							wb = new XSSFWorkbook(inStream);
							inStream.close();
						} catch (Exception e) {
							Log.e("ItemListAct", "error", e);
							Toast.makeText(this, "I/O stream Error.", Toast.LENGTH_SHORT).show();
							return;
						}
						Sheet sheet1 = wb.getSheetAt(0);
						if (sheet1 == null) {
							Toast.makeText(this, "Excel file does not have any sheets inside.", Toast.LENGTH_SHORT).show();
							return;
						}
						db.open();
						numImportErrors = ExcelReader.insertExcelToSqlite(db, sheet1);
						ItemListFragment.aa.notifyDataSetChanged();
						wb.close();
					}
				}
			} catch (Exception e) {
				Log.e("ItemListAct", e.getMessage());
				Toast.makeText(this, "Import Error.", Toast.LENGTH_SHORT).show();
				return;
			}
			if (numImportErrors > 0) {
				Toast.makeText(this, numImportErrors + " # items failed to import. " +
						"(cannot have duplicate words, & file must be formatted properly.)", Toast.LENGTH_SHORT).show();
			} else
			{
				Toast.makeText(this, "Import successful.", Toast.LENGTH_SHORT).show();
			}
		}
		//addOrModifyItemDialog onActivityResult
		else if (addorModifyItemDialog != null) //need this null check because sometimes gets called
		{
			addorModifyItemDialog.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		NavUtils.navigateUpTo(this, new Intent(this, ListLexica.class));
	}
	
	@Override
    public boolean onSearchRequested() {
        Bundle appDataBundle = new Bundle();
        appDataBundle.putString("tableOrWords", "words");
        startSearch("", false, appDataBundle, false);
        return true;
	}

	//called in the ItemDetailFragment.
	public static void speakOut(Context activity, String stringToSpeak)
	{
		if (!isTTSReady)
		{
			Toast.makeText(activity, "Text-to-Speech Loading...", Toast.LENGTH_SHORT).show();
		}
		tts.speak(stringToSpeak, TextToSpeech.QUEUE_FLUSH, null);
	}
}
