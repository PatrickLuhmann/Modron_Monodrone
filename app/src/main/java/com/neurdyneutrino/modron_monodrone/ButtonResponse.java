package com.neurdyneutrino.modron_monodrone;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ButtonResponse extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_button_response);

		TextView msg = new TextView(this);
		msg.setText("Welcome to the Button Response sample");

		ViewGroup layout = (ViewGroup)findViewById(R.id.activity_button_response);
		layout.addView(msg);

		Button btn = new Button(this);
		btn.setText("Press Me!");
		layout.addView(btn);

		TextView clickCountText = new TextView(this);
		clickCountText.setText("Number of clicks: 0");
		layout.addView(clickCountText);

		Log.v("nerdyneutrino", "This is a test log message");
	}
}
