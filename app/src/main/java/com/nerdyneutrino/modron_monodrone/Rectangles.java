package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class Rectangles extends Activity {
	ThreadedRenderView rview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		rview = new ThreadedRenderView(this);
		setContentView(rview);
		//setContentView(R.layout.activity_rectangles);
	}

	protected void onResume() {
		super.onResume();
		rview.resume();
	}

	protected void onPause() {
		super.onPause();
		rview.pause();
	}

	class ThreadedRenderView extends SurfaceView implements Runnable {
		Thread renderThread = null;
		SurfaceHolder holder;
		volatile boolean running = false;
		Paint myRed;

		public ThreadedRenderView(Context context) {
			super(context);
			MyDebug.Print("Rectangles", "ThreadedRenderView constructor.");
			holder = getHolder();

			myRed = new Paint();
			myRed.setColor(Color.RED);
			myRed.setStyle(Paint.Style.STROKE);
		}

		public void resume() {
			MyDebug.Print("Rectangles", "resume()");
			running = true;
			renderThread = new Thread(this);
			renderThread.start();
		}

		public void pause() {
			MyDebug.Print("Rectangles", "pause()");
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

		public void run() {
			long lastT = System.nanoTime();
			while (running) {
				if (!holder.getSurface().isValid())
					continue;

				float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
				lastT = System.nanoTime();
				MyDebug.Print("Rectangles:run", "Delta Time: " + deltaT);

				Canvas canvas = holder.lockCanvas();

				// Paint it green
				canvas.drawRGB(0, 255, 0);

				int w = 215;
				int h = 300;
				int z = (int)(deltaT * 100);
				if ((z & 1) == 1)
					myRed.setStyle(Paint.Style.STROKE);
				else
					myRed.setStyle(Paint.Style.FILL_AND_STROKE);
				for (int j = 0; j < 3; j++) {
					for (int i = 0; i < 8; i++) {
						int x = i * (w + 6) + 3;
						int y = j * (h + 6) + 3;

						canvas.drawRect(x, y, x + w - 1, y + h - 1, myRed);
					}
				}

				holder.unlockCanvasAndPost(canvas);
			}
		}
	}
}
