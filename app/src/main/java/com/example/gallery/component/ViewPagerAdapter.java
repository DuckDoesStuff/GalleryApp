package com.example.gallery.component;

import android.animation.ObjectAnimator;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;

import java.util.ArrayList;

//import me.saket.telephoto.zoomable.glide.ZoomableGlideImageKt;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    static ImageActivity imageActivity;
    ArrayList<String> images;

    public ViewPagerAdapter(ArrayList<String> images, ImageActivity imageActivity) {
        this.images = images;
        ViewPagerAdapter.imageActivity = imageActivity;
    }

    @NonNull
    @Override
    public ViewPagerAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page, parent, false);

        return new ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewPagerViewHolder holder, int position) {
        String image = images.get(position);
        //ZoomableGlideImageKt.glide(Glide.with(holder.itemView).load(image).centerInside().into(holder.imageView));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        final float minScale = 1.0f;
        final float maxScale = 5.0f;
        public ScaleListener scaleListener;
        public GestureListener gestureListener;
        float scaleFactor = 1.0f;
        boolean isScaling = false;
        PointF startPoint = new PointF();
        ImageView imageView;
        ScaleGestureDetector scaleGestureDetector;
        GestureDetector gestureDetector;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.page_image);

            scaleListener = new ScaleListener();
            scaleGestureDetector = new ScaleGestureDetector(itemView.getContext(), scaleListener);

            gestureListener = new GestureListener();
            gestureDetector = new GestureDetector(itemView.getContext(), gestureListener);

            itemView.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                scaleGestureDetector.onTouchEvent(event);
                if (!isScaling && imageView.getScaleX() != 1.0f && imageView.getScaleY() != 1.0f) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d("Log", "Coords: " + event.getX() + " " + event.getY());
                            startPoint.set(event.getX(), event.getY());
                            break;
                        case MotionEvent.ACTION_MOVE:
                            Log.d("Log", "Moving coords: " + event.getX() + " " + event.getY());
                            float dx = event.getX() - startPoint.x;
                            float dy = event.getY() - startPoint.y;
                            updatePan(dx, dy);
                            startPoint.set(event.getX(), event.getY());
                            break;
                    }
                }
                v.performClick();
                return true;
            });
        }

        public void setSmoothScaleFactor(float scaleFactor) {
            this.scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));

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

        public void setScaleFactor(float scaleFactor) {
            this.scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
            imageActivity.setViewPagerInputEnabled(scaleFactor == 1.0f);
            imageView.setScaleX(this.scaleFactor);
            imageView.setScaleY(this.scaleFactor);
        }

        private void updatePan(float dx, float dy) {
            float currentTransX = imageView.getTranslationX();
            float currentTransY = imageView.getTranslationY();
            float newTransX = Math.min(Math.abs(currentTransX + dx), imageView.getWidth());
            float newTransY = Math.min(Math.abs(currentTransY + dy), imageView.getHeight());


            imageView.setTranslationX(newTransX);
            imageView.setTranslationY(newTransY);
        }

        public class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (scaleFactor == 1.0f) {
                    setSmoothScaleFactor(3.0f);
                } else {
                    setSmoothScaleFactor(1.0f);
                    imageView.setX(0);
                    imageView.setY(0);
                }

                return true;
            }
        }

        public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                isScaling = true;
                scaleFactor *= detector.getScaleFactor();
                setScaleFactor(scaleFactor);
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                super.onScaleEnd(detector);
                isScaling = false;
            }


        }
    }
}
