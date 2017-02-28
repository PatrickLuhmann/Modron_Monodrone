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

	/*
	float xvt = -0.79f;
	float yvt = 0.09f;
	float xvb = -0.75f;
	float yvb = 0.05f;
	*/
	float centerX = 0.0f;
	float centerY = 0.0f;
	float rad = 1.0f;

	float xvt = -1.0f;
	float yvt = 1.0f;
	float xvb = -0.2f;
	float yvb = 0.2f;
	float xstep, ystep;
	float zoom = 0.01f;

	int xst = 500;
	int yst = 500;

	boolean complete = false;

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

	static public int returnOne() {
		return 1;
	}

	public int returnTwo() {
		return 2;
	}

	static public int compute2(float x0, float y0, int max_i) {
		float x = 0, y = 0;
		int i = 0;
		//int max_i = 100;
		while (((x * x + y * y) < 4.0f) && i < max_i) {
			float x_temp = x * x - y * y + x0;
			y = 2 * x * y + y0;
			x = x_temp;
			i++;
		}
		return i;
	}

	public int compute(int x, int y) {
		float xv = xvt + x * xstep;
		float yv = yvt + y * ystep;
		int max = 64 * 3;
		int ii = compute2(xv, yv, max);
		int color;
		if (ii == max)
			color = 0;
		else {
			int c = ii / 64; // 0, 1, 2
			int off = ii % 64;
			color = (off + 192) << (8 * c);
			ii = ii + 256 - max;
		}
		//color = 0xFFFFFF & (ii | (ii << 8) | (ii << 16));
		color |= 0xFF000000;

		return color;
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
			long lastT = System.nanoTime();
			while (running) {
				if (!holder.getSurface().isValid())
					continue;

				float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
				lastT = System.nanoTime();
				MyDebug.Print("Rectangles:run", "Delta Time: " + deltaT);
				MyDebug.Print("Rectangles:run", "This view is (" + xvt + " , " + yvt + ") to (" + xvb + " , " + yvb + ")");

				Bitmap b = Bitmap.createBitmap(xst + 1, yst + 1, Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b);
				c.drawRGB(0, 0, 0);
				

				Paint p = new Paint();
				p.setColor(Color.WHITE);

				Canvas canvas = holder.lockCanvas();

				xstep = (xvb - xvt) / (xst);
				ystep = (yvb - yvt) / (yst);
				for (int y = 0; y <= yst; y++) {
					if (!running)
						break;
					for (int x = 0; x <= xst; x++) {
						if (!running) {
							//MyDebug.Print("Rectangles:run", "This activity is not running any more.");
							break;
						}

						int color = compute(x, y);
						//MyDebug.Print("Rectangles:run", "(" + x + " , " + y + ") => " + color);
						/*
						if (color != 100) {
							int mask;
							if (color <= 25) {
								mask = color * 0xFF / 25;
								mask = mask | (mask << 8) | (mask << 16) | (0xFF << 24);
								p.setColor(Color.GREEN & mask);
							}
							else if (color <= 50) {
								mask = (color - 25) * 0xFF / 25;
								mask = mask | (mask << 8) | (mask << 16) | (0xFF << 24);
								p.setColor(Color.BLUE & mask);
							}
							else if (color <= 75) {
								mask = (color - 50) * 0xFF / 25;
								mask = mask | (mask << 8) | (mask << 16) | (0xFF << 24);
								p.setColor(Color.RED);
							}
							else {
								mask = (color - 75) * 0xFF / 25;
								mask = mask | (mask << 8) | (mask << 16) | (0xFF << 24);
								p.setColor(Color.WHITE);
							}
							//p.setColor(mask);
							canvas.drawPoint(x, y, p);
						}
						*/
						p.setColor(color);
						canvas.drawPoint(x, y, p);
					}
				}
				MyDebug.Print("Rectangles:run", "Done with loops.");

				holder.unlockCanvasAndPost(canvas);
				xvt += zoom;
				yvt -= zoom;
				xvb -= zoom;
				yvb += zoom;
			}
		}
	}
}
