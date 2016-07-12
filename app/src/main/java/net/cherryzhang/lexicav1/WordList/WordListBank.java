package net.cherryzhang.lexicav1.WordList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordListBank
{

	/**
	 * An array of sample word items.
	 */
	public static List<WordItem> ITEMS = new ArrayList<WordItem>();

	/**
	 * A map of sample word items, by ID.
	 */
	public static Map<String, WordItem> ITEM_MAP = new HashMap<String, WordItem>();

	public static void addItem(WordItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.item, item);
	}

	public static ArrayList<String> getItemListAsString()
	{
		ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < ITEMS.size(); i++)
		{
			result.add(ITEMS.get(i).item);
		}
		return result;
	}
	
	public static void close()
	{
		ITEM_MAP.clear();
	}

	/**
	 * A word item representing a piece of content.
	 */
	public static class WordItem implements Comparable<WordItem>  {
		public String item;
		public String description;
		public int isPassage;
		public String imageFileName;

		public WordItem(String word, String meaning, int isPassage, String imageFileName) {
			this.item = word;
			this.description = meaning;
			this.isPassage = isPassage;
			this.imageFileName = imageFileName;
		}

		@Override
		public String toString() {
			return item;
		}

		//so I can use Collections.sort(DummyContent.Items) where Items is a list of DummyItems
		@Override
		public int compareTo(WordItem other) {
			return this.item.compareTo(other.item);
		}
	}
	
	public static void setContext(ArrayList<WordItem> items) {
		ITEMS.clear();
		ITEM_MAP.clear();
		for (int i = 0; i < items.size(); i++)
		{
			addItem(new WordItem(items.get(i).item, items.get(i).description, items.get(i).isPassage, items.get(i).imageFileName));
		}
		
	}
}
