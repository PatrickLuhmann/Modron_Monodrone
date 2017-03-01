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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class Rectangles extends Activity implements View.OnTouchListener {
	ThreadedRenderView rview;

	/*
	float xvt = -0.79f;
	float yvt = 0.09f;
	float xvb = -0.75f;
	float yvb = 0.05f;
	*/
	double centerX = -0.5f;
	double centerY = 0.0f;
	double rad = 1.0f;

	double xvt = -1.0f;
	double yvt = 1.0f;
	double xvb = -0.2f;
	double yvb = 0.2f;
	double xstep, ystep;
	double zoom = 0.5f;

	int xst = 1000;
	int yst = 1000;

	boolean complete = false;
	boolean redraw = false;

	Bitmap b;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		rview = new ThreadedRenderView(this);
		rview.setOnTouchListener(this);
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

	static public int compute2(double x0, double y0, int max_i) {
		double x = 0, y = 0;
		int i = 0;
		while (((x * x + y * y) < 16.0f) && i < max_i) {
			double x_temp = x * x - y * y + x0;
			y = 2 * x * y + y0;
			x = x_temp;
			i++;
		}
		return i;
	}

	// Returns the number of iterations, not a Color.
	public int compute(int x, int y) {
		double xv = xvt + x * xstep;
		double yv = yvt + y * ystep;
		int max = 257;
		int ii = compute2(xv, yv, max);
		if (ii == max)
			ii = 0;
		return ii;
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
			myRed.setStyle(Paint.Style.FILL_AND_STROKE);
		}

		public void resume() {
			MyDebug.Print("Rectangles", "resume()");
			running = true;
			// Tell the thread to redraw the bitmap, if it is complete.
			if (complete)
				redraw = true;
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

		public void createColorPalette(int[] palette) {
			int idx = 0;
			palette[idx++] = 0xFF0000AA;
			palette[idx++] = 0xFF00AAAA; // cyan?
			palette[idx++] = 0xFF00AA00;
			palette[idx++] = 0xFFAAAA00; // yellow?
			palette[idx++] = 0xFFAA0000;
			palette[idx++] = 0xFFAA00AA; // magenta?
			palette[idx++] = 0xFFAAAAAA;
			palette[idx++] = 0xFF000055;

		}

		public void run() {
			Paint p = new Paint();
			int[] colorPalette = new int[8];
			createColorPalette(colorPalette);

			while (running) {
				if (complete) {
					if (redraw) {
						MyDebug.Print("Rectangles:run", "Redrawing bitmap as it is already complete.");
						// For a redraw, use a green border.
						p.setColor(Color.GREEN);
						p.setStyle(Paint.Style.FILL_AND_STROKE);
					}
				}
				else {
					long lastT = System.nanoTime();

					// Create the base bitmap
					b = Bitmap.createBitmap(xst + 1, yst + 1, Bitmap.Config.ARGB_8888);
					Canvas c = new Canvas(b);
					c.drawRGB(0, 0, 0);
					p.setStrokeWidth(0);

					// Calculate the corners of the view
					xvt = centerX - rad;
					yvt = centerY + rad;
					xvb = centerX + rad;
					yvb = centerY - rad;
					xstep = (xvb - xvt) / (xst);
					ystep = (yvb - yvt) / (yst);
					MyDebug.Print("Rectangles:run", "This view is (" + xvt + " , " + yvt + ") to (" + xvb + " , " + yvb + ")");

					int x = 0, y = 0;
					for (y = 0; y <= yst; y++) {
						if (!running)
							break;

						for (x = 0; x <= xst; x++) {
							if (!running)
								break;

							int steps = compute(x, y);
							if (steps == 0)
								p.setColor(Color.BLACK);
							else
								p.setColor(colorPalette[(steps - 1) & (colorPalette.length - 1)]);
							c.drawPoint(x, y, p);
						}
					}
					if (y > yst && x > xst) {
						complete = true;
						redraw = true;
						// For the first draw, use a blue border.
						p.setColor(Color.BLUE);
						p.setStyle(Paint.Style.FILL_AND_STROKE);
						MyDebug.Print("Rectangles:run", "Image creation is complete.");
					} else {
						MyDebug.Print("Rectangles:run", "Image creation interrupted.");
					}

					float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
					MyDebug.Print("Rectangles:run", "Fractal draw time: " + deltaT + " s");
				}

				if (redraw && holder.getSurface().isValid()) {
					Canvas canvas = holder.lockCanvas();
					// Put a border around the bitmap, just to show where it is.
					canvas.drawRect(0, 0, 3 + xst + 1 + 2, 3 + yst + 1 + 2, p);
					canvas.drawBitmap(b, 3, 3, null);
					holder.unlockCanvasAndPost(canvas);
					redraw = false;
				}
			}
		}
	}

	public boolean onTouch(View v, MotionEvent me) {
		float pointX = me.getX();
		float pointY = me.getY();
		int act = me.getAction();
		switch (act) {
			case MotionEvent.ACTION_DOWN:
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_DOWN @ " + pointX + " , " + pointY);

				// Determine whether click is inside or outside bitmap.
				if (pointX >= 3 && pointY >= 3 && pointX <= (3 + xst) && pointY <= (3 + yst)) {
					// Normalize point relative to bitmap
					double xv = (xvt + (pointX - 3) * xstep);
					double yv = (yvt + (pointY - 3) * ystep);
					MyDebug.Print(this.getClass().getSimpleName(), "  View point selected: (" + xv + " , " + yv + ")");

					centerX = xv;
					centerY = yv;
				}
				else
					rad *= zoom;

				complete = false;
				redraw = false;

				break;
			case MotionEvent.ACTION_MOVE:
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_MOVE @ " + pointX + " , " + pointY);

				break;
			case MotionEvent.ACTION_CANCEL:
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_CANCEL @ " + pointX + " , " + pointY);
				break;
			case MotionEvent.ACTION_UP:
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_UP @ " + pointX + " , " + pointY);

				break;
			default:
				MyDebug.Print(this.getClass().getSimpleName(), "Unhandled action " + act);
				break;
		}

		return true;
	}
}
