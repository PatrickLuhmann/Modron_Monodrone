package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class FallingDown extends Activity {
	private final String dbgTag = this.getClass().getSimpleName();

	private ThreadedRenderView renderView;
	private GestureDetectorCompat gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		renderView = new FallingDown.ThreadedRenderView(this);
		setContentView(renderView);
		gestureDetector = new GestureDetectorCompat(this, new FallingDown.MyGestureListener());
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
	class ThreadedRenderView extends SurfaceView implements Runnable {
		volatile boolean running = false;
		Thread renderThread = null;

		public ThreadedRenderView(Context context) {
			super(context);
			MyDebug.Print(dbgTag, "ThreadedRenderView constructor.");
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

		// This method is invoked once when the Thread.start() method is called.
		// This will happen every time the View is resumed. This method will
		// exit when the View is paused, because running will be set to false.
		public void run() {
			// Thread execution loop.
			while (running) {
				// TODO: Do something interesting here.
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
