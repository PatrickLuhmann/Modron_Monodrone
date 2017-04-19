package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;

import static java.lang.Math.abs;

public class FallingDown extends Activity {
	private final String dbgTag = this.getClass().getSimpleName();

	private ThreadedRenderView renderView;

	private int displayWidth, displayHeight;
	Rect scoreArea, playArea;
	Paint scorePaint, scoreAreaPaint, playAreaPaint;
	volatile boolean ready = false;
	int score = 0;
	StringBuilder scoreText;

	ArrayList<MyObject> objAll;

	MyObject ground, leftWall, rightWall, topWall;

	MyObject paddle;
	boolean movingPaddle;
	float paddlePrevX;
	float paddleRenderLastX;
	VelocityTracker velTracker = null;

	MyObject ball;

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

				objAll = new ArrayList<MyObject>();

				// Create the ground
				objAll.add(ground = new MyObject.Builder(playArea.width(), 100).posX(playArea.left).posY(displayHeight - 1).accel(100).background(Color.GREEN).build());
				objAll.add(topWall = new MyObject.Builder(playArea.width(), 100).posX(playArea.left).posY(playArea.top - 95).background(Color.GREEN).build());
				objAll.add(leftWall = new MyObject.Builder(100, playArea.height()).posX(playArea.left - 95).posY(playArea.top).background(Color.GREEN).build());
				objAll.add(rightWall = new MyObject.Builder(100, playArea.height()).posX(playArea.right - 5).posY(playArea.top).background(Color.GREEN).build());

				// Create the paddle.
				// Width is 20% of the play area and height is 5%.
				int paddleWidth = playArea.width() * 20 / 100;
				int paddleHeight = playArea.height() * 5 / 100;
				// Paddle starts in the middle at the bottom, but leave a small gap.
				int paddleX = playArea.centerX() - (paddleWidth / 2);
				int paddleY =  playArea.bottom - paddleHeight;
				MyDebug.Print(dbgTag, "  paddle: [" + paddleWidth + " , " + paddleHeight + "] @ (" + paddleX + " , " + paddleY + ").");
				paddle = new MyObject.Builder(paddleWidth, paddleHeight).posX(paddleX).posY(paddleY).background(Color.GRAY).build();
				objAll.add(paddle);
				movingPaddle = false;
				paddleRenderLastX = paddleX;

				// Create the ball.
				ball = new MyObject.Builder(30, 30)
					.background(Color.RED)
					.posX(displayWidth / 10).posY(playArea.top + 50)
					.velX(0).velY(0)
					.build();
				objAll.add(ball);

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
					paddle.updatePosition(deltaX, 0);

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

	// Return true if the view is to be refreshed. False will be returned
	// before the game state is ready, or if there wasn't a change.
	private boolean updateState(float deltaT) {
		if (!ready)
			return false;

		// Apply gravity to the ball. This changes the velocity of the ball.
		// The effects of this change will be seen when the position of the
		// ball is updated.
		ball.applyGravity(ground, deltaT);

		// Move the ball.
		ball.updatePosition(deltaT);

		//MyDebug.Print(dbgTag, "Paddle X: " + paddle.getX());

		// TODO: For collision detection, check for intersection of line described by
		// begin and end point of ball with the obstacle? This is one way to handle
		// the "tunnel-through" problem.

		// Check to see if the ball has encountered the left wall.
		if (ball.intersects(leftWall)) {
			MyDebug.Print(dbgTag, "The ball has hit the left wall.");

			// Just flip the direction of the x-axis.
			ball.scaleVelX(-1);

			// Put the ball on the right edge of the wall.
			ball.setX(leftWall.getX() + leftWall.getWidth());
		}

		// Check to see if the ball has encountered the right wall.
		if (ball.intersects(rightWall)) {
			MyDebug.Print(dbgTag, "The ball has hit the right wall.");

			// Just flip the direction of the x-axis.
			ball.scaleVelX(-1);

			// Put the ball on the left edge of the wall.
			ball.setX(rightWall.getX() - ball.getWidth());
		}

		// Check to see if the ball has encountered the top wall.
		if (ball.intersects(topWall)) {
			MyDebug.Print(dbgTag, "The ball has hit the top wall.");

			// Just flip the direction of the y-axis.
			ball.scaleVelY(-1);
		}

		// Check to see if the ball has encountered the paddle.
		if (ball.intersects(paddle)) {
			MyDebug.Print(dbgTag, "The ball and the paddle have intersected.");

			// What is the velocity of the paddle when it hits the ball?
			float velX;
			if (movingPaddle) {
				velTracker.computeCurrentVelocity(1000); // 1000 means px/sec.
				velX = velTracker.getXVelocity();
			}
			else {
				velX = 0;
			}
			MyDebug.Print(dbgTag, "  The paddle has a velocity of " + velX + " pixels per second.");

			float myVelCalc = paddleRenderLastX - paddle.getX();
			MyDebug.Print(dbgTag, "  Manual calc of paddle displacement: " + myVelCalc + " in " + deltaT + " seconds = " + myVelCalc * deltaT);

			// For the y-axis, just flip the direction/sign.
			ball.scaleVelY(-1);

			// For the x-axis, apply some of the velocity of the paddle to the ball.
			ball.changeVelX(velX / 10);

			// TODO: The ball is at least partially inside the paddle, which does not make sense.
			// Figure out how to handle this in a good way. Displacing it such that it is just
			// outside the paddle seems like it would be computationally difficult, especially
			// for objects that are not rectangles. Maybe there is a less accurate way that still
			// provides a good user experience.

			// Try giving the ball an additional time slice.
			ball.updatePosition(deltaT);

			// Player gets 1 point for successfully hitting the ball.
			score++;
		}

		// Check to see if the ball has hit the bottom.
		if (ball.pastY(playArea.bottom)) {
			MyDebug.Print(dbgTag, "The ball has hit the bottom.");
			ball.setX(displayWidth / 2);
			ball.setY(playArea.top + 50);
			ball.setVelX(0);
			ball.setVelY(0);
		}

		paddleRenderLastX = paddle.getX();

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

				// TODO: Update game state
				float deltaT = (System.nanoTime() - lastT) / 1000000000.0f;
				lastT = System.nanoTime();
				//TODO: If deltaT is too large, would it be possible to loop updateState
				// several times to provide smaller time-slices? This might be needed in
				// order to prevent a fast-moving object from "hopping over" a thin object.
				if (updateState(deltaT)) {
					// TODO: Draw game objAll
					Canvas canvas = holder.lockCanvas();

					canvas.drawRect(playArea, playAreaPaint);

					// TODO: Use a list of objects for this.
					// TODO: What about z-sorting?
					for (MyObject obj : objAll) {
						obj.Draw(canvas);
					}
					//ground.Draw(canvas);
					//paddle.Draw(canvas);
					//ball.Draw(canvas);
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
