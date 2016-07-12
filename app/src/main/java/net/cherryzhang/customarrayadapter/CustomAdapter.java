package net.cherryzhang.customarrayadapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import net.cherryzhang.lexicav1.Database;
import net.cherryzhang.lexicav1.R;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {

	public ArrayList<String> items;
	private Context context;
	
	public CustomAdapter(Context context, int resource, ArrayList<String> a) {
		super(context, R.layout.row, a);
		this.context = context;
		this.items = a;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		notifyDataSetChanged();
		View row = convertView;

		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		row = inflater.inflate(R.layout.row, parent, false);
		
		Button bRemove = (Button) row.findViewById(R.id.bDeleteRow);
		bRemove.setTag(position);
		TextView name = (TextView) row.findViewById(R.id.tvRow);
		name.setText(items.get(position));
		
		if (items.get(position).contentEquals("Homework"))
		{
			bRemove.setVisibility(View.GONE);
		}
		
		bRemove.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v1) {
					final Dialog dialog;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						dialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog_Alert);
					} else {
						dialog = new Dialog(context);
					}
					dialog.setTitle("Are you sure?");
					dialog.setContentView(R.layout.confirm_dialog_for_deletion);
					dialog.show();
					Button confirm = (Button) dialog.findViewById(R.id.bConfirm);
					Button cancel = (Button) dialog.findViewById(R.id.bCancel);
					
					final int i = (Integer) v1.getTag();
					
					confirm.setOnClickListener(new Button.OnClickListener(){
	
						@Override
						public void onClick(View v1) {
		                    Database db = new Database(context);
		                    db.open();
		                    db.deleteLexicon(items.get(i));
		                    CustomAdapter.this.remove(items.get(i));
		                    notifyDataSetChanged();
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

