package com.xylink.sdk.sample.share.whiteboard.message;

//coordinate  is whiteboard's
public class Point{
	public static final int NORMAL_SCALE = 1000;
	
	private Line line;
	private int x;
	private int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		line = null;
	}

	public Point(int x, int y, Line line) {
		this(x,y);
		this.line = line;
	}

	public Line getLine() {
		return line;
	}

	public void add2UnDrawn(Line line) {
		this.line = line;
		line.addUndrawnPoint(this);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Vec2f toOpenGLCoordinate(float aspectRadio) {
		return new Vec2f( ((float)x)/500 - 1.0f, (1.0f - ((float)y) / 500) * aspectRadio);
	}
	
	@Override
	public String toString() {
		return "Point [line=" + line + ", getX()=" + getX() + ", getY()="
				+ getY() + "]";
	}

}
