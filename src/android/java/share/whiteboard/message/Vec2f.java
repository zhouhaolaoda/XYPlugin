package com.xylink.sdk.sample.share.whiteboard.message;
//coordinate is align with opengl

public class Vec2f {
	public float x;
	public float y;

	public Vec2f(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(null == o) {
			return false;
		}
		
		if(o instanceof Vec2f) {
			Vec2f other = (Vec2f)o;
			return other.x == x && other.y == y;
		}
		return false;
	}

	public static Vec2f minus(Vec2f first, Vec2f second) {
		return new Vec2f(first.x - second.x, first.y - second.y);
	}
	
	public static Vec2f add(Vec2f first, Vec2f second) {
		return new Vec2f(first.x + second.x, first.y + second.y);
	}

	public Vec2f normalized() {
		float len = (float) Math.sqrt(x*x + y*y);
		try {
			x /= len;
			y /= len;
			return this;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public float dot(Vec2f other) {
		return x*other.x + y* other.y;
	}

	
	public void scale(float factor) {
		this.x *= factor;
		this.y *= factor;
	}
	
	@Override
	public String toString() {
		return "Vec2f [x=" + x + ", y=" + y + "]";
	}
}