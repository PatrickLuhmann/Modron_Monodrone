package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;

public class Swipe extends Activity {
	private final String dbgTag = this.getClass().getSimpleName();

	SwipeRenderView rview;
	private int displayWidth, displayHeight;
	private GestureDetectorCompat mDetector;
	float prevX, prevY;
	private Rect handDisplayArea;
	Paint handDisplayPaint;
	int[] handDisplayColors = new int[] {Color.GRAY, Color.CYAN, Color.RED, Color.WHITE};
	int handDisplayColorIdx = 0;

	ArrayList<MyObject> objects = new ArrayList<MyObject>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		rview = new SwipeRenderView(this);
		//rview.setOnTouchListener(this);
		setContentView(rview);
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());

		handDisplayPaint = new Paint();
		handDisplayPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		handDisplayPaint.setStrokeWidth(0);

		// The hand is displayed at the bottom of the screen.
		// Use the bottom 25% of the screen.
		// TODO: Make this generic for any screen size.
		rview.post(new Runnable() {
			@Override
			public void run() {
				displayWidth = rview.getMeasuredWidth();
				displayHeight = rview.getMeasuredHeight();
				MyDebug.Print(dbgTag, "post-run:View measured width: " + displayWidth);
				MyDebug.Print(dbgTag, "post-run:View measured height: " + displayHeight);
				// NOTE: Right/Bottom coord is exclusive, not inclusive.
				handDisplayArea = new Rect(0, displayHeight * 75 / 100, displayWidth, displayHeight);
			}
		});
	}

	// The sample for gestures does not include this function, but I needed
	// to put it here in order to get it to work. Without it the detector
	// methods never get called.
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mDetector.onTouchEvent(event);
		// TODO: Do I need to do super.onTouchEvent(event) here?
	}

	class SwipeRenderView extends View {
		public SwipeRenderView(Context context) {
			super(context);

		}

		protected void onDraw(Canvas canvas) {
			MyDebug.Print(dbgTag, "Canvas width: " + canvas.getWidth());
			MyDebug.Print(dbgTag, "Canvas height: " + canvas.getHeight());
			MyDebug.Print(dbgTag, "Display width: " + displayWidth);
			MyDebug.Print(dbgTag, "Display height: " + displayHeight);
			canvas.drawRGB(0, 200, 0);
			handDisplayPaint.setColor(handDisplayColors[handDisplayColorIdx]);
			canvas.drawRect(handDisplayArea, handDisplayPaint);

			// Draw the objects
			for (MyObject obj : objects) {
				obj.Draw(canvas);
			}

			// Only need invalidate() if we are trying to animate?
			//invalidate();
		}
	}

	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		// BKM: Always implement onDown, and always return true.
		@Override
		public boolean onDown(MotionEvent event) {
			MyDebug.Print(dbgTag, "onDown: " + event.toString());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velX, float velY) {
			MyDebug.Print(dbgTag, "onFling: [" + velX + " , " + velY + "] ; " + event1.toString() + " ; " + event2.toString());

			// Is the fling within the hand display area?
			if (!handDisplayArea.contains((int)event1.getX(), (int)event1.getY()) &&
				!handDisplayArea.contains((int)event2.getX(), (int)event2.getY())) {
				MyDebug.Print(dbgTag, "onFling:   This is not within the hand display area.");
				return false;
			}

			// Is the fling going "mostly" left or right?
			float deltaX = event2.getX() - event1.getX();
			float deltaY = event2.getY() - event1.getY();
			MyDebug.Print(dbgTag, "onFling:   deltaX = " + deltaX + " , deltaY = " + deltaY);
			// NOTE: I got these threshold numbers from an example online. They might not be
			// the best values. I also do not think they take screen resolution into account.
			// There might be a better solution using the velocity. On my phone I noticed that
			// the X velocity was orders of magnitude larger than the Y velocity, so the ratio
			// might give the direction of the swipe.
			if (deltaX > 250 && Math.abs(deltaY) < 120) {
				MyDebug.Print(dbgTag, "onFling:   This looks like a swipe to the right.");
				handDisplayColorIdx++;
				if (handDisplayColorIdx >= handDisplayColors.length)
					handDisplayColorIdx = 0;
				rview.invalidate();
			}
			else if (deltaX < -250 && Math.abs(deltaY) < 120) {
				MyDebug.Print(dbgTag, "onFling:   This looks like a swipe to the left.");
				handDisplayColorIdx--;
				if (handDisplayColorIdx < 0)
					handDisplayColorIdx = handDisplayColors.length - 1;
				rview.invalidate();
			}

			return true;
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2, float distX, float distY) {
			//MyDebug.Print(dbgTag, "onScroll: [" + distX + " , " + distY + "] ; " + event1.toString() + " ; " + event2.toString());
			return true;
		}

		// onLongPress
		// onShowPress
		// onSingleTapUp
		// onDoubleTap
		// onDoubleTapEvent
		// onSingleTapConfirmed
	}

	public boolean onTouchxxx(View v, MotionEvent event) {
		float pointX = event.getX();
		float pointY = event.getY();
		int act = MotionEventCompat.getActionMasked(event);
		switch (act) {
			case MotionEvent.ACTION_DOWN:
				MyDebug.Print(dbgTag, "ACTION_DOWN @ " + pointX + " , " + pointY);

				// If an object is selected, tell it so.
				for (MyObject obj : objects) {
					if (obj.contains(pointX, pointY))
						obj.setSelected();
				}

				// Remember the location in case this becomes a move
				prevX = pointX;
				prevY = pointY;

				break;
			case MotionEvent.ACTION_MOVE:
				MyDebug.Print(dbgTag, "ACTION_MOVE @ " + pointX + " , " + pointY);

				// If an object is selected, update its position. At this point, only
				// one object may be selected at a time so we can break out of the loop
				// early.
				for (MyObject obj : objects) {
					if (obj.isSelected()) {
						float deltaX = pointX - prevX;
						float deltaY = pointY - prevY;
						obj.updatePosition(deltaX, deltaY);
						break;
					}
				}

				// Remember the location in case this move continues
				prevX = pointX;
				prevY = pointY;

				break;
			case MotionEvent.ACTION_CANCEL:
				MyDebug.Print(dbgTag, "ACTION_CANCEL @ " + pointX + " , " + pointY);
				break;
			case MotionEvent.ACTION_OUTSIDE:
				MyDebug.Print(dbgTag, "ACTION_OUTSIDE @ " + pointX + " , " + pointY);
				break;
			case MotionEvent.ACTION_UP:
				MyDebug.Print(dbgTag, "ACTION_UP @ " + pointX + " , " + pointY);

				// Unselect all objects
				for (MyObject obj : objects) {
					obj.setUnselected();
				}

				break;
			default:
				MyDebug.Print(dbgTag, "Unhandled action " + act);
				// return super.onTouchEvent(event); // what does this do?
				break;
		}

		// Trigger redraw of view
		rview.invalidate();

		return true;
	}
}
