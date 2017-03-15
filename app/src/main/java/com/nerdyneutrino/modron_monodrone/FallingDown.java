package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class FallingDown extends Activity {
	private final String dbgTag = this.getClass().getSimpleName();

	private ThreadedRenderView renderView;
	private GestureDetectorCompat gestureDetector;

	private int displayWidth, displayHeight;
	Rect scoreArea, playArea;
	volatile boolean ready = false;

	// Game objects
	MyObject paddle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		renderView = new FallingDown.ThreadedRenderView(this);
		setContentView(renderView);
		gestureDetector = new GestureDetectorCompat(this, new FallingDown.MyGestureListener());

		// Determine the size of the graphical display we have to work with.
		renderView.post(new Runnable() {
			@Override
			public void run() {
				displayWidth = renderView.getMeasuredWidth();
				displayHeight = renderView.getMeasuredHeight();
				MyDebug.Print(dbgTag, "post-run:View measured width: " + displayWidth);
				MyDebug.Print(dbgTag, "post-run:View measured height: " + displayHeight);

				scoreArea = new Rect(0, 0, displayWidth, displayHeight * 15 / 100);
				playArea = new Rect(0, displayHeight * 15 / 100, displayWidth, displayHeight);

				// Create the paddle.
				// Width is 20% of the play area and height is 5%.
				int paddleWidth = playArea.width() * 20 / 100;
				int paddleHeight = playArea.height() * 5 / 100;
				// Paddle starts in the middle at the bottom, but leave a small gap.
				int paddleX = playArea.centerX() - (paddleWidth / 2);
				int paddleY =  playArea.bottom - paddleHeight;
				MyDebug.Print(dbgTag, "  paddle: [" + paddleWidth + " , " + paddleHeight + "] @ (" + paddleX + " , " + paddleY + ").");
				paddle = new MyObject.Builder(paddleWidth, paddleHeight).posX(paddleX).posY(paddleY).background(Color.GRAY).build();

				ready = true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		renderView.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		renderView.pause();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
		// TODO: Do I need to do super.onTouchEvent(event) here?
	}

	// Return true if the view is to be refreshed. False will be returned
	// before the game state is ready, or if there wasn't a change.
	private boolean updateState() {
		if (!ready)
			return false;

		return true;
	}

	class ThreadedRenderView extends SurfaceView implements Runnable {
		volatile boolean running = false;
		Thread renderThread = null;
		SurfaceHolder holder;

		public ThreadedRenderView(Context context) {
			super(context);
			MyDebug.Print(dbgTag, "ThreadedRenderView constructor.");
			holder = getHolder();
		}

		public void resume() {
			MyDebug.Print(dbgTag, "ThreadedRenderView resume().");
			running = true;
			renderThread = new Thread(this);
			renderThread.start();
		}

		public void pause() {
			MyDebug.Print(dbgTag, "ThreadedRenderView pause().");
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

		// This method is invoked once when the Thread.start() method is called.
		// This will happen every time the View is resumed. This method will
		// exit when the View is paused, because running will be set to false.
		public void run() {
			// Thread execution loop.
			while (running) {
				if (!holder.getSurface().isValid())
					continue;

				// TODO: Update game state
				if (updateState()) {
					// TODO: Draw game objects
					Canvas canvas = holder.lockCanvas();
					paddle.Draw(canvas);
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		// BKM: Always implement onDown, and always return true.
		@Override
		public boolean onDown(MotionEvent event) {
			MyDebug.Print(dbgTag, "onDown: " + event.toString());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velX, float velY) {
			MyDebug.Print(dbgTag, "onFling: [" + velX + " , " + velY + "] ; " + event1.toString() + " ; " + event2.toString());
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2, float distX, float distY) {
			MyDebug.Print(dbgTag, "onScroll: [" + distX + " , " + distY + "] ; " + event1.toString() + " ; " + event2.toString());
			return true;
		}

		// onLongPress
		// onShowPress
		// onSingleTapUp
		// onDoubleTap
		// onDoubleTapEvent
		// onSingleTapConfirmed
	}
}
