package com.nerdyneutrino.modron_monodrone;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;

public class PlayDeck1 extends Activity {

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

	Bitmap bmBlueQueen;

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
		}

		protected void onDraw(Canvas canvas) {
			canvas.drawRGB(0, 200, 0);

			// Create the draw pile
			MyObject drawPile = new MyObject.Builder(640, 100).skin(bmBlueQueen).scale(0.25f).build();

			// Create the up cards
			MyObject upCard1 = new MyObject.Builder(940, 100).skin(bmBlueQueen).scale(0.25f).build();
			MyObject upCard2 = new MyObject.Builder(1240, 100).skin(bmBlueQueen).scale(0.25f).build();
			MyObject upCard3 = new MyObject.Builder(1540, 100).skin(bmBlueQueen).scale(0.25f).build();

			// Draw the objects
			drawPile.Draw(canvas);
			upCard1.Draw(canvas);
			upCard2.Draw(canvas);
			upCard3.Draw(canvas);

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
		setContentView(new pd1RenderView(this));
	}
}
