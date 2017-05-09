package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.Window;
import android.view.WindowManager;

import com.patrickluhmann.plobjectslibrary.PLObject;

import java.util.ArrayList;

public class FallingDown extends Activity {
	private final String dbgTag = this.getClass().getSimpleName();

	private ThreadedRenderView renderView;

	private int displayWidth, displayHeight;
	Rect scoreArea, playArea;
	Paint scorePaint, scoreAreaPaint, playAreaPaint;
	volatile boolean ready = false;
	int score = 0;
	StringBuilder scoreText;

	boolean movingPaddle;
	float paddlePrevX;
	VelocityTracker velTracker = null;

	PLObject ball;
	PLObject paddle;
	PLObject top, left, right;

	ArrayList<PLObject> gravitySources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		renderView = new FallingDown.ThreadedRenderView(this);
		setContentView(renderView);

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

				scoreAreaPaint = new Paint();
				scoreAreaPaint.setColor(Color.GRAY);

				scorePaint = new Paint();
				scorePaint.setColor(Color.MAGENTA);
				scorePaint.setTextSize(100);
				scoreText = new StringBuilder();

				playAreaPaint = new Paint();
				playAreaPaint.setColor(Color.WHITE);

				Bitmap b;
				Canvas c;

				// Create the paddle.
				// Width is 20% of the play area and height is 5%.
				int paddleWidth = playArea.width() * 20 / 100;
				int paddleHeight = playArea.height() * 5 / 100;
				// Paddle starts in the middle at the bottom, but leave a small gap.
				int paddleX = playArea.centerX() - (paddleWidth / 2);
				int paddleY =  playArea.bottom - paddleHeight;
				MyDebug.Print(dbgTag, "  paddle: [" + paddleWidth + " , " + paddleHeight + "] @ (" + paddleX + " , " + paddleY + ").");
				movingPaddle = false;

				// Create the paddle object.

				// First, we need a bitmap. Create one instead of loading one from file.
				b = Bitmap.createBitmap(paddleWidth, paddleHeight, Bitmap.Config.ARGB_8888);
				c = new Canvas(b);
				c.drawRGB(192, 192, 192);

				paddle = PLObject.builder(paddleWidth, paddleHeight)
					.skin(b)
					.posX(paddleX).posY(paddleY)
					.build();

				// Create the ball.

				// First, we need a bitmap. Create one instead of loading one from file.
				b = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
				c = new Canvas(b);
				c.drawRGB(255, 0, 0);

				ball = PLObject.builder(16, 16)
					.skin(b)
					.posX(playArea.left + 200).posY(playArea.top + 199)
					.velX(-400).velY(-400)
					.build();

				// Create the arena walls.

				// First, we need a bitmap. Create one instead of loading one from file.
				b = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
				c = new Canvas(b);
				c.drawRGB(0, 255, 128);

				top = PLObject.builder(playArea.width(), 16)
					.skin(b)
					.posX(playArea.left).posY(playArea.top)
					.build();

				left = PLObject.builder(16, playArea.height())
					.skin(b)
					.posX(playArea.left).posY(playArea.top + 16)
					.build();

				right = PLObject.builder(16, playArea.height())
					.skin(b)
					.posX(playArea.right - 16).posY(playArea.top + 16)
					.build();

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
		//MyDebug.Print(dbgTag, "onTouchEvent");

		switch (event.getAction()) {
			case (MotionEvent.ACTION_DOWN):
				MyDebug.Print(dbgTag, "  ACTION_DOWN.");
				if (paddle.contains(event.getX(), event.getY())) {
					MyDebug.Print(dbgTag, "  Selected the paddle.");
					movingPaddle = true;
					paddlePrevX = event.getX();
					if (velTracker == null) {
						velTracker = VelocityTracker.obtain();
					}
					else {
						velTracker.clear();
					}
					velTracker.addMovement(event);
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				//MyDebug.Print(dbgTag, "  ACTION_MOVE");
				if (movingPaddle) {
					// Determine how much the player has moved this time.
					float deltaX = event.getX() - paddlePrevX;

					// Move the paddle that amount.
					paddle.adjustPosition(deltaX, 0);

					// Save the new position of the gesture.
					paddlePrevX = event.getX();

					velTracker.addMovement(event);
					float velX;
					velTracker.computeCurrentVelocity(1000); // 1000 means px/sec.
					velX = velTracker.getXVelocity();
					//MyDebug.Print(dbgTag, "  The paddle has a velocity of " + velX + " pixels per second [" + deltaX + "].");
				}
				return true;
			case MotionEvent.ACTION_CANCEL:
				MyDebug.Print(dbgTag, "  ACTION_CANCEL");
			case MotionEvent.ACTION_UP:
				MyDebug.Print(dbgTag, "  ACTION_UP");
				if (movingPaddle) {
					movingPaddle = false;
					velTracker.recycle();
					velTracker = null;
				}
				return true;
			default:
				MyDebug.Print(dbgTag, "  Not caught, kicking up to super.");
				return super.onTouchEvent(event);
		}
	}

	private void applyGravity(PLObject tgtObj, float deltaT) {
		float deltaVY = 0;

		// For now, the only source of gravity is the ground, and it
		// is constant in the Y-axis only.
		deltaVY += (200 * deltaT);

		tgtObj.adjustVelocity(0, deltaVY);
	}

	// Return true if the view is to be refreshed. False will be returned
	// before the game state is ready, or if there wasn't a change.
	private boolean updateState(float deltaT) {
		if (!ready)
			return false;

		applyGravity(ball, deltaT);

		float[] tempers = new float[2];
		float remainT = deltaT;

		boolean wasCollision;
		int maxCollide = 10; // based on number of objects?
		do {
			wasCollision = false;
			maxCollide--;

			// Check for collision with the paddle.
			float collideT = ball.willCollide(paddle, remainT);
			if (collideT >= 0.0f) {
				MyDebug.Print(dbgTag, "Ball2 will collide with Paddle2");
				wasCollision = true;

				// The whole point of willCollide() returning the time-to-collision
				// is so that we can model the collision correctly. The way to do
				// this is to updatePosition based on this value, then update the
				// velocity based on the type of collision, then updatePosition
				// again with the remained of deltaT.
				ball.updatePosition(collideT);

				// This is simple bounce for test purposes
				ball.getVelocity(tempers);
				ball.setVelocity(tempers[0], tempers[1] * -1);

				remainT = remainT - collideT;

				// Player gets 1 point for successfully hitting the ball.
				score++;
			}

			// Check for collision with the top of the arena.
			collideT = ball.willCollide(top, remainT);
			if (collideT >= 0.0f) {
				MyDebug.Print(dbgTag, "Ball2 will collide with the top of the arena.");
				wasCollision = true;
				ball.updatePosition(collideT);

				// This is simple bounce for test purposes
				ball.getVelocity(tempers);
				ball.setVelocity(tempers[0], tempers[1] * -1);

				remainT = remainT - collideT;
			}

			// Check for collision with the left side of the arena.
			collideT = ball.willCollide(left, remainT);
			if (collideT >= 0.0f) {
				MyDebug.Print(dbgTag, "Ball2 will collide with the left side of the arena.");
				wasCollision = true;
				ball.updatePosition(collideT);

				// This is simple bounce for test purposes
				ball.getVelocity(tempers);
				ball.setVelocity(tempers[0] * -1, tempers[1]);

				remainT = remainT - collideT;
			}

			// Check for collision with the right side of the arena.
			collideT = ball.willCollide(right, remainT);
			if (collideT >= 0.0f) {
				MyDebug.Print(dbgTag, "Ball2 will collide with the right side of the arena.");
				wasCollision = true;
				ball.updatePosition(collideT);

				// This is simple bounce for test purposes
				ball.getVelocity(tempers);
				ball.setVelocity(tempers[0] * -1, tempers[1]);

				remainT = remainT - collideT;
			}
		} while (wasCollision && (maxCollide > 0));

		// Finale update
		ball.updatePosition(remainT);

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
			long lastT = System.nanoTime();
			while (running) {
				if (!holder.getSurface().isValid())
					continue;

				float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
				lastT = System.nanoTime();

				if (updateState(deltaT)) {
					Canvas canvas = holder.lockCanvas();

					canvas.drawRect(playArea, playAreaPaint);

					// TODO: Use a list of objects for this.
					// TODO: What about z-sorting?

					top.draw(canvas);
					left.draw(canvas);
					right.draw(canvas);
					ball.draw(canvas);
					paddle.draw(canvas);
					canvas.drawRect(scoreArea, scoreAreaPaint);
					scoreText.setLength(0);
					scoreText.append("Score: ");
					scoreText.append(score);
					canvas.drawText(scoreText.toString(), scoreArea.left + 10, scoreArea.bottom - 10, scorePaint);

					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}
