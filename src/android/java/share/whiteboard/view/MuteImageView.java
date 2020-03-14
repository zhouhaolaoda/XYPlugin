package com.xylink.sdk.sample.share.whiteboard.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

public class MuteImageView extends AppCompatImageView {
	
	private volatile boolean isMuted = false;
	private Drawable mMuteDrawable = null;
	private Drawable mUnmuteDrawable = null;

	public MuteImageView(Context context) {
		super(context);
	}
	
	public MuteImageView(Context context, Drawable muteDraw, Drawable unmuteDraw) {
		super(context);
		mMuteDrawable = muteDraw;
		mUnmuteDrawable = unmuteDraw;
	}

	public MuteImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public MuteImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.MuteImageView);
		mMuteDrawable = arr.getDrawable(R.styleable.MuteImageView_mutePhoto);
		mUnmuteDrawable = arr.getDrawable(R.styleable.MuteImageView_unmutePhoto);
		setMuted(arr.getBoolean(R.styleable.MuteImageView_isMute, false));
		arr.recycle();
	}

	public boolean isMuted() {
		return isMuted;
	}

	public void setMuted(boolean muted) {
		isMuted = muted;
		setImageDrawable(isMuted ? mMuteDrawable : mUnmuteDrawable);
	}
}
