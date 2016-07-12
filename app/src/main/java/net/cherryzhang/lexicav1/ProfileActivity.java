package net.cherryzhang.lexicav1;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import net.cherryzhang.holographlibrary.PieGraph;
import net.cherryzhang.holographlibrary.PieSlice;

import java.util.ArrayList;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity
{
	ArrayList<String> lexica = new ArrayList<String>();
	ArrayList<Integer> numMiscuesInLexica = new ArrayList<Integer>();
	ArrayList<Integer> miscueLexicaColorSlices = new ArrayList<Integer>();
	Random r = new Random();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		PieGraph pg = (PieGraph) findViewById(R.id.graph);
		PieSlice slice;
		int color = 0;
		
		Database db = new Database(this);
		db.open();
		lexica.addAll(db.getAllLexica(Settings.ORDER_DATE_CREATED));
//		numMiscuesInLexica.addAll(db.getAllMiscues(lexica));
		Log.w("",""+numMiscuesInLexica.get(0));
		db.close();

		for (int i = 0; i < lexica.size(); i++)
		{
			Log.w("wtf","wtf");
			slice = new PieSlice();
			if (i != 0)
			{
				boolean notSameColorAsAnotherColor = true;
				int blah = 0; //should have used dowhile...
				while (notSameColorAsAnotherColor == false || blah == 0)
				{
					color = Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256)); 
					for (int j = 0; j < miscueLexicaColorSlices.size(); j++)
					{
						if (miscueLexicaColorSlices.get(j) == color)
						{
							notSameColorAsAnotherColor = true;
						}
					}
					blah++;
				}
				miscueLexicaColorSlices.add(color);
			}
			else 
			{
				miscueLexicaColorSlices.add(color);
			}
			
			slice.setColor(Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
			if (numMiscuesInLexica.get(i) != 0)
			{
				slice.setValue(numMiscuesInLexica.get(i));
				pg.addSlice(slice);
			}
		}
		
		///////////////////////////////////////////////////
		// pie graph of miscues for each table
//		slice.setColor(Color.parseColor("#99CC00"));
//		slice.setValue(1);
//		pg.addSlice(slice);
//		slice = new PieSlice();
//		slice.setColor(Color.parseColor("#FFBB33"));
//		slice.setValue(1);
//		pg.addSlice(slice);
//		slice = new PieSlice();
//		slice.setColor(Color.parseColor("#AA66CC"));
//		slice.setValue(2);
//		pg.addSlice(slice);
		///////////////////////////////////////////////////
	}
}
