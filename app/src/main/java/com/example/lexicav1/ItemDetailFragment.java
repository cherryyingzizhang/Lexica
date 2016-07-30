package com.example.lexicav1;

import android.animation.LayoutTransition;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lexicav1.WordList.WordListBank;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

/**
 * A fragment representing a single Book detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */

public class ItemDetailFragment extends Fragment{
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	int dp200;
	Database db;
	TextView word, expandable;
	TextToSpeechTextView meaning;
	ImageButton texttoSpeechItem, texttoSpeechDesc;
	Button homework, slideshow, prev, next;
	LinearLayout meaningLayout, layoutInsideScrollView, layoutToChangeInPassageMode, layoutToExpandOrHide;
	ImageView image;
	public static final String ARG_ITEM_ID = "item_id";
	public int indexOfItem = 0;
	public static String passageOrHint = "Hint", showOrHideExpandableView = "Show ";

	/**
	 * The following variables and method are for the slideshow feature.
	 **/
	Handler handlerForSlideshow; //handler specifically for the slideshow
	int timePassedInSlideshow; //we use this to keep track of the number of milliseconds that passed in the slideshow
	boolean repeat; //whether or not the slideshow repeats/loops/never ends
	double exposureDuration; //variable that stores the exposure duration for each item in the slideshow.
	Runnable runnableSlideShow = new Runnable() {
		   @Override
		   public void run() {
			   if (ItemListActivity.isSlideShowRunning)
			   {
					if (WordListBank.ITEMS.size() == indexOfItem)
					{
						if (repeat)
						{
							indexOfItem = 0;
							handlerForSlideshow.postDelayed(this, 100);
						}
						else
						{
							ItemListActivity.isSlideShowRunning = false;
							Toast.makeText(getActivity(), "Slideshow finished!", Toast.LENGTH_SHORT).show();
							handlerForSlideshow.removeCallbacks(this);
							if (WordListBank.ITEMS.size() != 1)
							{
								next.setEnabled(true);
								prev.setEnabled(true);
								next.getBackground().clearColorFilter();
								prev.getBackground().clearColorFilter();
							}
							slideshow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_slideshow_black, 0, 0, 0);
							logSlideshowSession(false);
						}
					}
					else //Go to the next item in the slideshow.
					{
						if (timePassedInSlideshow % (1000*exposureDuration) == 0)
						{
							mItem = WordListBank.ITEMS.get(indexOfItem);
							word.setText(mItem.item);
							if (!ItemListActivity.mTwoPane)
							{
								getActivity().setTitle(mItem.item);
							}
							meaning.setText(mItem.description);
							loadImage();
							changeToPassageLayoutIfNecessary();
							if (ItemListActivity.mTwoPane)
							{
								((ItemListFragment) getActivity().getSupportFragmentManager()
										.findFragmentById(R.id.book_list))
										.getListView().
										setItemChecked(indexOfItem, true);
							}
							
							indexOfItem++;
						}
						timePassedInSlideshow += 100;
						handlerForSlideshow.postDelayed(this, 100);
					}
			   }
			   else
			   {
				   logSlideshowSession(true);
				   timePassedInSlideshow = 0;
				   Toast.makeText(getActivity(), "SlideShow Stopped.", Toast.LENGTH_SHORT).show();
				   slideshow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_slideshow_black, 0, 0, 0);
				   handlerForSlideshow.removeCallbacks(this);
				   next.setEnabled(true);
				   prev.setEnabled(true);
				   next.getBackground().clearColorFilter();
				   prev.getBackground().clearColorFilter();
			   }
			   
		   	}
	};

	private void logSlideshowSession(boolean userManuallyEndedSlideshow)
	{
		Log.w("ItemDetailFrag","" + WordListBank.ITEMS);
		Telemetry.editTelemetryLog(
			"---------------\n" +
			"SLIDESHOW:\n" +
			"Table: " +  ItemListActivity.tableName + "\n" +
			"List of Words: " + WordListBank.ITEMS + "\n" +
			"Time per Word (s): " + exposureDuration + "\n" +
			"Repeat: " + repeat + "\n" +
			"User Manually Ended Slideshow: " + userManuallyEndedSlideshow + "\n" +
			"Total Time (ms): " + timePassedInSlideshow);
	}
	/**
	 * End of section of variables/methods for slideshow feature.
	 **/

	/**
	 * The following variables are for the start/pause/reset timer in the detail fragment, and
	 * are not for the slideshow feature.
	 **/
	//we need to have these buttons as static since a new ItemDetailFragment is created each time
	//the user clicks on a new item in the list of items in mTwoPane.
	public static Button bTimer, bresettimer;
	static String displayedTimeOnTimer;
	public static int numTimesStartPauseTimerButtonPressed = 0;
	static long startTime = 0L;
	static long timeInMilliseconds = 0L;
	static long timeSwapBuff = 0L;
	static long updatedTime = 0L;
	static boolean isTimerRunning = false;
	static Handler timerHandler;
	static Runnable updateTimerThread = new Runnable() {

		public void run() {
			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

			updatedTime = timeSwapBuff + timeInMilliseconds;

			int secs = (int) (updatedTime / 1000);
			int mins = secs / 60;
			secs = secs % 60;
			int milliseconds = (int) (updatedTime % 1000);
			displayedTimeOnTimer = "" + mins + ":" + String.format(Locale.US,"%02d", secs);
//					+ ":" + String.format(Locale.US,"%02d", milliseconds)
			bTimer.setText(displayedTimeOnTimer);
			timerHandler.postDelayed(this, 0);
		}

	};
	/**
	 * End of the start/pause/reset timer variables declaration section
	 **/

	//This variable contains the item's string, the desc. string, and isPassage attribute of the item
	public static WordListBank.WordItem mItem;


	//Mandatory empty constructor for the fragment manager to instantiate the
	//fragment (e.g. upon screen orientation changes).
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dp200 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getActivity().getResources().getDisplayMetrics());
		db = new Database(getActivity());
		handlerForSlideshow = new Handler();
		timerHandler = new Handler();
		Splash.tableOrWord = "word";
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = WordListBank.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
			indexOfItem = WordListBank.ITEMS.indexOf(mItem);
		}
		if (getArguments().containsKey("time")) 
		{
			exposureDuration = getArguments().getDouble("time");
		}
		
		if (getArguments().containsKey("repeat"))
		{
			repeat = getArguments().getBoolean("repeat");
		}

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// to find the index of the current item
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutInsideScrollView = (LinearLayout)container.findViewById(R.id.layoutInsideScrollView);
		layoutInsideScrollView.setLayoutTransition(new LayoutTransition());
		word = (TextView) container.findViewById(R.id.tvWord);
		meaning = (TextToSpeechTextView) container.findViewById(R.id.tvMeaning);
		meaning.setOnWordClickedListener(new TextToSpeechTextView.OnWordClickedListener()
		{
			@Override
			public void onWordClicked(String word)
			{
				speakOut(word);
			}
		});
		image = (ImageView) container.findViewById(R.id.ivImage);
		expandable = (TextView) container.findViewById(R.id.expandable);
		layoutToExpandOrHide = (LinearLayout) container.findViewById(R.id.layoutToExpandOrHide);
		expandable.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (layoutToExpandOrHide.getVisibility() == View.GONE)
				{
					layoutToExpandOrHide.setVisibility(View.VISIBLE);
					showOrHideExpandableView = "Hide ";
				}
				else
				{
					layoutToExpandOrHide.setVisibility(View.GONE);
					showOrHideExpandableView = "Show";
				}
				expandable.setText(showOrHideExpandableView + " " + passageOrHint);
			}});

		layoutToChangeInPassageMode = (LinearLayout) container.findViewById(R.id.layoutToChangeInPassageMode);
		meaningLayout = (LinearLayout) container.findViewById(R.id.meaningLayout);
		texttoSpeechItem = (ImageButton) container.findViewById(R.id.bTextToSpeechItem);
		texttoSpeechDesc = (ImageButton) container.findViewById(R.id.bTextToSpeechDesc);
		homework = (Button) container.findViewById(R.id.bHomework);
		slideshow = (Button) container.findViewById(R.id.bSlideshow);
		bTimer = (Button) container.findViewById(R.id.timer);
		bresettimer = (Button) container.findViewById(R.id.stopTimer);
		next = (Button) container.findViewById(R.id.next);
		prev = (Button) container.findViewById(R.id.prev);

		changeItemImageAndDescriptionContent();
		FontHelper.changeFont(getActivity(), word);
		FontHelper.changeFont(getActivity(), meaning);
		changeToPassageLayoutIfNecessary();

		texttoSpeechItem.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				speakOut(mItem.item);
			}});

		texttoSpeechDesc.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				speakOut(mItem.description);
			}});

		//set homework button disabled if this lexicon is the Homework lexicon
		if (ItemListActivity.tableName.contentEquals("Homework")) {
			homework.setEnabled(false);
			homework.getBackground().setColorFilter(Color.GRAY,
					PorterDuff.Mode.MULTIPLY);
		}

		//set next and prev buttons disabled if this lexicon list has only one item in it.
		if (WordListBank.ITEMS.size() == 1)
		{
			next.setEnabled(false);
			next.getBackground().setColorFilter(Color.GRAY,
					PorterDuff.Mode.MULTIPLY);
			prev.setEnabled(false);
			prev.getBackground().setColorFilter(Color.GRAY,
					PorterDuff.Mode.MULTIPLY);
		}
		else
		{
			next.setEnabled(true);
			next.getBackground().clearColorFilter();
			prev.setEnabled(true);
			prev.getBackground().clearColorFilter();
		}

		homework.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				final Database db = new Database(getActivity());
				db.open();
				if (!db.itemExists("Homework", mItem.item))
				{
					final String fileName = UUID.randomUUID().toString() + ".jpg";
					if(mItem.imageFileName != null)
					{
						if (!mItem.imageFileName.contentEquals(""))
						{
							File imageFile = new ImageSaver(getActivity()).load(mItem.imageFileName);
							new ImageSaver(getActivity()).save(Uri.fromFile(imageFile), fileName, new ImageSaver.OnFileSavedListener()
							{
								@Override
								public void OnFileSaved()
								{
									db.createItem("Homework", mItem.item, mItem.description, mItem.isPassage, fileName);
									Toast.makeText(getActivity().getApplicationContext(), "'" + mItem.item + "' added to Homework",
											Toast.LENGTH_SHORT).show();
								}
							});
						}
						else
						{
							db.createItem("Homework", mItem.item, mItem.description, mItem.isPassage, "");
							Toast.makeText(getActivity().getApplicationContext(), "'" + mItem.item + "' added to Homework",
									Toast.LENGTH_SHORT).show();
						}
					}
					else
					{
						db.createItem("Homework", mItem.item, mItem.description, mItem.isPassage, null);
						Toast.makeText(getActivity().getApplicationContext(), "'" + mItem.item + "' added to Homework",
								Toast.LENGTH_SHORT).show();
					}
				}
				else
				{
					Toast.makeText(getActivity().getApplicationContext(), "'" + mItem.item + "' is already in the Homework lexicon.",
							Toast.LENGTH_SHORT).show();
				}
			}});

		slideshow.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (ItemListActivity.mTwoPane)
				{
					((ItemListActivity)getActivity()).startOrStopSlideShow();
				}
				else
				{
					if (!ItemListActivity.isSlideShowRunning)
					{
						final SlideshowFeatureDialog slideshowFeatureDialog = new SlideshowFeatureDialog(getActivity());
						View.OnClickListener onUserConfirmSlideshowListener = new OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								repeat = slideshowFeatureDialog.userClickedSlideshowShouldLoop();
								exposureDuration = slideshowFeatureDialog.getExposureDurationPerWord();
								slideshowFeatureDialog.dismissDialog();
								ItemListActivity.isSlideShowRunning = true;
								indexOfItem = 0;
								changeItemAndDescriptionDueToNextOrPrevButtonClick();
								startSlideshow();
							}
						};
						slideshowFeatureDialog.startSlideshowFeature(onUserConfirmSlideshowListener);
					}
					else
					{
						ItemListActivity.isSlideShowRunning = false;
						slideshow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_slideshow_black, 0, 0, 0);
					}
				}
			}
		});

		next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				indexOfItem++;
				if (WordListBank.ITEMS.size() == indexOfItem+1)
				{
					Toast.makeText(getActivity(), "End of Lexicon",
							Toast.LENGTH_SHORT).show();
				}
				else if (WordListBank.ITEMS.size() <= indexOfItem)
				{
					indexOfItem = 0;
				}
				changeItemAndDescriptionDueToNextOrPrevButtonClick();
			}});

		prev.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				indexOfItem--;
				if (0 == indexOfItem)
				{
					Toast.makeText(getActivity(), "Beginning of Lexicon",
							Toast.LENGTH_SHORT).show();
				}
				else if (-1 == indexOfItem)
				{
					indexOfItem = WordListBank.ITEMS.size() -1;
				}
				changeItemAndDescriptionDueToNextOrPrevButtonClick();
			}});

		if (!bTimer.getText().equals("Start/Pause") && numTimesStartPauseTimerButtonPressed % 2 != 0)
		{
			long temp = timeInMilliseconds;
			timerHandler.post(updateTimerThread);
			timeInMilliseconds = temp;
		}

		bTimer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startOrPauseTimer();
			}
		});

		bresettimer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				bTimer.setText(resetTimer());
			}
		});

		if (ItemListActivity.isSlideShowRunning)
		{
			startSlideshow();
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private void startSlideshow()
	{
		handlerForSlideshow.post(runnableSlideShow);
		timePassedInSlideshow = 0;
		prev.setEnabled(false);
		prev.getBackground().setColorFilter(Color.GRAY,
				PorterDuff.Mode.MULTIPLY);
		next.setEnabled(false);
		next.getBackground().setColorFilter(Color.GRAY,
				PorterDuff.Mode.MULTIPLY);
		//change the slideshow icon to a pause button.
		slideshow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop_black_24dp, 0, 0, 0);
	}

	public void changeItemImageAndDescriptionContent()
	{
		mItem = WordListBank.ITEMS.get(indexOfItem);
		loadImage();
		word.setText(mItem.item);
		meaning.setText(mItem.description);
		meaning.clearSelectedTextRect();
	}

	private void changeItemAndDescriptionDueToNextOrPrevButtonClick()
	{
		changeItemImageAndDescriptionContent();
		if (ItemListActivity.mTwoPane) //fragment launched from two-pane ItemListActivity
		{
			ListView itemListFragmentListviewRef = ((ItemListFragment) getActivity().getSupportFragmentManager()
					.findFragmentById(R.id.book_list))
					.getListView();
			itemListFragmentListviewRef
					.performItemClick(itemListFragmentListviewRef.getAdapter().getView(indexOfItem, null, null),
							indexOfItem,
							itemListFragmentListviewRef.getAdapter().getItemId(indexOfItem));
		}
		else //fragment launched from ItemDetailActivity
		{
			getActivity().setTitle(ItemDetailFragment.mItem.item.substring(0, 1)
					.toUpperCase(Locale.ENGLISH)
					+ ItemDetailFragment.mItem.item.substring(1));
			getActivity().invalidateOptionsMenu();
		}
		changeToPassageLayoutIfNecessary();
	}

	//Handle the in-fragment startOrPause timer button
	public static String startOrPauseTimer() {
		numTimesStartPauseTimerButtonPressed++;
		if (numTimesStartPauseTimerButtonPressed % 2 != 0) {
			if (!isTimerRunning) {
				timerHandler = new Handler();
			}
			startTime = SystemClock.uptimeMillis();
			timerHandler.postDelayed(updateTimerThread, 0);
			isTimerRunning = true;
		} else {
			timeSwapBuff += timeInMilliseconds;
			timerHandler.removeCallbacks(updateTimerThread);
		}
		return displayedTimeOnTimer;
	}

	//Handle the in-fragment reset timer button
	public static String resetTimer() {
		if (isTimerRunning) {
			timerHandler.removeCallbacks(updateTimerThread);
			isTimerRunning = false;
			numTimesStartPauseTimerButtonPressed = 0;
			startTime = 0L;
			timeInMilliseconds = 0L;
			timeSwapBuff = 0L;
			updatedTime = 0L;
		}
		return "Start/Pause";
	}

	private void loadImage()
	{
		if (mItem.imageFileName != null) {
			if (!mItem.imageFileName.contentEquals("")) {
				//See if it is a URL. If it is a local image, load the image via file.
				try {
					//this will throw exception if imageFileName is not valid URL
					URL url = new URL(mItem.imageFileName);
					Picasso.with(getActivity()).load(mItem.imageFileName).resize(dp200, dp200).centerInside().into(image, new Callback() {
						@Override
						public void onSuccess() { }

						@Override
						public void onError() {
							Log.w("ImageLoading Error", "ImageLoading URL Error");
						}
					});
					return;
				}
				catch (MalformedURLException e) { /* invalid URL */ }

				//If we got to this point, it means the image was not a url. It is local file.
				File bitmapFile = new ImageSaver(getActivity()).
						load(mItem.imageFileName);
				Log.w("bitmapFile null", "" + (bitmapFile == null));
				Picasso.with(getActivity()).load(bitmapFile).resize(dp200, dp200).centerInside().into(image, new Callback() {
					@Override
					public void onSuccess() { }

					@Override
					public void onError() {
						Log.w("ImageLoading Error", "ImageLoading Error");
					}
				});
			}
			else {
				image.setImageBitmap(null);
				image.invalidate();
			}
		}
		else {
			image.setImageBitmap(null);
			image.invalidate();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		FontHelper.changeFont(getActivity(), word);
		FontHelper.changeFont(getActivity(), meaning);
		changeToPassageLayoutIfNecessary();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onDetach() {
		if (handlerForSlideshow != null)
		{
			handlerForSlideshow.removeCallbacks(runnableSlideShow);
		}
		super.onDestroy();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timerHandler.removeCallbacks(updateTimerThread);
	}

	private void changeToPassageLayoutIfNecessary()
	{
		SharedPreferences getPrefs =
				PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		String textSize = getPrefs.getString("TextSize", "40");
		word.setTextSize(Integer.parseInt(textSize));

		if (mItem.isPassage == 1)
		{
			passageOrHint = "Passage";
			meaning.setGravity(Gravity.NO_GRAVITY);
			meaning.setTextSize(Integer.parseInt(textSize)-Integer.parseInt(textSize)/2);
			putTTSButtonAboveMeaningLayout();
		}
		else
		{
			passageOrHint = "Hint";
			meaning.setGravity(Gravity.CENTER);
			meaning.setTextSize(Integer.parseInt(textSize));
			putTTSButtonRightOfMeaningLayout();
		}
		expandable.setText(showOrHideExpandableView + " " + passageOrHint);
	}

	private void putTTSButtonAboveMeaningLayout()
	{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		params.setMargins(10,1,10,0);
		layoutToChangeInPassageMode.setLayoutParams(params);
		layoutToChangeInPassageMode.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams paramsMeaning = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		meaningLayout.setLayoutParams(paramsMeaning);

		((LinearLayout.LayoutParams) texttoSpeechDesc.getLayoutParams()).leftMargin = 0;
		((LinearLayout.LayoutParams) texttoSpeechDesc.getLayoutParams()).bottomMargin = 10;
		((LinearLayout.LayoutParams) texttoSpeechDesc.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

		layoutToChangeInPassageMode.removeAllViews();
		layoutToChangeInPassageMode.addView(texttoSpeechDesc);
		layoutToChangeInPassageMode.addView(meaningLayout);
	}

	private void putTTSButtonRightOfMeaningLayout()
	{
		LinearLayout tempWordLayoutRef = (LinearLayout) getActivity().findViewById(R.id.wordLayout);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		params.setMargins(
				((LinearLayout.LayoutParams) tempWordLayoutRef.getLayoutParams()).leftMargin,
				((LinearLayout.LayoutParams) tempWordLayoutRef.getLayoutParams()).topMargin,
				((LinearLayout.LayoutParams) tempWordLayoutRef.getLayoutParams()).rightMargin,
				((LinearLayout.LayoutParams) tempWordLayoutRef.getLayoutParams()).bottomMargin
		);
		layoutToChangeInPassageMode.setLayoutParams(params);
		layoutToChangeInPassageMode.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout.LayoutParams paramsMeaning = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, 1000f);
		meaningLayout.setLayoutParams(paramsMeaning);
		meaningLayout.setGravity(Gravity.CENTER_HORIZONTAL);

		((LinearLayout.LayoutParams) texttoSpeechDesc.getLayoutParams()).leftMargin = ((LinearLayout.LayoutParams) texttoSpeechItem.getLayoutParams()).leftMargin;
		((LinearLayout.LayoutParams) texttoSpeechDesc.getLayoutParams()).bottomMargin = 0;
		((LinearLayout.LayoutParams) texttoSpeechDesc.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;

		layoutToChangeInPassageMode.removeAllViews();
		layoutToChangeInPassageMode.addView(meaningLayout);
		layoutToChangeInPassageMode.addView(texttoSpeechDesc);
	}

	private void speakOut(String string) {
		if (ItemListActivity.mTwoPane) //fragment launched two-pane from ItemListActivity
		{
			ItemListActivity.speakOut(getActivity(), string);
		}
		else //fragment launched from ItemDetailActivity
		{
			ItemDetailActivity.speakOut(getActivity(), string);
		}
	}
}
