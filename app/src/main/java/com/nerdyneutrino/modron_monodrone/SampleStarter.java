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
			default:
				MyDebug.Print(this.getClass().getSimpleName(), "Click on item not implemented yet.");
				break;
		}
	}
}
