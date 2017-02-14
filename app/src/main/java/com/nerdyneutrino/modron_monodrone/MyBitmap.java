package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class MyBitmap extends Activity {
	ThreadedRenderView rview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		rview = new ThreadedRenderView(this);
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

	class ThreadedRenderView extends SurfaceView implements Runnable {
		Thread renderThread = null;
		SurfaceHolder holder;
		volatile boolean running = false;
		volatile int tick = 0;
		int v = 100; // px per second
		Bitmap bob1;

		public ThreadedRenderView(Context context) {
			super(context);
			MyDebug.Print("MyBitmap", "ThreadedRenderView constructor.");
			holder = getHolder();

			// Grab a bitmap file
			try {
				AssetManager am = context.getAssets();
				InputStream is = am.open("bobargb8888.png");
				bob1 = BitmapFactory.decodeStream(is);
				is.close();
				MyDebug.Print("MyBitmap", "bobargb8888.png format: " + bob1.getConfig());
			} catch (IOException e) {
				// I don't know what to do here.
				MyDebug.Print("MyBitmap", "Caught an exception while trying to load my bitmap file.");
			} finally {
				// Close input streams, I guess.
			}
		}

		public void resume() {
			running = true;
			renderThread = new Thread(this);
			renderThread.start();
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

		public void run() {
			long lastT = System.nanoTime();
			float oldX = 0.0f;
			while (running) {
				if (!holder.getSurface().isValid())
					continue;

				float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
				lastT = System.nanoTime();
				MyDebug.Print("MyBitmap:run", "Delta Time: " + deltaT);

				Canvas canvas = holder.lockCanvas();

				// Paint it black
				canvas.drawRGB(0, 0, 0);

				// Determine the new position
				float newX = oldX + v * deltaT;
				if (newX > 500)
					newX = 0;
				oldX = newX;

				// Display the bitmap
				canvas.drawBitmap(bob1, newX, 100, null);

				holder.unlockCanvasAndPost(canvas);

				tick++;
			}
		}
	}
}
