package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Drawing extends Activity {
	Random rand = new Random();
	//int tick = 0;
	Paint myRed = new Paint();
	Paint myGreen = new Paint();
	FastRenderView rview;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		myRed.setColor(Color.RED);
		myGreen.setColor(Color.GREEN);
		rview = new FastRenderView(this);
		setContentView(rview);
	}

	protected void onResume() {
		super.onResume();
		rview.resume();
	}

	protected void onPause() {
		super.onPause();
		rview.pause();
	}

	class FastRenderView extends SurfaceView implements Runnable {
		Thread renderThread = null;
		SurfaceHolder holder;
		volatile boolean running = false;
		volatile int tick = 0;
		StringBuilder sb = new StringBuilder();

		public FastRenderView(Context context) {
			super(context);
			holder = getHolder();
		}

		public void resume() {
			running = true;
			renderThread = new Thread(this);
			renderThread.start();
		}

		public void run() {
			while(running) {
				if(!holder.getSurface().isValid())
					continue;

				Canvas canvas = holder.lockCanvas();

				// Start the drawing here

				canvas.drawRGB(0, 0, 0);

				myGreen.setTextSize(28);
				sb.setLength(0);
				sb.append("Surface View Sample: ");
				sb.append(tick);
				canvas.drawText(sb.toString(), 0, 100, myGreen);

				int Width = canvas.getWidth();
				int Height = canvas.getHeight();
				int smaller = min(Width, Height);

				//canvas.drawRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
				//canvas.drawRGB(tick, tick, tick);

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
				//canvas.drawRect(x1, y1, x2, y2, myGreen);

				myRed.setStyle(Paint.Style.STROKE);
				//canvas.drawLine(tick % smaller, tick % smaller, smaller - tick % smaller, smaller - tick % smaller, myRed);
				canvas.drawLine(0, tick, Width - 1, tick, myRed);

				tick++;
				// End the drawing here

				holder.unlockCanvasAndPost(canvas);
			}
		}

		public void pause() {
			running = false;
			while(true) {
				try {
					renderThread.join();
					return;
				} catch (InterruptedException e) {
					// retry
				}
			}
		}
	}
}
