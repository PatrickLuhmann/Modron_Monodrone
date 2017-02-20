package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

	class ThreadedRenderView extends SurfaceView implements Runnable {
		Thread renderThread = null;
		SurfaceHolder holder;
		volatile boolean running = false;
		volatile int tick = 0;
		Bitmap bob1, bmBlueQueen;
		MyObject bob_obj;
		MyObject bob_obj2;
		MyObject blue_queen;

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
				is = am.open("sample_blue_queen.png");
				bmBlueQueen = BitmapFactory.decodeStream(is);
				is.close();
				MyDebug.Print("AnimatedBitmapObjects", "sample_blue_queen.png format: " + bmBlueQueen.getConfig());
			} catch (IOException e) {
				// I don't know what to do here.
				MyDebug.Print("AnimatedBitmapObjects", "Caught an exception while trying to load my bitmap file.");
			} finally {
				// Close input streams, I guess.
			}

			bob_obj = new MyObject.Builder(0, 0).velX(100).velY(150).skin(bob1).scale(0.5f).build();
			bob_obj2 = new MyObject.Builder(40, 40).velX(20).velY(20).skin(bob1).scale(1.0f).build();
			blue_queen = new MyObject.Builder(0, 100).velX(5).velY(1).skin(bmBlueQueen).scale(0.25f).build();
		}

		public void resume() {
			running = true;
			renderThread = new Thread(this);
			renderThread.start();
		}

		public void pause() {
			running = false;
			while (true) {
				try {
					renderThread.join();
					return;
				} catch (InterruptedException e) {
					// retry
				}
			}
		}

		public void run() {
			int rightEdge = 1000;
			int bottomEdge = 1900;

			long lastT = System.nanoTime();
			while (running) {
				if (!holder.getSurface().isValid())
					continue;

				float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
				lastT = System.nanoTime();
				//MyDebug.Print("AnimatedBitmapObjects:run", "Delta Time: " + deltaT);

				MyDebug.Print("AnimatedBitmapObjects", "Surface width: " + holder.getSurfaceFrame().width());
				MyDebug.Print("AnimatedBitmapObjects", "Surface height: " + holder.getSurfaceFrame().height());

				// Determine the new positions
				bob_obj.UpdatePosition(deltaT);
				if (bob_obj.pastX(holder.getSurfaceFrame().width())){
					bob_obj.setX(0);
				}
				if (bob_obj.pastY(holder.getSurfaceFrame().height())){
					bob_obj.setY(0);
				}

				bob_obj2.UpdatePosition(deltaT);
				if (bob_obj2.pastX(holder.getSurfaceFrame().width())){
					bob_obj2.setX(0);
				}
				if (bob_obj2.pastY(holder.getSurfaceFrame().height())){
					bob_obj2.setY(0);
				}

				blue_queen.UpdatePosition(deltaT);
				if (blue_queen.pastX(holder.getSurfaceFrame().width())){
					blue_queen.setX(0);
				}
				if (blue_queen.pastY(holder.getSurfaceFrame().height())){
					blue_queen.setY(0);
				}

				Canvas canvas = holder.lockCanvas();

				// Paint it black
				Paint paint = new Paint();
				paint.setColor(Color.WHITE);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawRGB(0, 0, 0);
				canvas.drawRect(0, 0, holder.getSurfaceFrame().width() - 1, holder.getSurfaceFrame().height() - 1, paint);
				canvas.drawRect(1, 1, holder.getSurfaceFrame().width() - 2, holder.getSurfaceFrame().height() - 2, paint);

				// Display the bitmaps
				bob_obj.Draw(canvas);
				bob_obj2.Draw(canvas);
				blue_queen.Draw(canvas);

				holder.unlockCanvasAndPost(canvas);

				tick++;
			}
		}
	}
}
