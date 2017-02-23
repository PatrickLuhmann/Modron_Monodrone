package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PlayDeck1 extends Activity implements View.OnTouchListener {

	/*
	  Plan is to eventually have a deck of cards, several up cards, a discard
	  pile and a player's hand. The user will select a card by tapping it; it will
	  automatically move to his hand. The user can discard a card by dragging it to
	  the discard pile. Maybe allow dragging cards to hand in addition to tapping.
	  Maybe allow player to rearrange cards in hand. Maybe allow user to view
	  the discard pile.
	  Visually, the board is in two rows. On the top row, from left to right,
	  is the discard pile (have an "empty" bitmap at first), the draw pile,
	  and then three up cards. On the bottom row is the players cards, arranged in a row.
	  Not sure if there will be a card limit or not. If space permits, have them each
	  separate, without any overlap. Otherwise will need some amount of overlap. If that
	  is the case then selection will be more difficult. Maybe expand a card when it
	  is selected and display it on top so that it can be easily moved. Or, always
	  have the center card enlarged and on top and allow the player to swipe in either
	  direction to rotate the hand, like a carousel.
	 */

	pd1RenderView rview;
	float prevX, prevY;
	Bitmap bmBlueQueen;


	// Create the draw pile
	MyObject drawPile;

	// Create the up cards
	MyObject upCard1;
	MyObject upCard2;
	MyObject upCard3;

	ArrayList<MyObject> objects = new ArrayList<MyObject>();

	class pd1RenderView extends View {
		public pd1RenderView(Context context) {
			super(context);

			// Load the bitmap files
			AssetManager am;
			InputStream is;
			try {
				am = context.getAssets();
				is = am.open("sample_blue_queen.png");
				bmBlueQueen = BitmapFactory.decodeStream(is);
				is.close();
				MyDebug.Print(this.getClass().getSimpleName(), "sample_blue_queen.png format: " + bmBlueQueen.getConfig());
			} catch (IOException e) {
				// I don't know what to do here.
				MyDebug.Print(this.getClass().getSimpleName(), "Caught an exception while trying to load my bitmap file.");
			} finally {
				// Close input streams, I guess.
			}

			// Create the draw pile
			drawPile = new MyObject.Builder(193, 270).posX(640).posY(100).skin(bmBlueQueen).build();
			objects.add(drawPile);

			// Create the up cards
			upCard1 = new MyObject.Builder(193, 270).posX(940).posY(100).skin(bmBlueQueen).build();
			upCard2 = new MyObject.Builder(193, 270).posX(1240).posY(100).skin(bmBlueQueen).build();
			upCard3 = new MyObject.Builder(193, 270).posX(1540).posY(100).skin(bmBlueQueen).build();
			objects.add(upCard1);
			objects.add(upCard2);
			objects.add(upCard3);

			// Create some more cards
			objects.add(new MyObject.Builder(100, (int)(100 * 1.399f)).posX(0).posY(0).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(150, (int)(150 * 1.399f)).posX(0).posY(200).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(75, (int)(75 * 1.399f)).posX(0).posY(450).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(75, (int)(75 * 1.399f)).posX(100).posY(450).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(75, (int)(75 * 1.399f)).posX(200).posY(450).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(50, (int)(50 * 1.399f)).posX(300).posY(0).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(50, (int)(50 * 1.399f)).posX(300).posY(100).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(50, (int)(50 * 1.399f)).posX(300).posY(200).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(300, (int)(300 * 1.399f)).posX(640).posY(400).skin(bmBlueQueen).build());
			objects.add(new MyObject.Builder(400, (int)(400 * 1.399f)).posX(1240).posY(400).skin(bmBlueQueen).build());
		}

		protected void onDraw(Canvas canvas) {
			canvas.drawRGB(0, 200, 0);

			// Draw the objects
			for (MyObject obj : objects) {
				obj.Draw(canvas);
			}

			// Only need invalidate() if we are trying to animate?
			//invalidate();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		rview = new pd1RenderView(this);
		rview.setOnTouchListener(this);
		setContentView(rview);
	}

	public boolean onTouch(View v, MotionEvent me) {
		float pointX = me.getX();
		float pointY = me.getY();
		int act = me.getAction();
		switch (act) {
			case MotionEvent.ACTION_DOWN:
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_DOWN @ " + pointX + " , " + pointY);

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
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_MOVE @ " + pointX + " , " + pointY);

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
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_CANCEL @ " + pointX + " , " + pointY);
				break;
			case MotionEvent.ACTION_UP:
				MyDebug.Print(this.getClass().getSimpleName(), "ACTION_UP @ " + pointX + " , " + pointY);

				// Unselect all objects
				for (MyObject obj : objects) {
					obj.setUnselected();
				}

				break;
			default:
				MyDebug.Print(this.getClass().getSimpleName(), "Unhandled action " + act);
				break;
		}

		// Trigger redraw of view
		rview.invalidate();

		return true;
	}
}
