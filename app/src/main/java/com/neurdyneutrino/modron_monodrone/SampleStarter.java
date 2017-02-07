package com.neurdyneutrino.modron_monodrone;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class SampleStarter extends ListActivity {
	String samples[] = {"Sample 1", "Sample 2"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, samples));
	}
}
