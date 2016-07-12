package net.cherryzhang.lexicav1;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.cherryzhang.lexicav1.WordList.WordListBank;

import java.util.ArrayList;

public class SearchableActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{

	String tableOrWords;
	ArrayList<String> list = new ArrayList<String>();
	Intent intent;
	Database db;
	ListView lvSearchResults;
	ArrayAdapter arrayAdapter;
	static String data = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new Database(this);
		setContentView(R.layout.searchable_activity);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		setTitle("Search Results");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		lvSearchResults = (ListView) findViewById(R.id.lvSearchResults);

		// Get the intent, verify the action and get the query
		intent = getIntent();
		tableOrWords = intent.getBundleExtra(SearchManager.APP_DATA).getString("tableOrWords");

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			list = doMySearch(query, tableOrWords);

			if (list.size() == 0)
			{
				TextView noResults = (TextView) findViewById(R.id.NoResults);
				noResults.setVisibility(View.VISIBLE);
			}

			arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
			lvSearchResults.setAdapter(arrayAdapter);
			lvSearchResults.setOnItemClickListener(this);
		}
		else
		{
			db.open();
			data = intent.getData().toString();
			if (tableOrWords.contentEquals("table") && db.lexiconExists(data))
			{
				Intent i = new Intent("android.intent.action.booklistactivity");
				i.putExtra("TABLE_NAME", data);
				startActivity(i);
			}
			else if (tableOrWords.contentEquals("table"))
			{
				Toast.makeText(this, "Table not found.", Toast.LENGTH_SHORT).show();
				finish();
			}
			else if (tableOrWords.contentEquals("words") &&
					db.itemExists(ItemListActivity.tableName, data))
			{
				ItemListActivity.activityResultFromSearching = true;
				finish();
			}
			else if (tableOrWords.contentEquals("words"))
			{
				Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show();
				finish();
			}
			else
			{
				Toast.makeText(this, "Search error.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		db.open();
		Object item = parent.getItemAtPosition(position);
		boolean ifTableExists = db.lexiconExists(item.toString());
		//db.close();

		if (ifTableExists && tableOrWords.contentEquals("table"))
		{
			Intent i = new Intent("android.intent.action.booklistactivity");
			i.putExtra("TABLE_NAME", item.toString());
			startActivity(i);
		}
		else if (tableOrWords.contentEquals("words"))
		{
			data = item.toString();
			ItemListActivity.activityResultFromSearching = true;
			finish();
		}
	}

	private ArrayList<String> doMySearch(String query, String tableOrWords) {
		ArrayList<String> a = new ArrayList<String>();
		if (tableOrWords.contentEquals("table"))
		{
			for (int i = 0; i < ListLexica.items.size(); i++) {
				if (ListLexica.items.get(i).toLowerCase().contains(query)) {
					a.add(ListLexica.items.get(i));
				}
			}
			return a;
		}
		else if (tableOrWords.contentEquals("words")) //words
		{
			db.open();
			ArrayList<WordListBank.WordItem> words = db.getDataItems(ItemListActivity.tableName, Settings.ORDER_ALPHABETICAL);
			//db.close();
			for (int i = 0; i < words.size(); i++) {
				if (words.get(i).item.toLowerCase().contains(query)) {
					a.add(words.get(i).item);
				}
			}
			return a;
		}
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (android.R.id.home == item.getItemId())
		{
			finish();
		}
		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
}
