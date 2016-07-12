package net.cherryzhang.guides;

import java.util.ArrayList;
import java.util.Arrays;

import android.widget.ViewFlipper;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class Guides {

	public static ArrayList<String> guides = new ArrayList<String>(
		    Arrays.asList("Introduction", "Where Reading Is Used", "Getting Help"
		    		, "Planning Ahead", "Practice, Practice, Practice"));
	
	public static ViewFlipper flipper;
	
	
	
//	/**
//	 * An array of sample (dummy) items.
//	 */
//	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
//
//	/**
//	 * A map of sample (dummy) items, by ID.
//	 */
//	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();
//
//	static {
//		// Add 3 sample items.
//		addItem(new DummyItem("Guide 1", "Guide 1 Content"));
//		addItem(new DummyItem("Guide 2", "Guide 2 Content"));
//		addItem(new DummyItem("Guide 3", "Guide 3 Content"));
//	}
//
//	private static void addItem(DummyItem item) {
//		ITEMS.add(item);
//		ITEM_MAP.put(item.id, item);
//	}
//
//	/**
//	 * A dummy item representing a piece of content.
//	 */
//	public static class DummyItem {
//		public String id;
//		public String content;
//
//		public DummyItem(String id, String content) {
//			this.id = id;
//			this.content = content;
//		}
//
//		//changes listview in GuideListFragment
//		@Override
//		public String toString() {
//			return id;
//		}
//	}
}
