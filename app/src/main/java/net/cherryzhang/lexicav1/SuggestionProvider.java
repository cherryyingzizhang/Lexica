package net.cherryzhang.lexicav1;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import net.cherryzhang.lexicav1.WordList.WordListBank;

import java.util.ArrayList;
import java.util.Locale;

public class SuggestionProvider extends ContentProvider {
	
	public static boolean search = false;
	private Database db;
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public boolean onCreate() {
		db = new Database(getContext());
		Log.w("class name",getContext().getClass().toString());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (Splash.tableOrWord.contentEquals("table"))
		{
			String query = uri.getLastPathSegment();
			Log.w("",query);
			ArrayList<String> Alltables = new ArrayList<String>();
			ArrayList<String> tables = new ArrayList<String>();
			Alltables.addAll(db.getAllLexica(Settings.ORDER_ALPHABETICAL));
			for (int i = 0; i < Alltables.size(); i++)
			{
				if (Alltables.get(i).toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH)))
				{
					tables.add(Alltables.get(i));
				}
			}
			String[] array = tables.toArray(new String[tables.size()]);
			MatrixCursor cursor = new MatrixCursor
					(new String[] {"_ID",
							       SearchManager.SUGGEST_COLUMN_TEXT_1,
							       SearchManager.SUGGEST_COLUMN_INTENT_DATA});
			for (int i = 0; i < array.length; i++)
			{
				cursor.addRow(new String[] {""+i, array[i], array[i]});
				Log.w("", array[i]);
			}
			return cursor;
		}
		else if (Splash.tableOrWord.contentEquals("word"))
		{
			String query = uri.getLastPathSegment();
			Log.w("",query);
			ArrayList<WordListBank.WordItem> AllWords = new ArrayList<>();
			ArrayList<String> words = new ArrayList<String>();
			AllWords.addAll(db.getDataItems(ItemListActivity.tableName, Settings.ORDER_ALPHABETICAL));
			for (int i = 0; i < AllWords.size(); i++)
			{
				if (AllWords.get(i).item.toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH)))
				{
					words.add(AllWords.get(i).item);
				}
			}
			String[] array = words.toArray(new String[words.size()]);
			MatrixCursor cursor = new MatrixCursor
					(new String[] {"_ID",
							       SearchManager.SUGGEST_COLUMN_TEXT_1,
							       SearchManager.SUGGEST_COLUMN_INTENT_DATA});
			for (int i = 0; i < array.length; i++)
			{
				cursor.addRow(new String[] {""+i, array[i], array[i]});
				Log.w("", array[i]);
			}
			search = true;
			return cursor;
		}
		
		return null;
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
