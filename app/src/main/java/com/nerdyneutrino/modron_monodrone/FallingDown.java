package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

	private int displayWidth, displayHeight;
	Rect scoreArea, playArea;
	Paint scoreAreaPaint, playAreaPaint;
	volatile boolean ready = false;

	// Game objects
	MyObject paddle;
	boolean movingPaddle;
	float paddlePrevX;

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

				playAreaPaint = new Paint();
				playAreaPaint.setColor(Color.WHITE);

				// Create the paddle.
				// Width is 20% of the play area and height is 5%.
				int paddleWidth = playArea.width() * 20 / 100;
				int paddleHeight = playArea.height() * 5 / 100;
				// Paddle starts in the middle at the bottom, but leave a small gap.
				int paddleX = playArea.centerX() - (paddleWidth / 2);
				int paddleY =  playArea.bottom - paddleHeight;
				MyDebug.Print(dbgTag, "  paddle: [" + paddleWidth + " , " + paddleHeight + "] @ (" + paddleX + " , " + paddleY + ").");
				paddle = new MyObject.Builder(paddleWidth, paddleHeight).posX(paddleX).posY(paddleY).background(Color.GRAY).build();
				movingPaddle = false;

				// Create the ball.
				ball = new MyObject.Builder(30, 30)
					.background(Color.RED)
					.posX(displayWidth / 10).posY(playArea.top)
					.velX(0).velY(displayHeight / 3)
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
		MyDebug.Print(dbgTag, "onTouchEvent");

		switch (event.getAction()) {
			case (MotionEvent.ACTION_DOWN):
				MyDebug.Print(dbgTag, "  ACTION_DOWN.");
				if (paddle.contains(event.getX(), event.getY())) {
					MyDebug.Print(dbgTag, "  Selected the paddle.");
					movingPaddle = true;
					paddlePrevX = event.getX();
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				MyDebug.Print(dbgTag, "  ACTION_MOVE");
				if (movingPaddle) {
					// Determine how much the player has moved this time.
					float deltaX = event.getX() - paddlePrevX;

					// Move the paddle that amount.
					paddle.updatePosition(deltaX, 0);

					// Save the new position of the gesture.
					paddlePrevX = event.getX();
				}
				return true;
			case MotionEvent.ACTION_UP:
				MyDebug.Print(dbgTag, "  ACTION_UP");
				movingPaddle = false;
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

		// Move the ball.
		ball.updatePosition(deltaT);

		// Check to see if the ball has encountered the paddle.
		if (ball.intersects(paddle)) {
			MyDebug.Print(dbgTag, "The ball and the paddle have intersected.");
			ball.setY(playArea.top);
		}
		// Check to see if the ball has hit the bottom.
		if (ball.pastY(playArea.bottom)) {
			MyDebug.Print(dbgTag, "The ball has hit the bottom.");
			ball.setX(displayWidth / 2);
			ball.setY(playArea.top + 50);
			ball.setVelX(0);
			ball.setVelY(0);
		}
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
				if (updateState(deltaT)) {
					// TODO: Draw game objects
					Canvas canvas = holder.lockCanvas();

					canvas.drawRect(scoreArea, scoreAreaPaint);
					canvas.drawRect(playArea, playAreaPaint);
					paddle.Draw(canvas);

					ball.Draw(canvas);

					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
}
