package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Drawing extends Activity {
	Random rand = new Random();
	int tick = 0;
	Paint myRed = new Paint();
	Paint myGreen = new Paint();

	class RenderView extends View {

		public RenderView(Context context) {
			super(context);
		}

		protected void onDraw(Canvas canvas) {
			int Width = canvas.getWidth();
			int Height = canvas.getHeight();
			int smaller = min(Width, Height);

			//canvas.drawRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

			int x1 = tick % smaller;
			int x2 = smaller - tick % smaller;
			if (x1 > x2) {
				int temp = x2;
				x2 = x1;
				x1 = temp;
			}
			int y1 = tick % smaller;
			int y2 = smaller - tick % smaller;
			if (y1 > y2) {
				int temp = y2;
				y2 = y1;
				y1 = temp;
			}
			myRed.setStyle(Paint.Style.STROKE);
			canvas.drawRect(x1, y1, x2, y2, myGreen);

			canvas.drawLine(tick % smaller, tick % smaller, smaller - tick % smaller, smaller - tick % smaller, myRed);

			tick++;
			invalidate();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		myRed.setColor(0xFFFF0000);
		myGreen.setColor(Color.GREEN);
		setContentView(new RenderView(this));
	}
}
