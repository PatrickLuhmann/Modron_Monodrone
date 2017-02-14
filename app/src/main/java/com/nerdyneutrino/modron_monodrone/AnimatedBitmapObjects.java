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

class MyObject{
	private float pos_x;
	private float pos_y;
	private float vel_x;
	private float vel_y;
	private Bitmap skin;

	public static class Builder{
		private float pos_x;
		private float pos_y;
		private float vel_x = 0;
		private float vel_y = 0;
		private Bitmap skin;

		public Builder(float x, float y){
			this.pos_x = x;
			this.pos_y = y;
		}

		public Builder velX(float val){
			this.vel_x = val;
			return this;
		}

		public Builder velY(float val){
			this.vel_y = val;
			return this;
		}

		public Builder skin(Bitmap val) {
			this.skin = val;
			return this;
		}

		public MyObject build(){
			return new MyObject(this);
		}
	}

	private MyObject(Builder b){
		pos_x = b.pos_x;
		pos_y = b.pos_y;
		vel_x = b.vel_x;
		vel_y = b.vel_y;
		skin = b.skin;
	}

	void UpdatePosition(float deltaT){
		pos_x = pos_x + vel_x * deltaT;
		if (pos_x > 500)
			pos_x = 0;
		pos_y = pos_y + vel_y * deltaT;
		if (pos_y > 1000)
			pos_y = 0;
	}

	void Draw(Canvas canvas){
		canvas.drawBitmap(skin, pos_x, pos_y, null);
	}
}

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
		float v = 100; // px per second
		Bitmap bob1;
		MyObject bob_obj;
		MyObject bob_obj2;

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

			bob_obj = new MyObject.Builder(0, 0).velX(v).velY(0).skin(bob1).build();
			bob_obj2 = new MyObject.Builder(40, 40).velX(20).velY(20).skin(bob1).build();
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
