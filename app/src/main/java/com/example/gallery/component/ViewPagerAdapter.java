package com.example.gallery.component;

import android.animation.ObjectAnimator;
import android.gesture.Gesture;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    ArrayList<String> images;
    static ImageActivity imageActivity;
    public ViewPagerAdapter(ArrayList<String> images, ImageActivity imageActivity) {
        this.images = images;
        ViewPagerAdapter.imageActivity = imageActivity;
    }

    @NonNull
    @Override
    public ViewPagerAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page,parent,false);

        return new ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewPagerViewHolder holder, int position) {
        String image = images.get(position);
        Glide.with(holder.itemView).load(image).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        float scaleFactor = 1.0f;

        public void setScaleFactor(float scaleFactor) {
            // Ensure that the scaleFactor stays within the desired range
            this.scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));

            // Smooth scaling
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_X, this.scaleFactor);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_Y, this.scaleFactor);

            // Animation duration
            scaleXAnimator.setDuration(200);
            scaleYAnimator.setDuration(200);

            scaleXAnimator.start();
            scaleYAnimator.start();

            imageActivity.setViewPagerInputEnabled(scaleFactor == 1.0f);
        }

        public class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if(scaleFactor == 1.0f)
                    setScaleFactor(3.0f);
                else
                    setScaleFactor(1.0f);
                return true;
            }
        }

        public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                setScaleFactor(scaleFactor);
                return true;
            }

        }

        ImageView imageView;
        ScaleGestureDetector scaleGestureDetector;
        public ScaleListener scaleListener;

        GestureDetector gestureDetector;
        public GestureListener gestureListener;
        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.page_image);

            scaleListener = new ScaleListener();
            scaleGestureDetector = new ScaleGestureDetector(itemView.getContext(), scaleListener);

            gestureListener = new GestureListener();
            gestureDetector = new GestureDetector(itemView.getContext(), gestureListener);

            itemView.setOnTouchListener((v, event) -> {
                v.performClick();
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            });
        }
    }
}
