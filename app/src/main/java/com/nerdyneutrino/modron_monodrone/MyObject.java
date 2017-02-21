package com.nerdyneutrino.modron_monodrone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class MyObject {
	private int width;
	private int height;
	private float pos_x;
	private float pos_y;
	private float vel_x;
	private float vel_y;
	private Bitmap skin;
	private Rect dst = new Rect();
	private boolean selected = false;

	public static class Builder {
		private int width;
		private int height;
		private float pos_x = 0;
		private float pos_y = 0;
		private float vel_x = 0;
		private float vel_y = 0;
		private Bitmap skin = null;

		public Builder(int w, int h) {
			// Width and Height must be non-negative
			this.width = Math.abs(w);
			this.height = Math.abs(h);
		}

		public Builder posX(float val) {
			this.pos_x = val;
			return this;
		}

		public Builder posY(float val) {
			this.pos_y = val;
			return this;
		}

		public Builder velX(float val) {
			this.vel_x = val;
			return this;
		}

		public Builder velY(float val) {
			this.vel_y = val;
			return this;
		}

		public Builder skin(Bitmap val) {
			this.skin = val;
			return this;
		}

		public MyObject build() {
			return new MyObject(this);
		}
	}

	private MyObject(Builder b) {
		width = b.width;
		height = b.height;
		pos_x = b.pos_x;
		pos_y = b.pos_y;
		dst.set((int) pos_x, (int) pos_y, (int) pos_x + width - 1, (int) pos_y + height - 1);
		vel_x = b.vel_x;
		vel_y = b.vel_y;
		skin = b.skin;
	}

	void updatePosition(float deltaT) {
		pos_x = pos_x + vel_x * deltaT;
		pos_y = pos_y + vel_y * deltaT;
		dst.set((int) pos_x, (int) pos_y, (int) pos_x + width - 1, (int) pos_y + height - 1);
	}

	void updatePosition(float deltaX, float deltaY) {
		pos_x += deltaX;
		pos_y += deltaY;
		dst.set((int) pos_x, (int) pos_y, (int) pos_x + width - 1, (int) pos_y + height - 1);
	}

	void Draw(Canvas canvas) {
		if (skin != null) {
			if (selected) {
				Rect border = new Rect(dst);
				border.inset(-5, -5);
				Paint myRed = new Paint();
				myRed.setColor(Color.RED);
				myRed.setStyle(Paint.Style.FILL_AND_STROKE);
				canvas.drawRect(border, myRed);
			}
			canvas.drawBitmap(skin, null, dst, null);
		}
	}

	void setX(int val) {
		pos_x = val;
		dst.set((int) pos_x, (int) pos_y, (int) pos_x + width - 1, (int) pos_y + height - 1);
	}

	void setY(int val) {
		pos_y = val;
		dst.set((int) pos_x, (int) pos_y, (int) pos_x + width - 1, (int) pos_y + height - 1);
	}

	void setSelected() {
		MyDebug.Print(this.getClass().getSimpleName(), "setSelected()");
		selected = true;
	}

	void setUnselected() {
		MyDebug.Print(this.getClass().getSimpleName(), "setUnselected()");
		selected = false;
	}

	boolean isSelected() {
		return selected;
	}

	boolean contains(float x, float y) {
		if (dst.contains((int)x, (int)y))
			return true;
		return false;
	}

	boolean pastX(int val) {
		if (pos_x + width > val)
			return true;
		else
			return false;
	}

	boolean pastY(int val) {
		if (pos_y + height > val)
			return true;
		else
			return false;
	}
}

