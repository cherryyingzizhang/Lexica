package net.cherryzhang.lexicav1;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import net.cherryzhang.lexicav1.WordList.WordListBank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Database {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_WORD_OR_TITLE = "word_or_title"; //can be word or title of passage
	public static final String KEY_MEANING_OR_PASSAGE = "meaning"; //can be word meaning or passage body
	private static final String KEY_IS_PASSAGE = "passage";
	private static final String KEY_IMAGE = "image";
	
	private static final String DATABASE_NAME = "Lexica";
	public static final int DATABASE_VERSION = 1;

	private DbHelper ourHelper;
	private final Context ourContext;
	private static SQLiteDatabase ourDatabase;
	
	
	private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_WORD_OR_TITLE, KEY_WORD_OR_TITLE);
        map.put(KEY_MEANING_OR_PASSAGE, KEY_MEANING_OR_PASSAGE);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }
	
	private static class DbHelper extends SQLiteOpenHelper{

		public DbHelper(Context context) {
			//super(context, name, factory, version);
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		//first table: 26 letters
		private static final String DEFAULT_TABLE_1 = "Alphabet";
		//second table: simple 3-letter words
		private static final String DEFAULT_TABLE_2 = "3-letter Words";
		//third table: passages
		private static final String DEFAULT_TABLE_3 = "Short Passages";

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			// when database is created, we create a default table
			db.execSQL("CREATE TABLE [Homework] (" +		//table 0
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_WORD_OR_TITLE + " TEXT NOT NULL, " +
					KEY_MEANING_OR_PASSAGE + " TEXT NOT NULL , " +
					KEY_IS_PASSAGE + " INTEGER DEFAULT 0, " +
					KEY_IMAGE + " TEXT" + ");"
					);
			
			db.execSQL("CREATE TABLE [" + DEFAULT_TABLE_1 + "] (" +
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_WORD_OR_TITLE + " TEXT NOT NULL, " +
					KEY_MEANING_OR_PASSAGE + " TEXT NOT NULL," +
					KEY_IS_PASSAGE + " INTEGER DEFAULT 0, " +
					KEY_IMAGE + " TEXT" + ");"
					);

			db.execSQL("CREATE TABLE [" + DEFAULT_TABLE_2 + "] (" +
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_WORD_OR_TITLE + " TEXT NOT NULL, " +
					KEY_MEANING_OR_PASSAGE + " TEXT NOT NULL," +
					KEY_IS_PASSAGE + " INTEGER DEFAULT 0, " +
					KEY_IMAGE + " TEXT" + ");"
					);

			db.execSQL("CREATE TABLE [" + DEFAULT_TABLE_3 + "] (" +
					KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_WORD_OR_TITLE + " TEXT NOT NULL, " +
					KEY_MEANING_OR_PASSAGE + " TEXT NOT NULL," +
					KEY_IS_PASSAGE + " INTEGER DEFAULT 0, " +
					KEY_IMAGE + " TEXT" + ");"
			);

			for (int i = 65; i < 65+26; i++)
			{
				db.execSQL("INSERT INTO '" + DEFAULT_TABLE_1
						+ "' VALUES(null, '" + Character.toString((char)i) + "', '"
						+ Character.toString((char)(i+32)) + "', 0, null);"); //32 offset for lower case
			}
			
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'cat', 'meow', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'dog', 'woof', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'cow', 'moo', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'bat', 'flying; cave', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'sad', 'upset', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'mad', 'angry', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'bad', 'not good', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'rat', 'mouse', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'sun', 'solar', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'cup', 'drink, mug', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'hat', 'head', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'bus', 'big car', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'man', 'human', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'pen', 'ink pencil', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'map', 'directions', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'bed', 'sleep', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'box', 'square', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'car', 'drive', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'pig', 'oink', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'fan', 'A/C', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'leg', 'foot', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'egg', 'chicken', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'mom', 'mother', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'dad', 'father', 0, '');");
			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_2 +"' VALUES(null, 'ice', 'frozen water', 0, '');");

			db.execSQL("INSERT INTO '"+ DEFAULT_TABLE_3 +"' VALUES(null, 'Persuasion','Persuasion is the art of convincing someone to agree with your " +
					"point of view. According to the ancient Greek philosopher Aristotle, there " +
					"are three basic tools of persuasion: ethos, pathos, and logos.\n" +
					"Ethos is a speaker’s way of convincing the audience that she is a " +
					"credible source. An audience will consider a speaker credible if she seems " +
					"trustworthy, reliable, and sincere. This can be done in many ways. For " +
					"example, a speaker can develop ethos by explaining how much experience " +
					"or education she has in the field. After all, you would be more likely to listen " +
					"to advice about how to take care of your teeth from a dentist than a " +
					"firefighter. A speaker can also create ethos by convincing the audience that she is a good person who " +
					"has their best interests at heart. If an audience cannot trust you, you will not be able to persuade them.\n" +
					"Pathos is a speaker’s way of connecting with an audience’s emotions. For example, a speaker " +
					"who is trying to convince an audience to vote for him might say that he alone can save the country from a " +
					"terrible war. These words are intended to fill the audience with fear, thus making them want to vote for " +
					"him. Similarly, a charity organization that helps animals might show an audience pictures of injured dogs " +
					"and cats. These images are intended to fill the viewers with pity. If the audience feels bad for the animals, " +
					"they will be more likely to donate money.\n" +
					"Logos is the use of facts, information, statistics, or other evidence to make your argument more " +
					"convincing. An audience will be more likely to believe you if you have data to back up your claims. For " +
					"example, a commercial for soap might tell you that laboratory tests have shown that their soap kills all " +
					"7,000,000 of the bacteria living on your hands right now. This piece of information might make you more " +
					"likely to buy their brand of soap. Presenting this evidence is much more convincing than simply saying " +
					"“our soap is the best!” Use of logos can also increase a speaker’s ethos; the more facts a speaker " +
					"includes in his argument, the more likely you are to think that he is educated and trustworthy.\n" +
					"Although ethos, pathos, and logos all have their strengths, they are often most effective when " +
					"they are used together. Indeed, most speakers use a combination of ethos, pathos, and logos to " +
					"persuade their audiences. The next time you listen to a speech, watch a commercial, or listen to a friend " +
					"try to convince you to lend him some money, be on the lookout for these ancient Greek tools of " +
					"persuasion.', 1, '');");

			db.execSQL("INSERT INTO '" + DEFAULT_TABLE_3 + "' VALUES(null , 'Popcorn', '" +
					"Popcorn, also known as popping corn, is a special variety of corn (Zea mays everta). Each kernel contains a " +
					"tiny drop of water. When it is heated, the water expands causing the kernel to explode and flip inside out. " +
					"Most US popcorn is grown in Nebraska and Indiana, and increasingly in Texas.\n" +
					"Native Americans first discovered popcorn thousands of years ago in Guatemala or Mexico. It was popped in " +
					"China during the Song Dynasty (960-279) as well as in Sumatra and India long before Columbus reached the Americas.\n" +
					"In 1519 when he invaded Mexico, Spanish Conquistador Hernando Cortes first saw popcorn when he met the Aztecs. " +
					"Popcorn was important to the Aztecs as food, as decoration for ceremonial headdresses and necklaces, and as " +
					"ornaments on statues of their gods. Around 1612, French explorers around the Great Lakes met Iroquois who used " +
					"heated sand in a pottery vessel to make popcorn. There is an unproven theory that an Indian named Quadequina " +
					"brought a deerskin bag of popped corn for the first Thanksgiving feast on October 15, 1621. In 1948 and 1950, " +
					"anthropologist Herbert Dick and botanist Earle Smith discovered ears of popcorn in the Bat Cave of west central " +
					"New Mexico. The ears measured from smaller than a penny to about two inches. They were determined to be about " +
					"5,600 years old.\n" +
					"Colonial housewives served popcorn with sugar and cream for breakfast. Some colonists used a cylinder of thin " +
					"sheet-iron that revolved on an axle in front of the fireplace to make popped corn.\n" +
					"In 1885, Charles Cretors of Chicago, Illinois, invented the first popcorn machine. Street vendors were soon pushing" +
					" steam or gas-powered poppers through fairs, parks, and expositions.\n" +
					"Today much of the popcorn you buy at movies and fairs is popped in machines manufactured by the Cretors family.\n" +
					"In 1914, in Sioux City, Iowa, Cloid H. Smith created America’s first branded popcorn (Jolly Time), and for the " +
					"first time, popcorn was available in grocery stores.\n" +
					"Americans eat more than 17 billion quarts of popcorn a year, an average of 60 quarts per person per year. " +
					"As the result of an elementary school project, popcorn became the official state snack food of Illinois. January " +
					"19 is National Popcorn Day, and October is National Popcorn Month." +
					"', 1, '');" + ")");

			db.execSQL("INSERT INTO '" + DEFAULT_TABLE_3 + "' VALUES(null , 'Windsor Castle', '" +
					"Windsor Palace is the world’s largest and oldest continuously inhabited castle. Occupying over 484,000 square feet, " +
					"it is over 240 times the size of an average house. William the Conqueror built the first castle on the grounds " +
					"between 1070 and 1086, but the castle that exists today was largely built by Edward of Windsor in 1350. Edward of " +
					"Windsor authorized the construction of a new keep, a large chapel, and new fortifications. From a distance, the " +
					"castle appears dominated by a massive round tower in its center. \n" +
					"\n" +
					"In 1475, King Edward IV authorized construction of St. George’s Chapel as a cathedral and royal mausoleum. The " +
					"chapel became an important destination for pilgrims in the late medieval period and is probably the most famous " +
					"of the structures within Windsor Palace. During the 1500’s and 1600’s, Windsor Castle was damaged as a result of " +
					"various wars. In 1660, however, Charles II became interested in restoring the castle and laid out plans for “The " +
					"Long Walk,” a three-mile long avenue running south from the castle. Charles II also had the royal apartments and " +
					"St. George’s Hall rebuilt. The royal apartments were spectacular, with numerous carvings, frescoes, and tapestries. " +
					"The artwork acquired during the rebuilding of Windsor Castle became known as the Royal Collection, which remains " +
					"relatively unchanged today. \n" +
					"\n" +
					"In 1824, George IV moved into the castle and was granted 300,000 pounds to renovate Windsor Castle. The entire " +
					"castle was remodeled and the architect, Jeffrey Wyattville, succeeded in blending the castle to seem like one " +
					"entity rather than a collection of buildings. Wyattville raised and lowered the heights of various buildings to " +
					"give them symmetry and improved the appearance and structure of others.\n" +
					"', 1, '');" + ")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DEFAULT_TABLE_1);
			db.execSQL("DROP TABLE IF EXISTS " + DEFAULT_TABLE_2);
			db.execSQL("DROP TABLE IF EXISTS " + DEFAULT_TABLE_3);
			onCreate(db);
		}

	}

	//constructor
	public Database(Context c)
	{
		ourContext = c;
	}

	//opens database
	public Database open() throws SQLException{
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();
		return this;
	}

	public void close(){
		ourHelper.close();
	}

	//A method that may be used in the future
	public Cursor getTableSuggestions(String query)
	{
		Cursor cursor = 
				ourDatabase.rawQuery
				("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE " + query,null);
		return cursor;
	}

	public long createItem(String tableName, String word, String meaning, int isPassage, String imageFileName)
	{
		//TODO: Once we do data analytics.
		ContentValues cv = new ContentValues();
		cv.put("table_name", tableName);
		cv.put("word", word);
		cv.put("numMiscues", 0);
		///

		ContentValues cv2 = new ContentValues();
		cv2.put(KEY_WORD_OR_TITLE, word); //keys are the column NAMES you created
		cv2.put(KEY_MEANING_OR_PASSAGE, meaning); //in the table!
		cv2.put(KEY_IS_PASSAGE, isPassage);
		cv2.put(KEY_IMAGE, imageFileName);

		return ourDatabase.insert("[" + tableName + "]", null, cv2);
	}

	public void editItem(String tableName, WordListBank.WordItem oldItem, String word, String meaning, int isPassage, String imageFileName)
	{
		ContentValues cvUpdate = new ContentValues();
		cvUpdate.put(KEY_WORD_OR_TITLE, word);
		cvUpdate.put(KEY_MEANING_OR_PASSAGE, meaning);
		cvUpdate.put(KEY_IS_PASSAGE, isPassage);
		cvUpdate.put(KEY_IMAGE, imageFileName);

		ourDatabase.update("'" + tableName + "'", cvUpdate, KEY_WORD_OR_TITLE + " = '" + oldItem.item + "'", null);

		ContentValues cv = new ContentValues();
		cv.put("word", word);
		cv.put("numMiscues", 0);
	}

	public ArrayList<WordListBank.WordItem> getDataItems(String TableName, int order) {
		String []columns = new String[] {KEY_ROWID, KEY_WORD_OR_TITLE, KEY_MEANING_OR_PASSAGE, KEY_IS_PASSAGE, KEY_IMAGE};
		//cursor helps you read information from database
		Cursor c = ourDatabase.query("["+TableName+"]",columns,null, null, null, null, null);
		ArrayList<WordListBank.WordItem> result = new ArrayList<>();
		int item = c.getColumnIndex(KEY_WORD_OR_TITLE); //position 1
		int description = c.getColumnIndex(KEY_MEANING_OR_PASSAGE); //position 2
		int isPassage = c.getColumnIndex(KEY_IS_PASSAGE); //position 3
		int imageFileName = c.getColumnIndex(KEY_IMAGE); //position 4
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
		{
			result.add(new WordListBank.WordItem(c.getString(item), c.getString(description), c.getInt(isPassage), c.getString(imageFileName)));
		}

		switch (order)
		{
			case Settings.ORDER_DATE_CREATED:
				//do nothing
				break;
			case Settings.ORDER_ALPHABETICAL:
				Collections.sort(result);
				break;
			case Settings.ORDER_RANDOM:
				Collections.shuffle(result);
				break;
		}

		return result;
	}

	//Unused method that will likely be used sometime in the future.
	public String getMeaningFromWord(String tableName, String word) throws SQLException
	{
		    Cursor c = ourDatabase.rawQuery("SELECT " + KEY_ROWID + ", " +
					KEY_WORD_OR_TITLE + ", " + KEY_MEANING_OR_PASSAGE + " FROM [" + tableName + "] WHERE " + KEY_WORD_OR_TITLE + " like '"
		    		+ word + "'", null);
		    String meaning = c.getString(2);
			return meaning;
	}

	public String getImageFileNameFromWord(String tableName, String word) {
		Cursor c = ourDatabase.rawQuery("SELECT " + KEY_ROWID + ", " +
				KEY_WORD_OR_TITLE + ", " + KEY_MEANING_OR_PASSAGE + ", " +
				KEY_IS_PASSAGE + ", " + KEY_IMAGE +
				" FROM [" + tableName + "] WHERE " + KEY_WORD_OR_TITLE + " == '"
				+ word + "'", null);
		String imageFileName = c.getString(4);
		return imageFileName;
	}
	
	public void createLexicon(String name) throws SQLException{
		ourDatabase.execSQL("CREATE TABLE [" + name + "] (" +		
				KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				KEY_WORD_OR_TITLE + " TEXT NOT NULL, " +
				KEY_MEANING_OR_PASSAGE + " TEXT NOT NULL, " +
				KEY_IS_PASSAGE + " INTEGER DEFAULT 0, " +
				KEY_IMAGE + " TEXT);"
		);

		ContentValues cv = new ContentValues();
		cv.put("tableName", name);
		cv.put("numMiscues", 0);
	}
	
	public void deleteLexicon(String tableName) throws SQLException{
		ourDatabase.execSQL("DROP TABLE IF EXISTS [" + tableName + "]");
	}

	public ArrayList<String> getAllLexica(int order) {
		ArrayList<String> tableList = new ArrayList<String>();

        Cursor cursor = ourDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                tableList.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        for (int i = 0; i < tableList.size(); i++) {Log.w("",tableList.get(i));
			if (tableList.get(i).contentEquals("android_metadata")
					|| tableList.get(i).contentEquals("sqlite_sequence")
					|| tableList.get(i).contentEquals("DIAGNOSTICS_TABLE")
					|| tableList.get(i).contentEquals("miscuesinEachLexicon")) {
				tableList.remove(i);
				i--;
			}
		}

		switch (order)
		{
			case Settings.ORDER_DATE_CREATED:
				//do nothing
				break;
			case Settings.ORDER_ALPHABETICAL:
				Collections.sort(tableList);
				tableList.remove("Homework");
				tableList.add(0, "Homework");
				break;
			case Settings.ORDER_RANDOM:
				Collections.shuffle(tableList);
				tableList.remove("Homework");
				tableList.add(0, "Homework");
				break;
		}
        
        return tableList;
	}
	
	public boolean lexiconExists(String tableName)
	{
		ArrayList<String> tables = getAllLexica(Settings.ORDER_DATE_CREATED);
		for (int i = 0; i < tables.size(); i++)
		{
			if (tables.get(i).contentEquals(tableName))
			{
				return true;
			}
		}
		return false;
	}

	public void deleteItem(String tableName, String word) {
		ourDatabase.delete("["+tableName+"]", KEY_WORD_OR_TITLE + "='" + word + "'", null);
	}

	public boolean itemExists(String tableName, String word) {
		String sql = "SELECT * FROM [" + tableName + "] WHERE "+ KEY_WORD_OR_TITLE +" = '" + word + "'";
		Cursor c = ourDatabase.rawQuery(sql, null);
		
		if (c.moveToFirst()) {
		    return true;
		} else {
		    return false;
		}
	}

	public void changeLexiconName(String oldLexicon, String lexiconName) {
		ourDatabase.execSQL("ALTER TABLE [" + oldLexicon + "] RENAME TO [" + lexiconName + "]");

		//remember to update both special tables!
		ContentValues cvUpdate = new ContentValues();
		cvUpdate.put("table_name", lexiconName);
	}
}
