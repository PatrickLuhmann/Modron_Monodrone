package com.neurdyneutrino.modron_monodrone;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SampleStarter extends ListActivity {
	String samples[] = {
		"Button Response",
		"Sample 2"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, samples));
	}
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);

		// TODO: There is probably a more elegant or "Android" way of doing this.
		switch (position)
		{
			case 0:
				Intent intent = new Intent(this, ButtonResponse.class);
				startActivity(intent);
				break;
			default:
				break;
		}
	}

}
