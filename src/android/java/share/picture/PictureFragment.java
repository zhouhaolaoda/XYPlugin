package com.xylink.sdk.sample.share.picture;

import android.log.L;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

// import com.xylink.sdk.sample.R;
import com.sdjy.allLink.R;

public class PictureFragment extends Fragment {
    private ImageView ivPicture;
    private OnPagerClickListener listener;


    public interface OnPagerClickListener {
        void onPagerClicked();
    }

    public void setOnPagerClickListener(OnPagerClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivPicture = view.findViewById(R.id.iv_picture);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String path = arguments.getString("path");
            Glide.with(this).load(path).into(ivPicture);
            ivPicture.setOnClickListener(v -> {
                L.i("wang", "click......" + listener);
                if (listener != null) {
                    listener.onPagerClicked();
                }
            });
        }
    }
}
