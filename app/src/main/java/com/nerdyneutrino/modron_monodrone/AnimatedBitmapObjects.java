package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class AnimatedBitmapObjects extends Activity {
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

	class BobObj {
		int velocity = 0;
		float pos_x = 0, pos_y = 100;

		BobObj(int v){
			velocity = v;
		}

		BobObj(float start_x, float start_y, int v){
			pos_x = start_x;
			pos_y = start_y;
			velocity = v;
		}

		void UpdatePosition(float deltaT){
			pos_x = pos_x + velocity * deltaT;
			if (pos_x > 500)
				pos_x = 0;
		}

		void Draw(Canvas canvas, Bitmap me){
			canvas.drawBitmap(me, pos_x, pos_y, null);
		}
	}

	class ThreadedRenderView extends SurfaceView implements Runnable {
		Thread renderThread = null;
		SurfaceHolder holder;
		volatile boolean running = false;
		volatile int tick = 0;
		int v = 100; // px per second
		Bitmap bob1;
		BobObj bob_obj, bob_obj2;

		public ThreadedRenderView(Context context) {
			super(context);
			MyDebug.Print("AnimatedBitmapObjects", "ThreadedRenderView constructor.");
			holder = getHolder();

			// Grab a bitmap file
			try {
				AssetManager am = context.getAssets();
				InputStream is = am.open("bobargb8888.png");
				bob1 = BitmapFactory.decodeStream(is);
				is.close();
				MyDebug.Print("AnimatedBitmapObjects", "bobargb8888.png format: " + bob1.getConfig());
			} catch (IOException e) {
				// I don't know what to do here.
				MyDebug.Print("AnimatedBitmapObjects", "Caught an exception while trying to load my bitmap file.");
			} finally {
				// Close input streams, I guess.
			}

			bob_obj = new BobObj(v);
			bob_obj2 = new BobObj(1, 1, 50);
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
				//MyDebug.Print("AnimatedBitmapObjects:run", "Delta Time: " + deltaT);

				// Determine the new positions
				bob_obj.UpdatePosition(deltaT);
				bob_obj2.UpdatePosition(deltaT);

				Canvas canvas = holder.lockCanvas();

				// Paint it black
				canvas.drawRGB(0, 0, 0);

				// Display the bitmaps
				bob_obj.Draw(canvas, bob1);
				bob_obj2.Draw(canvas, bob1);

				holder.unlockCanvasAndPost(canvas);

				tick++;
			}
		}
	}
}
