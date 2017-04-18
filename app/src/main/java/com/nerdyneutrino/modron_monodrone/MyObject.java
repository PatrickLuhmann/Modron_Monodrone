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
	private float accel;
	private int backgroundColor;
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
		private float accel = 0;
		private Bitmap skin = null;
		private int backgroundColor = 0;

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

		public Builder accel(float val) {
			this.accel = val;
			return this;
		}

		public Builder skin(Bitmap val) {
			this.skin = val;
			return this;
		}

		public Builder background(int color) {
			this.backgroundColor = color;
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
		accel = b.accel;
		skin = b.skin;
		backgroundColor = b.backgroundColor;
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
		if (backgroundColor != 0x00000000) {
			Rect back = new Rect(dst);
			Paint paint = new Paint();
			paint.setColor(backgroundColor);
			canvas.drawRect(back, paint);
		}
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

	void setVelX(float val) {
		vel_x = val;
	}

	void setVelY(float val) {
		vel_y = val;
	}

	void changeVelX(float val) {
		vel_x += val;
	}

	void changeVelY(float val) {
		vel_y += val;
		MyDebug.Print(this.getClass().getSimpleName(), "New vel_y: " + vel_y);
	}

	void scaleVelX(float val) {
		vel_x *= val;
	}

	void scaleVelY(float val) {
		vel_y *= val;
		MyDebug.Print(this.getClass().getSimpleName(), "New vel_y: " + vel_y);
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

	boolean intersects(MyObject obj) {
		return dst.intersect(obj.dst);
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

	public void scaleWidth(float factor) {
		width *= factor;
	}

	public void scaleHeight(float factor) {
		height *= factor;
	}

	public void scale(float factor) {
		scaleWidth(factor);
		scaleHeight(factor);
	}

	public void setWidth(int w, boolean scale) {
		if (scale) {
			float factor = (float)w / (float)width;
			height *= factor;
		}
		width = w;
	}

	public void setHeight(int h, boolean scale) {
		if (scale) {
			float factor = (float)h / (float)height;
			width *= factor;
		}
		height = h;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	float getAccel() {
		return accel;
	}

	public void applyGravity(MyObject src, float deltaT) {
		// Do different things based on the type of gravity source:
		// <none>, <point>, <line>
		changeVelY(src.getAccel() * deltaT);
	}
}

