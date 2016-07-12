package net.cherryzhang.lexicav1;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import net.cherryzhang.customarrayadapter.CustomAdapter;

import java.util.ArrayList;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public class ListLexica extends AppCompatActivity
{
	ListView listview;
	Button createLexicon;
	Button OKButton;
	EditText lexiconName;
	TextView warning;
	public static CustomAdapter adapter;
	public static ArrayList<String> items;
	Dialog createLexiconDialog;
	Database db;
	ShowcaseView showcaseView;
	Toolbar toolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listlexica);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		Splash.tableOrWord = "table";
		setTitle("Lexica List");
		db = new Database(ListLexica.this);
		db.open();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			

			listview = (ListView) findViewById(android.R.id.list);
			
			listview.setOnItemLongClickListener(new OnItemLongClickListener(){

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					Object item = arg0.getItemAtPosition(position);
					final String oldLexicon = item.toString();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						createLexiconDialog = new Dialog(ListLexica.this, android.R.style.Theme_Material_Light_Dialog_Alert);
					} else {
						createLexiconDialog = new Dialog(ListLexica.this);
					}
					createLexiconDialog.setTitle("Edit " + oldLexicon);
					if (oldLexicon.matches("Homework"))
					{
						Toast.makeText(getApplicationContext(), 
								"Don't edit homework!", 
								   Toast.LENGTH_SHORT).show();
						return true;
					}
					createLexiconDialog.setContentView(R.layout.addlexicondialog);
					createLexiconDialog.show();
					OKButton = (Button) createLexiconDialog.findViewById(R.id.OKBUTTON);
					OKButton.setText("Edit");
					lexiconName = (EditText) createLexiconDialog.findViewById(R.id.addLexiconText);
					warning = (TextView) createLexiconDialog.findViewById(R.id.tvWarning);

					OKButton.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							
							boolean INeedThisBoolToChecklowercasetablenameVSUpperCaseTableName = false;
							//bad code when there'displayedTimeOnTimer lots of tables :D
							for (int i = 0; i < items.size(); i++)
							{
								if (items.get(i).equalsIgnoreCase(lexiconName.getText().toString()))
								{
									INeedThisBoolToChecklowercasetablenameVSUpperCaseTableName = true;
								}
							}
							
							if (lexiconName.getText().toString().matches("")
									|| Functions.isMadeOfSpaces(lexiconName.getText().toString())
									|| db.lexiconExists(lexiconName.getText().toString())
									|| INeedThisBoolToChecklowercasetablenameVSUpperCaseTableName)
							{
								createLexiconDialog.dismiss();
								Toast.makeText(getApplicationContext(), 
										"Invalid lexicon name", 
										   Toast.LENGTH_SHORT).show();
							}
							else
							{
								
								//add lexicon and update listview and update SP.
								db.changeLexiconName(oldLexicon, lexiconName.getText().toString());
								int oldOrder = Settings.getOrderFromSP(ListLexica.this, oldLexicon);
								Settings.saveOrderInSP(ListLexica.this, lexiconName.getText().toString(), oldOrder);
								Settings.clearOrderFromSP(ListLexica.this, oldLexicon);

								createLexiconDialog.dismiss();

								items = db.getAllLexica(Settings.getOrderFromSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER));
								adapter.clear();
								adapter.addAll(items);
								adapter.notifyDataSetChanged();
								
								Toast.makeText(getApplicationContext(), 
										"Lexicon Edited.", 
										   Toast.LENGTH_SHORT).show();
							}
						}
						
					});
					return true;
				}});
			
			createLexicon = (Button) findViewById(R.id.bAddLexicon);
			createLexicon.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						createLexiconDialog = new Dialog(ListLexica.this, android.R.style.Theme_Material_Light_Dialog_Alert);
					} else {
						createLexiconDialog = new Dialog(ListLexica.this);
					}
					createLexiconDialog.setTitle("Create New Lexicon");
					createLexiconDialog.setContentView(R.layout.addlexicondialog);
					createLexiconDialog.show();
					OKButton = (Button) createLexiconDialog.findViewById(R.id.OKBUTTON);
					lexiconName = (EditText) createLexiconDialog.findViewById(R.id.addLexiconText);
					warning = (TextView) createLexiconDialog.findViewById(R.id.tvWarning);
					OKButton.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							boolean INeedThisBoolToChecklowercasetablenameVSUpperCaseTableName = false;
							//bad code when there'displayedTimeOnTimer lots of tables :D
							for (int i = 0; i < items.size(); i++)
							{
								if (items.get(i).equalsIgnoreCase(lexiconName.getText().toString()))
								{
									INeedThisBoolToChecklowercasetablenameVSUpperCaseTableName = true;
								}
							}
							
							if (lexiconName.getText().toString().matches("")
									|| Functions.isMadeOfSpaces(lexiconName.getText().toString())
									|| db.lexiconExists(lexiconName.getText().toString())
									|| INeedThisBoolToChecklowercasetablenameVSUpperCaseTableName)
							{
								createLexiconDialog.dismiss();
								Toast.makeText(getApplicationContext(), 
										"Invalid lexicon name", 
										   Toast.LENGTH_SHORT).show();
							}
							else
							{
								
								//add lexicon and update listview
								db.createLexicon(lexiconName.getText().toString());
								items = db.getAllLexica(Settings.getOrderFromSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER));
								createLexiconDialog.dismiss();
								
								adapter.add(lexiconName.getText().toString());
								adapter.notifyDataSetChanged();
								
								Toast.makeText(getApplicationContext(), 
										lexiconName.getText().toString() + " added to list of lexica.", 
										   Toast.LENGTH_SHORT).show();
							}
						}
						
					});
				}
				
			});
			
			@SuppressWarnings({ "deprecation", "unchecked" })
			ArrayList<String> listRef = (ArrayList<String>) getLastNonConfigurationInstance();

			if (listRef == null)
			{
				items = db.getAllLexica(Settings.getOrderFromSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER));
			}
			else
			{
				items = listRef;
			}

			adapter = new CustomAdapter(this,
					android.R.layout.simple_list_item_1, items);
			listview.setAdapter(adapter);

			listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					Splash.tableOrWord = "word";
					Object item = arg0.getItemAtPosition(position);
					String myitem = item.toString();
					Intent intent = new Intent(getApplicationContext(),
							ItemListActivity.class);
					intent.putExtra("TABLE_NAME", myitem);
					startActivity(intent);
				}
			});
		}
	}

	//
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//Here we can get the size of the listview, etc. for our tutorial if
		//we wanted a first-time tutorial when the user first gets to this activity.
		boolean displayedTutorialOnceBefore = Settings.getBooleanFromSP(this, "LISTLEXICAKEY");
		if (!displayedTutorialOnceBefore)
		{
			Settings.saveBooleanInSP(this, "LISTLEXICAKEY", true);

			ActionMenuItemView actionMenuView = (ActionMenuItemView) findViewById(R.id.TUTORIAL_item);
			SimpleTooltip simpleToolTip = new SimpleTooltip.Builder(this)
					.anchorView(actionMenuView)
					.text("First time user? Touch this menu item to begin the tutorial for this screen.")
					.gravity(Gravity.BOTTOM)
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

	//Currently called in a menu item in OnOptionsItemSelected and now in onWindowFocusChanged.
	private void startShowCaseViewIfNecessary()
	{
		showcaseView = new ShowcaseView.Builder(this)
				.setTarget(Target.NONE)
				.setContentTitle("List of Lexica")
				.setContentText("This page is where we display the list of lexica. Lexica contain 'items'. " +
						"'Items' can be letters, words, or passages. Click 'next' to continue.")
				.blockAllTouches()
				.setOnClickListener(showcaseOnClickListener)
				.withMaterialShowcase()
				.setStyle(R.style.showcaseTheme)
				.build();
		showcaseView.setButtonText("Next");
	}

	//counter for which showcaseview to display.
	private int showcaseCounter = 0;
	SimpleTooltip simpleToolTip;

	//TODO: have this activity implement OnDismissListener so that we don't have annoying
	//pyramid like code for simpletooltip
	private View.OnClickListener showcaseOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			switch (showcaseCounter) {
				case 0:
					showcaseView.setShowcase(new ViewTarget(R.id.bAddLexicon, ListLexica.this), true);
					showcaseView.setContentTitle("Adding New List of Items/Lexicon List");
					showcaseView.setContentText("This button is to add a new lexicon. Click 'next' to continue");
					break;
				case 1:
					showcaseView.hide();
					simpleToolTip = new SimpleTooltip.Builder(ListLexica.this)
							.anchorView(listview)
							.text("We already have three premade lexica for you: " +
									"a list of the alphabet letters, a list of 3-letter words," +
									" and simple passages. Click on this tooltip to continue.")
							.gravity(Gravity.BOTTOM)
							.animated(true)
							.transparentOverlay(false)
							.dismissOnOutsideTouch(false)
							.dismissOnInsideTouch(true)
							.backgroundColor(getResources().getColor(R.color.blue_tooltip_background_color))
							.arrowColor(getResources().getColor(R.color.blue_tooltip_background_color))
							.modal(true)
							.onDismissListener(new SimpleTooltip.OnDismissListener()
							{
								@Override
								public void onDismiss(SimpleTooltip tooltip)
								{
									simpleToolTip.dismiss();
									simpleToolTip = new SimpleTooltip.Builder(ListLexica.this)
											.anchorView(listview)
											.text("Click the 'x' to delete a lexicon. Long press the item to modify " +
													"the lexicon's name. Click on this tooltip to continue.")
											.gravity(Gravity.BOTTOM)
											.animated(true)
											.dismissOnOutsideTouch(false)
											.dismissOnInsideTouch(true)
											.backgroundColor(getResources().getColor(R.color.blue_tooltip_background_color))
											.arrowColor(getResources().getColor(R.color.blue_tooltip_background_color))
											.modal(true)
											.transparentOverlay(false)
											.onDismissListener(new SimpleTooltip.OnDismissListener()
											{
												@Override
												public void onDismiss(SimpleTooltip tooltip)
												{
													simpleToolTip.dismiss();
													simpleToolTip = new SimpleTooltip.Builder(ListLexica.this)
															.anchorView(listview)
															.text("Note that the homework lexicon is a special un-removable lexicon." +
																	" You can put items from other lexica into the homework lexicon.")
															.gravity(Gravity.BOTTOM)
															.animated(true)
															.backgroundColor(getResources().getColor(R.color.blue_tooltip_background_color))
															.arrowColor(getResources().getColor(R.color.blue_tooltip_background_color))
															.dismissOnOutsideTouch(true)
															.dismissOnInsideTouch(true)
															.transparentOverlay(true)
															.onDismissListener(new SimpleTooltip.OnDismissListener()
															{
																@Override
																public void onDismiss(SimpleTooltip tooltip)
																{
																	showcaseView = new ShowcaseView.Builder(ListLexica.this)
																			.setTarget(new ToolbarActionItemTarget(toolbar, R.id.SEARCH_item))
																			.setContentTitle("Lexicon Search Feature")
																			.setContentText("Clicking on this menu item allows you to search for lexica by their name. Note that it does not search for items in each lexicon but only lexica. Click next to continue.")
																			.blockAllTouches()
																			.setOnClickListener(showcaseOnClickListener)
																			.withMaterialShowcase()
																			.setStyle(R.style.showcaseTheme)
																			.build();
																	showcaseView.setButtonText("Next");
																}
															})
															.build();
													simpleToolTip.show();
												}
											})
											.build();
									simpleToolTip.show();
								}
							})
							.build();
					simpleToolTip.show();
					break;
				case 2:
					showcaseView.setShowcase(new ToolbarActionItemTarget(toolbar, R.id.OrderBy_item), true);
					showcaseView.setContentTitle("Lexicon Re-order Feature");
					showcaseView.setContentText("Clicking on this menu item allows you to re-order the list of lexica shown in this screen. You can re-order them by alphabetical, random, or date-created.");
					showcaseView.setButtonText("Finish");
					showcaseView.show();
					break;
				case 3:
					showcaseView.hide();
					break;
			}
			showcaseCounter++;
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		db.close();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		db.open();
		Splash.tableOrWord = "table";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mif = getMenuInflater();
		mif.inflate(R.menu.listlexica_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this,
					new Intent(this, Splash.class));
			break;
		case R.id.SEARCH_item:
			onSearchRequested();
			break;
		case R.id.OrderBy_item:
			orderItems();
			break;
		case R.id.TUTORIAL_item:
			showcaseCounter = 0;
			startShowCaseViewIfNecessary();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void orderItems()
	{
		final Dialog dialog;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			dialog = new Dialog(ListLexica.this, android.R.style.Theme_Material_Light_Dialog_Alert);
		} else {
			dialog = new Dialog(ListLexica.this);
		}
		dialog.setTitle("Re-order List of Lexica");
		dialog.setContentView(R.layout.order_by_dialog);
		Button okButton = (Button) dialog.findViewById(R.id.bConfirmOrderByDialog);
		final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.rgOrderBy);

		switch (Settings.getOrderFromSP(this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER))
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
				items.removeAll(items);
				if(selectedId == R.id.rbAlphabetical)
				{
					Settings.saveOrderInSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER, Settings.ORDER_ALPHABETICAL);
					Toast.makeText(getApplicationContext(), "List of Lexica Alphabetically Sorted", Toast.LENGTH_SHORT)
							.show();
				}
				else if(selectedId == R.id.rbRandom)
				{
					Settings.saveOrderInSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER, Settings.ORDER_RANDOM);
					Toast.makeText(getApplicationContext(), "List of Lexica Shuffled", Toast.LENGTH_SHORT)
							.show();
				}
				else if (selectedId == R.id.rbDateCreated)
				{
					Settings.saveOrderInSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER, Settings.ORDER_DATE_CREATED);
					Toast.makeText(getApplicationContext(), "List of Lexica Sorted by Date", Toast.LENGTH_SHORT)
							.show();
				}
				else if (selectedId == -1) //nothing was checked
				{
					Toast.makeText(getApplicationContext(), "Nothing selected.", Toast.LENGTH_SHORT).show();
				}
				items.addAll(db.getAllLexica(Settings.getOrderFromSP(ListLexica.this, Settings.SP_KEY_LIST_OF_LEXICA_ORDER)));
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}});
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<String> onRetainCustomNonConfigurationInstance()
	{
		final ArrayList<String> list = (ArrayList<String>) items.clone();
	    return list;
	}

	@Override
    public boolean onSearchRequested() {
        Bundle appDataBundle = new Bundle();
        appDataBundle.putString("tableOrWords", "table");
        startSearch("", false, appDataBundle, false);
        return true;
	}
}
