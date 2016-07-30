package com.example.lexicav1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.customarrayadapter.CustomAdapterWords;
import com.example.lexicav1.WordList.WordListBank;

/**
 * A list fragment representing a list of Books. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment {
	public static CustomAdapterWords aa;
	private Database db;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment'displayedTimeOnTimer current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		aa = new CustomAdapterWords(getActivity(), 0, WordListBank.ITEMS);
		this.setListAdapter(aa);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

	        @Override
	        public boolean onItemLongClick(final AdapterView<?> listView, View view,
										   final int position, long id) {
				ItemListActivity.addorModifyItemDialog = new AddorModifyItemDialog(getActivity(), db, WordListBank.ITEMS.get(position));
				ItemListActivity.addorModifyItemDialog.startDialog(new AddorModifyItemDialog.OnItemSuccessfullyAddedListener()
				{
					@Override
					public void OnItemSuccessfullyAdded(String wordText, String meaningText, boolean isPassageChecked)
					{
						Toast.makeText(getActivity().getApplicationContext(),
								"Item entry successfully edited. ", Toast.LENGTH_SHORT)
								.show();

						if (ItemListActivity.mTwoPane) //if two pane
						{
							//programmatically display newly added word in itemdetailfragment if in two-pane mode.
							ListView itemListFragmentListviewRef = ((ItemListFragment) getActivity().getSupportFragmentManager()
									.findFragmentById(R.id.book_list))
									.getListView();
							WordListBank.WordItem mItem = WordListBank.ITEM_MAP.get(wordText);
							int position = WordListBank.ITEMS.indexOf(mItem);
							itemListFragmentListviewRef
									.performItemClick(itemListFragmentListviewRef.getAdapter().getView(position, null, null),
											position,
											itemListFragmentListviewRef.getAdapter().getItemId(position));
						}
					}
				});
	            return true;
	        }
	    });
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment'displayedTimeOnTimer callbacks.");
		}

		mCallbacks = (Callbacks) activity;
		
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		// mCallbacks.onItemSelected((String)
		// listView.getItemAtPosition(position));
		// mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).item);
		// //replaced id with word
		mCallbacks.onItemSelected(WordListBank.ITEMS.get(position).item);

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	public void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			if (ItemListActivity.isSlideShowRunning)
			{
				//do nothing?
			}
			else
			{
				getListView().setItemChecked(position, true);
			}
		}

		mActivatedPosition = position;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		db = new Database(getActivity());
		db.open();
		int order = Settings.getOrderFromSP(getActivity(), ItemListActivity.tableName);
		WordListBank.setContext(db.getDataItems(ItemListActivity.tableName, order));
		aa.notifyDataSetChanged();

		if (ItemListActivity.mTwoPane)
		{
			//by default, set the first item to be selected in the itemlistfragment pane, unless
			//ItemListActivity.activityResultFromSearching = true (this boolean means another word
			// other than the first word has to be displayed and that is handled within the
			// ItemListActivity.invisButtonForSearch onClick handler)
			if (WordListBank.ITEMS.size() > 0)
			{
				if (!ItemListActivity.activityResultFromSearching)
				{
					this.getListView().setItemChecked(0, true);
					mCallbacks.onItemSelected(WordListBank.ITEMS.get(0).item);
				}
				else
				{
					ItemListActivity.activityResultFromSearching = false;
				}
			}
			else //nothing would be in this item list, so itemdetailfragment should not display anything
			{
				LinearLayout layoutInsideScrollViewInsideItemDetailFragmentContainer =
						(LinearLayout) getActivity().findViewById(R.id.layoutInsideScrollView);
				layoutInsideScrollViewInsideItemDetailFragmentContainer.setVisibility(View.GONE);
			}
		}
	}
	
	@Override
	public void onPause()
	{
		if (ItemListActivity.mTwoPane)
		{
			ItemListActivity.isSlideShowRunning = false;
		}
		super.onPause();
		db.close();
	}
	
	public class UpdateReceiver extends BroadcastReceiver {
	    public static final String NEW_UPDATE_BROADCAST =
	            "com.example.NEW_UPDATE_BROADCAST";
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        //Update the list
	    }
	}
}
