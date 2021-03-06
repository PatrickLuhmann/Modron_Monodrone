package com.nerdyneutrino.modron_monodrone;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SampleStarter extends ListActivity {
	String samples[] = {
		"Button Response",
		"Drawing",
		"Bitmap",
		"Animated Bitmap Objects",
		"Fractal",
		"Play Deck 1",
		"Swipe",
		"Falling Down",
		"Sample Of Unknown Origin"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, samples));
		MyDebug.Print(this.getClass().getSimpleName(), "onCreate");
	}
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);

		// TODO: There is probably a more elegant or "Android" way of doing this.
		// The game book extracts the text of the string at the position and uses
		// it to create the class name, which means that there is no need for manual
		// addition of cases. That seems too clever for me so I will do it the old-
		// fashioned way for now.
		Intent intent;
		switch (position)
		{
			case 0:
				intent = new Intent(this, ButtonResponse.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(this, Drawing.class);
				startActivity(intent);
				break;
			case 2:
				intent = new Intent(this, MyBitmap.class);
				startActivity(intent);
				break;
			case 3:
				intent = new Intent(this, AnimatedBitmapObjects.class);
				startActivity(intent);
				break;
			case 4:
				intent = new Intent(this, Rectangles.class);
				startActivity(intent);
				break;
			case 5:
				intent = new Intent(this, PlayDeck1.class);
				startActivity(intent);
				break;
			case 6:
				intent = new Intent(this, Swipe.class);
				startActivity(intent);
				break;
			case 7:
				intent = new Intent(this, FallingDown.class);
				startActivity(intent);
				break;
			default:
				MyDebug.Print(this.getClass().getSimpleName(), "Click on item not implemented yet.");
				break;
		}
	}
}
