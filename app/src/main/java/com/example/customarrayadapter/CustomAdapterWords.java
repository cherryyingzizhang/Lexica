package com.example.customarrayadapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.lexicav1.Database;
import com.example.lexicav1.ImageSaver;
import com.example.lexicav1.ItemListActivity;
import com.example.lexicav1.R;
import com.example.lexicav1.Settings;
import com.example.lexicav1.WordList.WordListBank;
import com.example.lexicav1.WordList.WordListBank.WordItem;

import java.util.List;

public class CustomAdapterWords extends ArrayAdapter<WordItem> {

	private List<WordItem> items;
	private Context context;
	
	public CustomAdapterWords(Context context, int resource, List<WordItem> iTEMS2) {
		super(context, resource, iTEMS2);
		this.context = context;
		this.items = iTEMS2;
	}
	
	@Override
    public boolean isEnabled(int index) {
        return !ItemListActivity.isSlideShowRunning;
    }

	@Override
	public View getView(int position, View row, ViewGroup parent) {
		
		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		row = inflater.inflate(R.layout.row, parent, false);
		
		Button bRemove = (Button) row.findViewById(R.id.bDeleteRow);
		bRemove.setTag(position);
		row.setTag(items.get(position).item);
		TextView name = (TextView) row.findViewById(R.id.tvRow);
		name.setText(items.get(position).item);
		
		bRemove.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				final Dialog dialog;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					dialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert);
				} else {
					dialog = new Dialog(context);
				}
				dialog.setTitle("Delete this item?");
				dialog.setContentView(R.layout.confirm_dialog_for_deletion);
				if (!ItemListActivity.isSlideShowRunning)
				{
					dialog.show();
				}
				final Button confirm = (Button) dialog.findViewById(R.id.bConfirm);
				Button cancel = (Button) dialog.findViewById(R.id.bCancel);
				
				final int i = (Integer) arg0.getTag();
				
				confirm.setOnClickListener(new Button.OnClickListener(){

					@Override
					public void onClick(View v1) {
	                    Database db = new Database(context);
	                    db.open();
	                    db.deleteItem(ItemListActivity.tableName, items.get(i).item);
						boolean deleted = new ImageSaver(context).deleteFile(items.get(i).imageFileName);
						Log.w("image file deleted?", "" + deleted);
	                    CustomAdapterWords.this.remove(items.get(i));
						WordListBank.setContext(db.getDataItems(ItemListActivity.tableName,
								Settings.getOrderFromSP(context, ItemListActivity.tableName)));
	                    CustomAdapterWords.this.notifyDataSetChanged();
						dialog.dismiss();
					}}
				);
				
				cancel.setOnClickListener(new Button.OnClickListener(){

					@Override
					public void onClick(View v2) {
						dialog.cancel();
					}}
				);
			}});
		
		return row;
	}
	
}
