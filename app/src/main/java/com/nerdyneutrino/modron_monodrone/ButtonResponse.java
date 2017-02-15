package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ButtonResponse extends Activity {

	static int totalCount = 0;
	int currentCount = 0;
	StringBuilder sb = new StringBuilder();
	TextView clickCountText;
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_button_response);
		Log.d("test1", "test2");
		MyDebug.Print(this.getClass().getSimpleName(), "onCreate");

		// This sample uses a dynamically-created view, as opposed
		// to defining it statically in the XML file.

		TextView msg = new TextView(this);
		msg.setText("Welcome to the Button Response sample");

		ViewGroup layout = (ViewGroup)findViewById(R.id.activity_button_response);
		layout.addView(msg);

		intent = new Intent(this, ButtonResponseTarget.class);
		Button btn = new Button(this);
		btn.setText("Press Me!");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MyDebug.Print("button", "button click test");
				Inc();

				// Invoke the target activity.
				startActivity(intent);
			}
		});
		layout.addView(btn);

		clickCountText = new TextView(this);
		UpdateClickCountText();
		layout.addView(clickCountText);

	}

	public void Inc(){
		MyDebug.Print("Inc", "enter");
		totalCount++;
		currentCount++;
		UpdateClickCountText();
	}

	public void UpdateClickCountText(){
		sb.setLength(0);
		sb.append("Number of clicks for current instance: ");
		sb.append(currentCount);
		sb.append('\n');
		sb.append("Number of clicks across all instances: ");
		sb.append(totalCount);
		clickCountText.setText(sb.toString());
	}
}
