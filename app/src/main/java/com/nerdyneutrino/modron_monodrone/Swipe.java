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
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Swipe extends Activity {
	private final String dbgTag = this.getClass().getSimpleName();

	SwipeRenderView rview;
	Context myContext;
	private int displayWidth, displayHeight;
	private GestureDetectorCompat mDetector;
	float prevX, prevY;
	private Rect handDisplayArea;
	Paint handDisplayPaint;
	int[] handDisplayColors = new int[] {Color.GRAY, Color.CYAN, Color.RED, Color.WHITE};
	int handDisplayColorIdx = 0;
	int handDisplayFirstHandIdx = 0;

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
			myContext = context;

			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("sample_blue_queen.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("bobargb8888.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("sample_blue_queen.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("bobargb8888.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("sample_blue_queen.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("bobargb8888.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("sample_blue_queen.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("bobargb8888.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("sample_blue_queen.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("bobargb8888.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("sample_blue_queen.png")).build());
			objects.add(new MyObject.Builder(193, 270).skin(LoadBitmap("bobargb8888.png")).build());
		}

		private void drawPlayerHand1(Canvas canvas) {
			handDisplayPaint.setColor(handDisplayColors[handDisplayColorIdx]);
			canvas.drawRect(handDisplayArea, handDisplayPaint);

			if (objects.size() == 0)
				return;

			int targetCardHeight = handDisplayArea.height() * 90 / 100;
			int marginVert = (handDisplayArea.height() - targetCardHeight) / 2;

			// We need the card width now, but the only way to get it is to scale
			// a card and see the result. Do that on the first card for this purpose.
			MyObject obj = objects.get(0);
			obj.setHeight(targetCardHeight, true);
			int targetCardWidth = obj.getWidth();

			// Draw the cards of the hand that are currently visible.
			int handDisplayHandWidth = (handDisplayArea.width() - marginVert) / (targetCardWidth + marginVert);
			MyDebug.Print(dbgTag, "Display hand width: " + handDisplayHandWidth);

			int marginHoriz = (handDisplayArea.width() - handDisplayHandWidth * targetCardWidth) / (handDisplayHandWidth + 1);
			MyDebug.Print(dbgTag, "Horizontal margin: " + marginHoriz);

			for (int i = 0; i < handDisplayHandWidth; i++) {
				int idx = handDisplayFirstHandIdx + i;
				if (idx < objects.size()) {
					int posX = marginHoriz + i * (targetCardWidth + marginHoriz);
					int posY = handDisplayArea.top + marginVert;
					obj = objects.get(idx);
					// Scale the card to fit the draw area
					obj.setHeight(targetCardHeight, true);
					obj.setX(posX);
					obj.setY(posY);
					obj.Draw(canvas);
				}
			}
		}

		protected void onDraw(Canvas canvas) {
			MyDebug.Print(dbgTag, "Canvas width: " + canvas.getWidth());
			MyDebug.Print(dbgTag, "Canvas height: " + canvas.getHeight());
			MyDebug.Print(dbgTag, "Display width: " + displayWidth);
			MyDebug.Print(dbgTag, "Display height: " + displayHeight);

			// Draw the background sections
			canvas.drawRGB(0, 200, 0);

			// Draw the hand for example 1.
			drawPlayerHand1(canvas);

			// Draw the objects
//			for (MyObject obj : objects) {
//				obj.Draw(canvas);
//			}
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

				handDisplayFirstHandIdx--;
				if (handDisplayFirstHandIdx < 0)
					handDisplayFirstHandIdx = 0;

				rview.invalidate();
			}
			else if (deltaX < -250 && Math.abs(deltaY) < 120) {
				MyDebug.Print(dbgTag, "onFling:   This looks like a swipe to the left.");
				handDisplayColorIdx--;
				if (handDisplayColorIdx < 0)
					handDisplayColorIdx = handDisplayColors.length - 1;

				handDisplayFirstHandIdx++;
				if (handDisplayFirstHandIdx >= objects.size())
					handDisplayFirstHandIdx = objects.size() - 1;

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

	// Load a bitmap from a file.
	private Bitmap LoadBitmap(String filename) {
		Bitmap bitmap = null;
		// Grab a bitmap file
		try {
			AssetManager am = myContext.getAssets();
			InputStream is = am.open(filename);
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			MyDebug.Print(dbgTag, filename + " format: " + bitmap.getConfig());
		} catch (IOException e) {
			// I don't know what to do here.
			MyDebug.Print(dbgTag, "Caught an exception while trying to load bitmap file " + filename + ".");
		} finally {
			// Close input streams, I guess.
		}

		return bitmap;
	}
}
