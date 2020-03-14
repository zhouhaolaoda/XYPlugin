package com.xylink.sdk.sample.share.whiteboard.message;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class VetexDataBuffer {
	private float[] mPoints;
	private int mPointCount = 0;
	private float[] mColors;
	private int mColorCount = 0;
	private short[] mIndices;
	private int mIndicesCount = 0;
	private short[] mRealIndices;
	private float alpha = 1.0f;

	public float getAlpha() {
		return alpha;
	}
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
	
	public VetexDataBuffer(int count, float alpha) {
		mPoints = new float[(count + 8) * 10 * 3];
		mColors = new float[(count + 8) * 10 * 4];
		mIndices = new short[(count + 8) * 24];
		mRealIndices = new short[(count + 8) * 24];
		mPointCount = 0;
		mColorCount = 0;
		mIndicesCount = 0;
		this.alpha = alpha;
	}

	public void addPoint(Vec2f p) {
		mPoints[mPointCount++] = p.x;
		mPoints[mPointCount++] = p.y;
		mPoints[mPointCount++] = 0.0f;
	}

	public void addColor(float[] color) {
		mColors[mColorCount++] = color[0];
		mColors[mColorCount++] = color[1];
		mColors[mColorCount++] = color[2];
		mColors[mColorCount++] = color[3];
	}

	public void addIndex(short index) {
		mIndices[mIndicesCount++] = index;
	}

	public void commit(FloatBuffer vertexs, FloatBuffer colors, ShortBuffer indices) {
		short vertextStartIndex = (short)(vertexs.position()/3);
		for(int i = 0; i<mIndicesCount; i++) {
			mRealIndices[i] = (short)(vertextStartIndex + mIndices[i]);
		}
		vertexs.put(mPoints, 0, mPointCount);
		colors.put(mColors, 0, mColorCount);
		indices.put(mRealIndices, 0, mIndicesCount);
	}
}
